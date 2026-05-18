package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vitalwear.protos.Character
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.utils.DeviceType
import com.google.protobuf.ByteString
import kotlinx.coroutines.flow.firstOrNull

internal const val DEFAULT_VITALWEAR_TRAINING_TIME_SECONDS = 100L * 60L * 60L

internal fun resolveTrainingSeconds(
    deviceType: DeviceType,
    beData: BECharacterData?,
): Long {
    val remainingTrainingTimeInMinutes = beData?.remainingTrainingTimeInMinutes
    if (remainingTrainingTimeInMinutes != null) {
        return remainingTrainingTimeInMinutes.toLong() * 60L
    }

    // VBHelper only stores the explicit training timer on BE payloads. When exporting non-BE
    // characters to VitalWear, preserve their ability to train by seeding the same default
    // window VitalWear uses for freshly created partners instead of sending an immediate zero.
    return if (deviceType == DeviceType.BEDevice) {
        0L
    } else {
        DEFAULT_VITALWEAR_TRAINING_TIME_SECONDS
    }
}

class VitalWearCharacterExporter(
    private val database: AppDatabase
) {
    /**
     * Builds a Character proto from the stored character data.
     * Used by the HCE ISO-DEP transfer path.
     *
     * @param characterId ID of the character to export
     * @param forcedTransferProfile If specified (e.g., DeviceType.BEDevice for HCE), overrides stored device type
     *                              and ensures stats are serialized for the target ecosystem.
     *                              Used to lock VitalWear (HCE) transfers to BE profile.
     */
    suspend fun buildCharacterProto(
        characterId: Long,
        forcedTransferProfile: DeviceType? = null,
    ): Character {
        val characterWithSprites = database.userCharacterDao().getCharacterWithSprites(characterId)
        val userCharacter = database.userCharacterDao().getCharacter(characterId)
        val cardCharacter = database.characterDao().getCharacterById(userCharacter.charId)
            ?: error("Card character not found for user character $characterId")
        val sprite = database.spriteDao().getSpriteById(cardCharacter.spriteId)
            ?: error("Sprite not found for user character $characterId")
        val characterInfo = database.characterDao().getCharacterInfo(userCharacter.charId)
        val vwSettings = database.vitalWearSettingsDao().getByCharacterId(characterId)
        val card = database.cardDao().getCardByCharacterIdSync(characterId)
            ?: error("Card not found for character $characterId")
        val cardProgress = database.cardProgressDao().getCardProgressSync(card.id) ?: 0
        val beData = database.userCharacterDao().getBeDataOrNull(characterId)
        val vbData = database.userCharacterDao().getVbDataOrNull(characterId)
        val normalizedTransformationCountdownMinutes = normalizeTransformationCountdownMinutes(
            transformationCountdownMinutes = userCharacter.transformationCountdown,
            hasPossibleTransformations = hasPossibleTransformations(userCharacter.charId),
        )

        // Use forced profile if specified (e.g., BE for HCE route), otherwise use stored type.
        // For HCE/VitalWear routes, forcedTransferProfile = BE ensures BE stats are serialized.
        val transferDeviceType = forcedTransferProfile ?: userCharacter.characterType

        val settingsBuilder = Character.Settings.newBuilder()
            .setTrainingInBackground(vwSettings?.trainingInBackground ?: false)
            .setAllowedBattles(resolveAllowedBattles(vwSettings?.allowedBattles))

        // VitalWear chooses DIM-vs-BE runtime path from card type + assumedFranchise.
        // For forced BE HCE exports of DIM cards, provide a stable assumed franchise hint.
        if (forcedTransferProfile == DeviceType.BEDevice && !card.isBEm) {
            settingsBuilder.setAssumedFranchise(0)
        }

        return Character.newBuilder()
            .setCardId(card.cardId)
            .setCardName(card.name)
            .setCharacterStats(
                Character.CharacterStats.newBuilder()
                    .setSlotId(characterInfo.charId)
                    .setVitals(characterWithSprites.vitalPoints)
                    .setTrainingTimeRemainingInSeconds(resolveTrainingSeconds(transferDeviceType, beData))
                    .setTimeUntilNextTransformation(normalizedTransformationCountdownMinutes.toLong() * 60L)
                    .setTrainedBp(resolveTrainedBp(beData))
                    .setTrainedHp(resolveTrainedHp(beData))
                    .setTrainedAp(resolveTrainedAp(beData))
                    .setTrainedPp(characterWithSprites.trophies)
                    .setInjured(characterWithSprites.injuryStatus.name.lowercase().contains("inj"))
                    .setAccumulatedDailyInjuries(vwSettings?.accumulatedDailyInjuries ?: 0)
                    .setTotalBattles(characterWithSprites.totalBattlesWon + characterWithSprites.totalBattlesLost)
                    .setCurrentPhaseBattles(characterWithSprites.currentPhaseBattlesWon + characterWithSprites.currentPhaseBattlesLost)
                    .setTotalWins(characterWithSprites.totalBattlesWon)
                    .setCurrentPhaseWins(characterWithSprites.currentPhaseBattlesWon)
                    .setMood(characterWithSprites.mood)
                    .setDeviceType(transferDeviceType.toTransferDeviceType())
                    .setAgeInDays(userCharacter.ageInDays.coerceAtLeast(0))
                    .setActivityLevel(userCharacter.activityLevel.coerceAtLeast(0))
                    .setHeartRateCurrent(userCharacter.heartRateCurrent.coerceAtLeast(0))
                    .setGeneration(vbData?.generation ?: 0)
                    .setTotalTrophies(vbData?.totalTrophies ?: userCharacter.trophies.coerceAtLeast(0))
                    .setNextAdventureMissionStage(characterInfo.currentStage.coerceAtLeast(0))
                    .setItemEffectMentalStateValue(beData?.itemEffectMentalStateValue ?: 0)
                    .setItemEffectMentalStateMinutesRemaining(beData?.itemEffectMentalStateMinutesRemaining ?: 0)
                    .setItemEffectActivityLevelValue(beData?.itemEffectActivityLevelValue ?: 0)
                    .setItemEffectActivityLevelMinutesRemaining(beData?.itemEffectActivityLevelMinutesRemaining ?: 0)
                    .setItemEffectVitalPointsChangeValue(beData?.itemEffectVitalPointsChangeValue ?: 0)
                    .setItemEffectVitalPointsChangeMinutesRemaining(beData?.itemEffectVitalPointsChangeMinutesRemaining ?: 0)
                    .setAbilityRarity(beData?.abilityRarity?.ordinal ?: 0)
                    .setAbilityType(beData?.abilityType ?: 0)
                    .setAbilityBranch(beData?.abilityBranch ?: 0)
                    .setAbilityReset(beData?.abilityReset ?: 0)
                    .setRank(beData?.rank ?: 0)
                    .setItemType(beData?.itemType ?: 0)
                    .setItemMultiplier(beData?.itemMultiplier ?: 0)
                    .setItemRemainingTime(beData?.itemRemainingTime ?: 0)
                    .setFirmwareMinorVersion(beData?.minorVersion ?: 0)
                    .setFirmwareMajorVersion(beData?.majorVersion ?: 0)
                    .build()
            )
            .setSettings(settingsBuilder.build())
            .putMaxAdventureCompletedByCard(card.name, (cardProgress - 1).coerceAtLeast(0))
            .addAllTransformationHistory(
                database.userCharacterDao().getTransformationHistoryForExport(characterId).map {
                    Character.TransformationEvent.newBuilder()
                        .setCardName(it.cardName)
                        .setPhase(0)
                        .setSlotId(it.monIndex)
                        .build()
                }
            )
            .setTransferCard(
                Character.TransferCard.newBuilder()
                    .setCardId(card.cardId)
                    .setCardName(card.name)
                    .setIsBem(card.isBEm)
                    .setStageCount(card.stageCount)
                    .setFranchise(0)
                    .build()
            )
            .setTransferSpecies(
                Character.TransferSpecies.newBuilder()
                    .setSlotId(characterInfo.charId)
                    .setStage(cardCharacter.stage)
                    .setAttribute(cardCharacter.attribute.ordinal)
                    .setBaseHp(cardCharacter.baseHp)
                    .setBaseBp(cardCharacter.baseBp)
                    .setBaseAp(cardCharacter.baseAp)
                    .setNameSprite(ByteString.copyFrom(cardCharacter.nameSprite))
                    .setNameSpriteWidth(cardCharacter.nameWidth)
                    .setNameSpriteHeight(cardCharacter.nameHeight)
                    .setIdle1(ByteString.copyFrom(sprite.spriteIdle1))
                    .setIdle2(ByteString.copyFrom(sprite.spriteIdle2))
                    .setWalk1(ByteString.copyFrom(sprite.spriteWalk1))
                    .setWalk2(ByteString.copyFrom(sprite.spriteWalk2))
                    .setRun1(ByteString.copyFrom(sprite.spriteRun1))
                    .setRun2(ByteString.copyFrom(sprite.spriteRun2))
                    .setTrain1(ByteString.copyFrom(sprite.spriteTrain1))
                    .setTrain2(ByteString.copyFrom(sprite.spriteTrain2))
                    .setWin(ByteString.copyFrom(sprite.spriteHappy))
                    .setDown(ByteString.copyFrom(sprite.spriteSleep))
                    .setAttack(ByteString.copyFrom(sprite.spriteAttack))
                    .setDodge(ByteString.copyFrom(sprite.spriteDodge))
                    .setSplash(ByteString.copyFrom(sprite.spriteIdle1))
                    .setSpriteWidth(sprite.width)
                    .setSpriteHeight(sprite.height)
                    .build()
            )
            .build()
    }


    private fun resolveTrainedBp(beData: com.github.nacabaro.vbhelper.domain.device_data.BECharacterData?): Int {
        return beData?.trainingBp ?: 0
    }

    private fun resolveTrainedHp(beData: com.github.nacabaro.vbhelper.domain.device_data.BECharacterData?): Int {
        return beData?.trainingHp ?: 0
    }

    private fun resolveTrainedAp(beData: com.github.nacabaro.vbhelper.domain.device_data.BECharacterData?): Int {
        return beData?.trainingAp ?: 0
    }

    private suspend fun hasPossibleTransformations(cardCharacterId: Long): Boolean {
        return database.characterDao().getEvolutionRequirementsForCard(cardCharacterId).firstOrNull()?.isNotEmpty() == true
    }


    private fun resolveAllowedBattles(rawValue: Int?): Character.Settings.AllowedBattles {
        if (rawValue == null) return Character.Settings.AllowedBattles.CARD_ONLY
        return Character.Settings.AllowedBattles.forNumber(rawValue)
            ?: Character.Settings.AllowedBattles.CARD_ONLY
    }

}
