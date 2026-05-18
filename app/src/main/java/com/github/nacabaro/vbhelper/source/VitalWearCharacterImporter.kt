package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vitalwear.common.data.SharedTransferSeenDao
import com.github.cfogrady.vitalwear.protos.Character
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.domain.card.CardCharacter
import com.github.nacabaro.vbhelper.domain.card.CardProgress
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.domain.device_data.VitalWearCharacterSettings
import com.github.nacabaro.vbhelper.domain.characters.Sprite
import com.github.nacabaro.vbhelper.utils.DeviceType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import kotlin.math.max

class VitalWearCharacterImporter(
    private val database: AppDatabase,
    private val transferSeenDao: SharedTransferSeenDao,
) {
    data class ImportResult(
        val success: Boolean,
        val message: String
    )

    fun importCharacter(character: Character): ImportResult {
        val importedCard = resolveCard(character)
            ?: createPlaceholderCard(character)
            ?: return ImportResult(
                success = false,
                message = "Matching card not found in VBHelper. Import that card first."
            )

        val slotId = character.characterStats.slotId
        val cardCharacter = database.characterDao().getCharacterByMonIndexOrNull(slotId, importedCard.id)
            ?: createPlaceholderCharacter(importedCard, character)
            ?: return ImportResult(
            success = false,
            message = "Character slot $slotId was not found on card ${importedCard.name}."
        )

        database.userCharacterDao().clearActiveCharacter()

        val totalBattles = character.characterStats.totalBattles
        val totalWins = character.characterStats.totalWins.coerceAtMost(totalBattles)
        val currentPhaseBattles = character.characterStats.currentPhaseBattles
        val currentPhaseWins = character.characterStats.currentPhaseWins.coerceAtMost(currentPhaseBattles)
        val normalizedTransformationCountdown = normalizeTransformationCountdownMinutes(
            transformationCountdownMinutes = secondsToMinutes(character.characterStats.timeUntilNextTransformation),
            hasPossibleTransformations = hasPossibleTransformations(cardCharacter.id),
        )
        val hasBePayloadStats = character.characterStats.trainedHp > 0 ||
            character.characterStats.trainedAp > 0 ||
            character.characterStats.trainedBp > 0 ||
            character.characterStats.trainingTimeRemainingInSeconds > 0L
        val fallbackIsBeCharacter = importedCard.isBEm || hasBePayloadStats
        val deviceType = resolveDeviceType(
            transferDeviceType = character.characterStats.deviceType,
            fallbackIsBeCharacter = fallbackIsBeCharacter,
        )
        val isBeCharacter = deviceType == DeviceType.BEDevice

        val userCharacterId = database.userCharacterDao().insertCharacterData(
            UserCharacter(
                charId = cardCharacter.id,
                ageInDays = character.characterStats.ageInDays.takeIf { it > 0 }
                    ?: max(character.transformationHistoryCount - 1, 0),
                mood = character.characterStats.mood,
                vitalPoints = character.characterStats.vitals,
                transformationCountdown = normalizedTransformationCountdown,
                injuryStatus = resolveInjuryStatus(character.characterStats.injured),
                trophies = character.characterStats.trainedPp,
                currentPhaseBattlesWon = currentPhaseWins,
                currentPhaseBattlesLost = (currentPhaseBattles - currentPhaseWins).coerceAtLeast(0),
                totalBattlesWon = totalWins,
                totalBattlesLost = (totalBattles - totalWins).coerceAtLeast(0),
                activityLevel = character.characterStats.activityLevel.coerceAtLeast(0),
                heartRateCurrent = character.characterStats.heartRateCurrent.coerceAtLeast(0),
                characterType = deviceType,
                isActive = true
            )
        )

        runBlocking {
            database.vitalWearSettingsDao().upsert(
                VitalWearCharacterSettings(
                    characterId = userCharacterId,
                    trainingInBackground = character.settings.trainingInBackground,
                    allowedBattles = character.settings.allowedBattles.number,
                    accumulatedDailyInjuries = character.characterStats.accumulatedDailyInjuries,
                )
            )
        }

        val vbProfile = VBCharacterData(
            id = userCharacterId,
            generation = if (character.characterStats.generation > 0) {
                character.characterStats.generation
            } else {
                max(character.transformationHistoryCount - 1, 0)
            },
            totalTrophies = if (character.characterStats.totalTrophies > 0) {
                character.characterStats.totalTrophies
            } else {
                character.characterStats.trainedPp.coerceAtLeast(0)
            }
        )
        database.userCharacterDao().insertVBCharacterData(vbProfile)

        if (isBeCharacter) {
            val abilityRarity = resolveAbilityRarity(character.characterStats.abilityRarity)
            database.userCharacterDao().insertBECharacterData(
                BECharacterData(
                    id = userCharacterId,
                    trainingHp = character.characterStats.trainedHp,
                    trainingAp = character.characterStats.trainedAp,
                    trainingBp = character.characterStats.trainedBp,
                    remainingTrainingTimeInMinutes = secondsToMinutes(character.characterStats.trainingTimeRemainingInSeconds),
                    itemEffectMentalStateValue = character.characterStats.itemEffectMentalStateValue,
                    itemEffectMentalStateMinutesRemaining = character.characterStats.itemEffectMentalStateMinutesRemaining,
                    itemEffectActivityLevelValue = character.characterStats.itemEffectActivityLevelValue,
                    itemEffectActivityLevelMinutesRemaining = character.characterStats.itemEffectActivityLevelMinutesRemaining,
                    itemEffectVitalPointsChangeValue = character.characterStats.itemEffectVitalPointsChangeValue,
                    itemEffectVitalPointsChangeMinutesRemaining = character.characterStats.itemEffectVitalPointsChangeMinutesRemaining,
                    abilityRarity = abilityRarity,
                    abilityType = character.characterStats.abilityType,
                    abilityBranch = character.characterStats.abilityBranch,
                    abilityReset = character.characterStats.abilityReset,
                    rank = character.characterStats.rank,
                    itemType = character.characterStats.itemType,
                    itemMultiplier = character.characterStats.itemMultiplier,
                    itemRemainingTime = character.characterStats.itemRemainingTime,
                    otp0 = "",
                    otp1 = "",
                    minorVersion = character.characterStats.firmwareMinorVersion,
                    majorVersion = character.characterStats.firmwareMajorVersion
                )
            )
        }

        val now = System.currentTimeMillis()
        markSeen(importedCard.name, slotId, importedCard.id, now)

        var insertedTransformationCount = 0
        for (transformation in character.transformationHistoryList) {
            val transformationCard = resolveRelatedCard(
                incomingCardName = transformation.cardName,
                incomingCardId = character.cardId,
                matchedRootCard = importedCard,
                incomingRootCardName = character.cardName,
            )
            if (transformationCard != null) {
                database.userCharacterDao().insertTransformation(
                    userCharacterId,
                    transformation.slotId,
                    transformationCard.id,
                    now
                )
                insertedTransformationCount++
                markSeen(transformationCard.name, transformation.slotId, transformationCard.id, now)
            }
        }

        if (insertedTransformationCount == 0) {
            // Keep HomeScreen renderable for freshly imported characters with empty history.
            database.userCharacterDao().insertTransformation(
                userCharacterId,
                slotId,
                importedCard.id,
                now
            )
        }

        for ((cardName, maxAdventureCompleted) in character.maxAdventureCompletedByCardMap) {
            val adventureCard = resolveRelatedCard(
                incomingCardName = cardName,
                incomingCardId = null,
                matchedRootCard = importedCard,
                incomingRootCardName = character.cardName,
            ) ?: continue
            val currentStage = (maxAdventureCompleted + 1).coerceAtLeast(0)
            database.cardProgressDao().updateCardProgress(
                currentStage = currentStage,
                cardId = adventureCard.id,
                unlocked = currentStage > adventureCard.stageCount
            )
        }

        val nextAdventureMissionStage = character.characterStats.nextAdventureMissionStage.coerceAtLeast(0)
        if (nextAdventureMissionStage > 0) {
            database.cardProgressDao().updateCardProgress(
                currentStage = nextAdventureMissionStage,
                cardId = importedCard.id,
                unlocked = nextAdventureMissionStage > importedCard.stageCount,
            )
        }

        return ImportResult(
            success = true,
            message = "Imported ${importedCard.name} slot $slotId from VitalWear."
        )
    }

    /**
     * Import a VitalWear character with a forced device type.
     * Used by HCE (VitalWear) read path to ensure imports are marked as BE.
     * 
     * @param character Protobuf character from VitalWear HCE
     * @param forcedDeviceType Always use this device type for the imported character (e.g., BEDevice for HCE)
     */
    fun importCharacter(character: Character, forcedDeviceType: DeviceType): ImportResult {
        val importedCard = resolveCard(character)
            ?: createPlaceholderCard(character)
            ?: return ImportResult(
                success = false,
                message = "Matching card not found in VBHelper. Import that card first."
            )

        val slotId = character.characterStats.slotId
        val cardCharacter = database.characterDao().getCharacterByMonIndexOrNull(slotId, importedCard.id)
            ?: createPlaceholderCharacter(importedCard, character)
            ?: return ImportResult(
                success = false,
                message = "Character slot $slotId was not found on card ${importedCard.name}."
            )

        database.userCharacterDao().clearActiveCharacter()

        val totalBattles = character.characterStats.totalBattles
        val totalWins = character.characterStats.totalWins.coerceAtMost(totalBattles)
        val currentPhaseBattles = character.characterStats.currentPhaseBattles
        val currentPhaseWins = character.characterStats.currentPhaseWins.coerceAtMost(currentPhaseBattles)
        val normalizedTransformationCountdown = normalizeTransformationCountdownMinutes(
            transformationCountdownMinutes = secondsToMinutes(character.characterStats.timeUntilNextTransformation),
            hasPossibleTransformations = hasPossibleTransformations(cardCharacter.id),
        )

        val userCharacterId = database.userCharacterDao().insertCharacterData(
            UserCharacter(
                charId = cardCharacter.id,
                ageInDays = character.characterStats.ageInDays.takeIf { it > 0 }
                    ?: max(character.transformationHistoryCount - 1, 0),
                mood = character.characterStats.mood,
                vitalPoints = character.characterStats.vitals,
                transformationCountdown = normalizedTransformationCountdown,
                injuryStatus = resolveInjuryStatus(character.characterStats.injured),
                trophies = character.characterStats.trainedPp,
                currentPhaseBattlesWon = currentPhaseWins,
                currentPhaseBattlesLost = (currentPhaseBattles - currentPhaseWins).coerceAtLeast(0),
                totalBattlesWon = totalWins,
                totalBattlesLost = (totalBattles - totalWins).coerceAtLeast(0),
                activityLevel = character.characterStats.activityLevel.coerceAtLeast(0),
                heartRateCurrent = character.characterStats.heartRateCurrent.coerceAtLeast(0),
                characterType = forcedDeviceType,
                isActive = true
            )
        )

        runBlocking {
            database.vitalWearSettingsDao().upsert(
                VitalWearCharacterSettings(
                    characterId = userCharacterId,
                    trainingInBackground = character.settings.trainingInBackground,
                    allowedBattles = character.settings.allowedBattles.number,
                    accumulatedDailyInjuries = character.characterStats.accumulatedDailyInjuries,
                )
            )
        }

        val vbProfile = VBCharacterData(
            id = userCharacterId,
            generation = if (character.characterStats.generation > 0) {
                character.characterStats.generation
            } else {
                max(character.transformationHistoryCount - 1, 0)
            },
            totalTrophies = if (character.characterStats.totalTrophies > 0) {
                character.characterStats.totalTrophies
            } else {
                character.characterStats.trainedPp.coerceAtLeast(0)
            }
        )
        database.userCharacterDao().insertVBCharacterData(vbProfile)

        if (forcedDeviceType == DeviceType.BEDevice) {
            val abilityRarity = resolveAbilityRarity(character.characterStats.abilityRarity)
            database.userCharacterDao().insertBECharacterData(
                BECharacterData(
                    id = userCharacterId,
                    trainingHp = character.characterStats.trainedHp,
                    trainingAp = character.characterStats.trainedAp,
                    trainingBp = character.characterStats.trainedBp,
                    remainingTrainingTimeInMinutes = secondsToMinutes(character.characterStats.trainingTimeRemainingInSeconds),
                    itemEffectMentalStateValue = character.characterStats.itemEffectMentalStateValue,
                    itemEffectMentalStateMinutesRemaining = character.characterStats.itemEffectMentalStateMinutesRemaining,
                    itemEffectActivityLevelValue = character.characterStats.itemEffectActivityLevelValue,
                    itemEffectActivityLevelMinutesRemaining = character.characterStats.itemEffectActivityLevelMinutesRemaining,
                    itemEffectVitalPointsChangeValue = character.characterStats.itemEffectVitalPointsChangeValue,
                    itemEffectVitalPointsChangeMinutesRemaining = character.characterStats.itemEffectVitalPointsChangeMinutesRemaining,
                    abilityRarity = abilityRarity,
                    abilityType = character.characterStats.abilityType,
                    abilityBranch = character.characterStats.abilityBranch,
                    abilityReset = character.characterStats.abilityReset,
                    rank = character.characterStats.rank,
                    itemType = character.characterStats.itemType,
                    itemMultiplier = character.characterStats.itemMultiplier,
                    itemRemainingTime = character.characterStats.itemRemainingTime,
                    otp0 = "",
                    otp1 = "",
                    minorVersion = character.characterStats.firmwareMinorVersion,
                    majorVersion = character.characterStats.firmwareMajorVersion
                )
            )
        }

        val now = System.currentTimeMillis()
        markSeen(importedCard.name, slotId, importedCard.id, now)

        var insertedTransformationCount = 0
        for (transformation in character.transformationHistoryList) {
            val transformationCard = resolveRelatedCard(
                incomingCardName = transformation.cardName,
                incomingCardId = character.cardId,
                matchedRootCard = importedCard,
                incomingRootCardName = character.cardName,
            )
            if (transformationCard != null) {
                database.userCharacterDao().insertTransformation(
                    userCharacterId,
                    transformation.slotId,
                    transformationCard.id,
                    now
                )
                insertedTransformationCount++
                markSeen(transformationCard.name, transformation.slotId, transformationCard.id, now)
            }
        }

        if (insertedTransformationCount == 0) {
            database.userCharacterDao().insertTransformation(
                userCharacterId,
                slotId,
                importedCard.id,
                now
            )
        }

        for ((cardName, maxAdventureCompleted) in character.maxAdventureCompletedByCardMap) {
            val adventureCard = resolveRelatedCard(
                incomingCardName = cardName,
                incomingCardId = null,
                matchedRootCard = importedCard,
                incomingRootCardName = character.cardName,
            ) ?: continue
            val currentStage = (maxAdventureCompleted + 1).coerceAtLeast(0)
            database.cardProgressDao().updateCardProgress(
                currentStage = currentStage,
                cardId = adventureCard.id,
                unlocked = currentStage > adventureCard.stageCount
            )
        }

        val nextAdventureMissionStage = character.characterStats.nextAdventureMissionStage.coerceAtLeast(0)
        if (nextAdventureMissionStage > 0) {
            database.cardProgressDao().updateCardProgress(
                currentStage = nextAdventureMissionStage,
                cardId = importedCard.id,
                unlocked = nextAdventureMissionStage > importedCard.stageCount,
            )
        }

        return ImportResult(
            success = true,
            message = "Imported ${importedCard.name} slot $slotId from VitalWear."
        )
    }

    private fun resolveCard(character: Character) = resolveCard(character.cardName, character.cardId)

    private fun resolveCard(cardName: String?, cardId: Int?): Card? {
        return selectImportedCard(
            candidates = database.cardDao().getAllCards(),
            incomingCardName = cardName,
            incomingCardId = cardId,
        )
    }

    private fun resolveRelatedCard(
        incomingCardName: String?,
        incomingCardId: Int?,
        matchedRootCard: Card,
        incomingRootCardName: String,
    ): Card? {
        if (incomingCardName.isNullOrBlank()) {
            return if (incomingCardId != null && incomingCardId > 0 && matchedRootCard.cardId == incomingCardId) {
                matchedRootCard
            } else {
                null
            }
        }

        if (cardNamesMatch(incomingCardName, incomingRootCardName)) {
            return matchedRootCard
        }

        return resolveCard(incomingCardName, incomingCardId)
    }

    private fun createPlaceholderCard(character: Character): Card? {
        if (!character.hasTransferCard() || !character.hasTransferSpecies()) {
            return null
        }

        val transferCard = character.transferCard
        selectImportedCard(
            candidates = database.cardDao().getAllCards(),
            incomingCardName = transferCard.cardName,
            incomingCardId = transferCard.cardId,
        )?.let { return it }

        val transferSpecies = character.transferSpecies
        val baseName = transferCard.cardName
            .takeIf { it.isNotBlank() }
            ?: character.cardName.takeIf { it.isNotBlank() }
            ?: "Transferred Card ${transferCard.cardId}"
        val placeholderName = buildPlaceholderCardName(baseName, transferCard.cardId)
        val logoBytes = when {
            !transferSpecies.nameSprite.isEmpty -> transferSpecies.nameSprite.toByteArray()
            !transferSpecies.idle1.isEmpty -> transferSpecies.idle1.toByteArray()
            else -> byteArrayOf()
        }
        val logoWidth = when {
            transferSpecies.nameSpriteWidth > 0 -> transferSpecies.nameSpriteWidth
            transferSpecies.spriteWidth > 0 -> transferSpecies.spriteWidth
            else -> 0
        }
        val logoHeight = when {
            transferSpecies.nameSpriteHeight > 0 -> transferSpecies.nameSpriteHeight
            transferSpecies.spriteHeight > 0 -> transferSpecies.spriteHeight
            else -> 0
        }

        val insertedId = runBlocking {
            database.cardDao().insertNewCard(
                Card(
                    cardId = transferCard.cardId,
                    logo = logoBytes,
                    logoWidth = logoWidth,
                    logoHeight = logoHeight,
                    name = placeholderName,
                    stageCount = transferCard.stageCount.coerceAtLeast(0),
                    isBEm = transferCard.isBem,
                )
            )
        }
        val resolvedCard = when {
            insertedId > 0L -> database.cardDao().getCardById(insertedId)
            else -> database.cardDao().getCardByName(placeholderName)
        } ?: return null

        if (database.cardProgressDao().getCardProgressSync(resolvedCard.id) == null) {
            database.cardProgressDao().insertCardProgress(
                CardProgress(
                    cardId = resolvedCard.id,
                    currentStage = 1,
                    unlocked = false,
                )
            )
        }

        return resolvedCard
    }

    private fun createPlaceholderCharacter(card: Card, character: Character): CardCharacter? {
        if (!character.hasTransferSpecies()) {
            return null
        }

        val transferSpecies = character.transferSpecies
        database.characterDao().getCharacterByMonIndexOrNull(transferSpecies.slotId, card.id)?.let { return it }

        val spriteId = database.spriteDao().insertSprite(
            Sprite(
                spriteIdle1 = transferSpecies.idle1.toByteArray(),
                spriteIdle2 = transferSpecies.idle2.toByteArray(),
                spriteWalk1 = transferSpecies.walk1.toByteArray(),
                spriteWalk2 = transferSpecies.walk2.toByteArray(),
                spriteRun1 = transferSpecies.run1.toByteArray(),
                spriteRun2 = transferSpecies.run2.toByteArray(),
                spriteTrain1 = transferSpecies.train1.toByteArray(),
                spriteTrain2 = transferSpecies.train2.toByteArray(),
                spriteHappy = transferSpecies.win.toByteArray(),
                spriteSleep = transferSpecies.down.toByteArray(),
                spriteAttack = transferSpecies.attack.toByteArray(),
                spriteDodge = transferSpecies.dodge.toByteArray(),
                width = transferSpecies.spriteWidth,
                height = transferSpecies.spriteHeight,
            )
        )

        val attribute = enumValues<NfcCharacter.Attribute>().getOrElse(transferSpecies.attribute) {
            enumValues<NfcCharacter.Attribute>().first()
        }

        runBlocking {
            database.characterDao().insertCharacter(
                CardCharacter(
                    cardId = card.id,
                    spriteId = spriteId,
                    charaIndex = transferSpecies.slotId,
                    stage = transferSpecies.stage,
                    attribute = attribute,
                    baseHp = transferSpecies.baseHp,
                    baseBp = transferSpecies.baseBp,
                    baseAp = transferSpecies.baseAp,
                    nameSprite = transferSpecies.nameSprite.toByteArray(),
                    nameWidth = transferSpecies.nameSpriteWidth,
                    nameHeight = transferSpecies.nameSpriteHeight,
                )
            )
        }

        return database.characterDao().getCharacterByMonIndexOrNull(transferSpecies.slotId, card.id)
    }

    private fun buildPlaceholderCardName(baseName: String, cardId: Int): String {
        val trimmedBaseName = baseName.trim().ifBlank { "Transferred Card" }
        database.cardDao().getCardByName(trimmedBaseName)?.let {
            return if (cardId > 0) "$trimmedBaseName [Transfer $cardId]" else "$trimmedBaseName [Transfer]"
        }
        return trimmedBaseName
    }

    private fun secondsToMinutes(seconds: Long): Int {
        if (seconds <= 0L) {
            return 0
        }
        // Preserve non-zero timers from HCE payloads instead of flooring 1..59s to 0.
        return ((seconds + 59L) / 60L).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    private fun hasPossibleTransformations(cardCharacterId: Long): Boolean {
        return runBlocking {
            database.characterDao().getEvolutionRequirementsForCard(cardCharacterId).firstOrNull()?.isNotEmpty() == true
        }
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

    private fun resolveAbilityRarity(rawValue: Int): NfcCharacter.AbilityRarity {
        val rarities = enumValues<NfcCharacter.AbilityRarity>()
        return rarities.getOrElse(rawValue) { resolveDefaultAbilityRarity() }
    }

    private fun markSeen(cardName: String, slotId: Int, cardId: Long, timestamp: Long) {
        database.dexDao().insertCharacter(slotId, cardId, timestamp)
        transferSeenDao.markSeen(cardName, slotId, timestamp)
    }
}

internal fun selectImportedCard(
    candidates: List<Card>,
    incomingCardName: String?,
    incomingCardId: Int?,
): Card? {
    val requestedName = incomingCardName?.takeIf { it.isNotBlank() }
    val idMatches = if (incomingCardId != null && incomingCardId > 0) {
        candidates.filter { it.cardId == incomingCardId }
    } else {
        emptyList()
    }

    requestedName?.let { requestedNameValue ->
        candidates.firstOrNull {
            it.name == requestedNameValue && (incomingCardId == null || incomingCardId <= 0 || it.cardId == incomingCardId)
        }?.let { return it }

        val ignoreCaseMatches = candidates.filter {
            it.name.equals(requestedNameValue, ignoreCase = true) &&
                (incomingCardId == null || incomingCardId <= 0 || it.cardId == incomingCardId)
        }
        if (ignoreCaseMatches.size == 1) {
            return ignoreCaseMatches.single()
        }
    }

    if (idMatches.size == 1) {
        return idMatches.single()
    }

    requestedName?.let { requestedNameValue ->
        val normalizedMatches = candidates.filter {
            cardNamesMatch(it.name, requestedNameValue) &&
                (incomingCardId == null || incomingCardId <= 0 || it.cardId == incomingCardId)
        }
        if (normalizedMatches.size == 1) {
            return normalizedMatches.single()
        }
    }

    return null
}

internal fun cardNamesMatch(left: String, right: String): Boolean {
    return left.equals(right, ignoreCase = true) || left.toNormalizedCardLookupKey() == right.toNormalizedCardLookupKey()
}

private fun String.toNormalizedCardLookupKey(): String {
    return lowercase().filter { it.isLetterOrDigit() }
}

