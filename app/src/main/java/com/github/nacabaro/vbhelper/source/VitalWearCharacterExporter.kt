package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vitalwear.protos.Character
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.utils.DeviceType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking

class VitalWearCharacterExporter(
    private val database: AppDatabase
) {
    /**
     * Builds a Character proto from the stored character data.
     * Used by the HCE ISO-DEP transfer path.
     */
    suspend fun buildCharacterProto(characterId: Long): Character {
        val characterWithSprites = database.userCharacterDao().getCharacterWithSprites(characterId)
        val userCharacter = database.userCharacterDao().getCharacter(characterId)
        val vwSettings = database.vitalWearSettingsDao().getByCharacterId(characterId)
        val card = database.cardDao().getCardByCharacterIdSync(characterId)
            ?: error("Card not found for character $characterId")
        val cardProgress = database.cardProgressDao().getCardProgressSync(card.id) ?: 0
        return Character.newBuilder()
            .setCardId(card.cardId)
            .setCardName(card.name)
            .setCharacterStats(
                Character.CharacterStats.newBuilder()
                    .setSlotId(database.userCharacterDao().getCharacterInfo(characterId).charaIndex)
                    .setVitals(characterWithSprites.vitalPoints)
                    .setTrainingTimeRemainingInSeconds(resolveTrainingSeconds(characterId, userCharacter.characterType))
                    .setTimeUntilNextTransformation(characterWithSprites.transformationCountdown.toLong() * 60L)
                    .setTrainedBp(resolveTrainedBp(characterId, userCharacter.characterType))
                    .setTrainedHp(resolveTrainedHp(characterId, userCharacter.characterType))
                    .setTrainedAp(resolveTrainedAp(characterId, userCharacter.characterType))
                    .setTrainedPp(characterWithSprites.trophies)
                    .setInjured(characterWithSprites.injuryStatus.name.lowercase().contains("inj"))
                    .setAccumulatedDailyInjuries(vwSettings?.accumulatedDailyInjuries ?: 0)
                    .setTotalBattles(characterWithSprites.totalBattlesWon + characterWithSprites.totalBattlesLost)
                    .setCurrentPhaseBattles(characterWithSprites.currentPhaseBattlesWon + characterWithSprites.currentPhaseBattlesLost)
                    .setTotalWins(characterWithSprites.totalBattlesWon)
                    .setCurrentPhaseWins(characterWithSprites.currentPhaseBattlesWon)
                    .setMood(characterWithSprites.mood)
                    .build()
            )
            .setSettings(
                Character.Settings.newBuilder()
                    .setTrainingInBackground(vwSettings?.trainingInBackground ?: false)
                    .setAllowedBattles(resolveAllowedBattles(vwSettings?.allowedBattles))
                    .build()
            )
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
            .build()
    }

    private fun resolveTrainingSeconds(characterId: Long, deviceType: DeviceType): Long {
        if (deviceType != DeviceType.BEDevice) return 0L
        return database.userCharacterDao().getBeData(characterId).valueOrNull()?.remainingTrainingTimeInMinutes?.toLong()?.times(60L) ?: 0L
    }

    private fun resolveTrainedBp(characterId: Long, deviceType: DeviceType): Int {
        return if (deviceType == DeviceType.BEDevice) {
            database.userCharacterDao().getBeData(characterId).valueOrNull()?.trainingBp ?: 0
        } else 0
    }

    private fun resolveTrainedHp(characterId: Long, deviceType: DeviceType): Int {
        return if (deviceType == DeviceType.BEDevice) {
            database.userCharacterDao().getBeData(characterId).valueOrNull()?.trainingHp ?: 0
        } else 0
    }

    private fun resolveTrainedAp(characterId: Long, deviceType: DeviceType): Int {
        return if (deviceType == DeviceType.BEDevice) {
            database.userCharacterDao().getBeData(characterId).valueOrNull()?.trainingAp ?: 0
        } else 0
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.valueOrNull(): T? {
        return runBlocking { firstOrNull() }
    }

    private fun resolveAllowedBattles(rawValue: Int?): Character.Settings.AllowedBattles {
        if (rawValue == null) return Character.Settings.AllowedBattles.CARD_ONLY
        return Character.Settings.AllowedBattles.forNumber(rawValue)
            ?: Character.Settings.AllowedBattles.CARD_ONLY
    }

}
