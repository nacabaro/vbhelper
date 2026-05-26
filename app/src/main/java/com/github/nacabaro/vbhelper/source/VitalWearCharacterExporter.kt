package com.github.nacabaro.vbhelper.source

import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.github.cfogrady.vitalwear.protos.Character
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.utils.DeviceType
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.io.File

class VitalWearCharacterExporter(
    private val context: Context,
    private val database: AppDatabase
) {
    fun buildShareIntent(characterId: Long): Intent {
        return runBlocking {
            val characterWithSprites = database.userCharacterDao().getCharacterWithSprites(characterId)
            val userCharacter = database.userCharacterDao().getCharacter(characterId)
            val card = database.cardDao().getCardByCharacterIdSync(characterId)
                ?: error("Card not found for character $characterId")
            val cardProgress = database.cardProgressDao().getCardProgressSync(card.id) ?: 0
            val proto = Character.newBuilder()
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
                        .setAccumulatedDailyInjuries(0)
                        .setTotalBattles(characterWithSprites.totalBattlesWon + characterWithSprites.totalBattlesLost)
                        .setCurrentPhaseBattles(characterWithSprites.currentPhaseBattlesWon + characterWithSprites.currentPhaseBattlesLost)
                        .setTotalWins(characterWithSprites.totalBattlesWon)
                        .setCurrentPhaseWins(characterWithSprites.currentPhaseBattlesWon)
                        .setMood(characterWithSprites.mood)
                        .build()
                )
                .setSettings(
                    Character.Settings.newBuilder()
                        .setTrainingInBackground(false)
                        .setAllowedBattles(Character.Settings.AllowedBattles.CARD_ONLY)
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

            val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
            val exportFile = File(exportDir, "vbhelper_character_$characterId.vitalwear")
            exportFile.writeBytes(proto.toByteArray())
            val exportUri = FileProvider.getUriForFile(context, "${context.packageName}.provider", exportFile)

            Intent(Intent.ACTION_SEND).apply {
                `package` = "com.github.cfogrady.vitalwear"
                type = VITALWEAR_CHARACTER_MIME
                putExtra(Intent.EXTRA_STREAM, exportUri)
                clipData = ClipData.newRawUri("", exportUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }
    }

    private fun resolveTrainingSeconds(characterId: Long, deviceType: DeviceType): Long {
        if (deviceType != DeviceType.BEDevice) {
            return 0L
        }
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
        return runBlocking {
            firstOrNull()
        }
    }

    companion object {
        const val VITALWEAR_CHARACTER_MIME = "application/x-vitalwear-character"
    }
}
