package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vitalwear.protos.Character
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.utils.DeviceType
import kotlin.math.max

class VitalWearCharacterImporter(
    private val database: AppDatabase
) {
    data class ImportResult(
        val success: Boolean,
        val message: String
    )

    fun importCharacter(character: Character): ImportResult {
        val importedCard = resolveCard(character)
            ?: return ImportResult(
                success = false,
                message = "Matching card not found in VBHelper. Import that card first."
            )

        val slotId = character.characterStats.slotId
        val cardCharacter = runCatching {
            database.characterDao().getCharacterByMonIndex(slotId, importedCard.id)
        }.getOrNull() ?: return ImportResult(
            success = false,
            message = "Character slot $slotId was not found on card ${importedCard.name}."
        )

        database.userCharacterDao().clearActiveCharacter()

        val totalBattles = character.characterStats.totalBattles
        val totalWins = character.characterStats.totalWins.coerceAtMost(totalBattles)
        val currentPhaseBattles = character.characterStats.currentPhaseBattles
        val currentPhaseWins = character.characterStats.currentPhaseWins.coerceAtMost(currentPhaseBattles)

        val deviceType = resolveDeviceType(character, importedCard)

        val userCharacterId = database.userCharacterDao().insertCharacterData(
            UserCharacter(
                charId = cardCharacter.id,
                ageInDays = max(character.transformationHistoryCount - 1, 0),
                mood = character.characterStats.mood,
                vitalPoints = character.characterStats.vitals,
                transformationCountdown = secondsToMinutes(character.characterStats.timeUntilNextTransformation),
                injuryStatus = resolveInjuryStatus(character.characterStats.injured),
                trophies = character.characterStats.trainedPp,
                currentPhaseBattlesWon = currentPhaseWins,
                currentPhaseBattlesLost = (currentPhaseBattles - currentPhaseWins).coerceAtLeast(0),
                totalBattlesWon = totalWins,
                totalBattlesLost = (totalBattles - totalWins).coerceAtLeast(0),
                activityLevel = 0,
                heartRateCurrent = 0,
                characterType = deviceType,
                isActive = true
            )
        )

        if (deviceType == DeviceType.BEDevice) {
            database.userCharacterDao().insertBECharacterData(
                BECharacterData(
                    id = userCharacterId,
                    trainingHp = character.characterStats.trainedHp,
                    trainingAp = character.characterStats.trainedAp,
                    trainingBp = character.characterStats.trainedBp,
                    remainingTrainingTimeInMinutes = secondsToMinutes(character.characterStats.trainingTimeRemainingInSeconds),
                    itemEffectMentalStateValue = 0,
                    itemEffectMentalStateMinutesRemaining = 0,
                    itemEffectActivityLevelValue = 0,
                    itemEffectActivityLevelMinutesRemaining = 0,
                    itemEffectVitalPointsChangeValue = 0,
                    itemEffectVitalPointsChangeMinutesRemaining = 0,
                    abilityRarity = resolveDefaultAbilityRarity(),
                    abilityType = 0,
                    abilityBranch = 0,
                    abilityReset = 0,
                    rank = 0,
                    itemType = 0,
                    itemMultiplier = 0,
                    itemRemainingTime = 0,
                    otp0 = "",
                    otp1 = "",
                    minorVersion = 0,
                    majorVersion = 0
                )
            )
        } else {
            database.userCharacterDao().insertVBCharacterData(
                VBCharacterData(
                    id = userCharacterId,
                    generation = 0,
                    totalTrophies = character.characterStats.trainedPp
                )
            )
        }

        val now = System.currentTimeMillis()
        database.dexDao().insertCharacter(slotId, importedCard.id, now)

        for (transformation in character.transformationHistoryList) {
            val transformationCard = resolveCard(transformation.cardName, character.cardId)
            if (transformationCard != null) {
                database.userCharacterDao().insertTransformation(
                    userCharacterId,
                    transformation.slotId,
                    transformationCard.id,
                    now
                )
                database.dexDao().insertCharacter(transformation.slotId, transformationCard.id, now)
            }
        }

        for ((cardName, maxAdventureCompleted) in character.maxAdventureCompletedByCardMap) {
            val adventureCard = resolveCard(cardName, null) ?: continue
            val currentStage = (maxAdventureCompleted + 1).coerceAtLeast(0)
            database.cardProgressDao().updateCardProgress(
                currentStage = currentStage,
                cardId = adventureCard.id,
                unlocked = currentStage > adventureCard.stageCount
            )
        }

        return ImportResult(
            success = true,
            message = "Imported ${importedCard.name} slot $slotId from VitalWear."
        )
    }

    /**
     * Determines whether the imported VitalWear character is a VB DiM or a BE character.
     *
     * The VitalWear watch reports this via the proto's transferDeviceType field. Older
     * exports leave it UNSPECIFIED, in which case we fall back to the matched card: BEm
     * cards are always BE, everything else defaults to VB (the original Vital Bracelet).
     */
    private fun resolveDeviceType(
        character: Character,
        card: com.github.nacabaro.vbhelper.domain.card.Card
    ): DeviceType {
        return when (character.characterStats.transferDeviceType) {
            Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_BE -> DeviceType.BEDevice
            Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_VB -> DeviceType.VBDevice
            else -> if (card.isBEm) DeviceType.BEDevice else DeviceType.VBDevice
        }
    }

    private fun resolveCard(character: Character) = resolveCard(character.cardName, character.cardId)

    private fun resolveCard(cardName: String?, cardId: Int?): com.github.nacabaro.vbhelper.domain.card.Card? {
        if (!cardName.isNullOrBlank()) {
            database.cardDao().getCardByName(cardName)?.let { return it }
        }

        if (cardId != null) {
            val matches = database.cardDao().getCardByCardId(cardId)
            if (matches.size == 1) {
                return matches.first()
            }
        }

        return null
    }

    private fun secondsToMinutes(seconds: Long): Int {
        if (seconds <= 0L) {
            return 0
        }
        return (seconds / 60L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    private fun resolveInjuryStatus(injured: Boolean): NfcCharacter.InjuryStatus {
        val statuses = enumValues<NfcCharacter.InjuryStatus>()
        if (!injured) {
            return statuses.firstOrNull {
                val normalized = it.name.lowercase()
                normalized.contains("none") || normalized.contains("normal") || normalized.contains("healthy")
            } ?: statuses.first()
        }

        return statuses.firstOrNull {
            val normalized = it.name.lowercase()
            normalized.contains("inj")
        } ?: statuses.last()
    }

    private fun resolveDefaultAbilityRarity(): NfcCharacter.AbilityRarity {
        return enumValues<NfcCharacter.AbilityRarity>().first()
    }
}
