package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vitalwear.common.data.SharedTransferSeenDao
import com.github.cfogrady.vitalwear.protos.Character
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.domain.device_data.VitalWearCharacterSettings
import com.github.nacabaro.vbhelper.utils.DeviceType
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
        val deviceType = if (importedCard.isBEm) DeviceType.BEDevice else DeviceType.VBDevice

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

        if (importedCard.isBEm) {
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
                    generation = max(character.transformationHistoryCount - 1, 0),
                    totalTrophies = character.characterStats.trainedPp.coerceAtLeast(0)
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

