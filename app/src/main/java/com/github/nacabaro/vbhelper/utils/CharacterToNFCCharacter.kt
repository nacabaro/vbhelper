package com.github.nacabaro.vbhelper.utils

import android.content.Context
import android.icu.util.Calendar
import android.icu.util.GregorianCalendar
import com.github.cfogrady.vbnfc.be.BENfcCharacter
import com.github.cfogrady.vbnfc.be.FirmwareVersion
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.DeviceType
import com.github.nacabaro.vbhelper.source.StorageRepository
import java.util.Date

suspend fun characterToNfc(context: Context, characterId: Long): NfcCharacter? {
    val app = context.applicationContext as VBHelper
    val database = app.container.db
    val storageRepository = StorageRepository(database)
    val userCharacter = storageRepository.getSingleCharacter(characterId)
    val characterInfo = storageRepository.getCharacterData(characterId)

    if (userCharacter.characterType == DeviceType.BEDevice) {
        val beData = storageRepository.getCharacterBeData(characterId)
        val transformationHistory = storageRepository
            .getTransformationHistory(characterId)!!
            .map {
                val date = Date(it.transformationDate)
                val calendar = GregorianCalendar()
                calendar.time = date

                NfcCharacter.Transformation(
                    toCharIndex = it.monIndex.toUByte(),
                    year = calendar
                        .get(Calendar.YEAR)
                        .toUShort(),
                    month = calendar
                        .get(Calendar.MONTH)
                        .toUByte(),
                    day = calendar
                        .get(Calendar.DAY_OF_MONTH)
                        .toUByte()
                )
            }.toTypedArray()

        val paddedTransformationArray = padTransformationArray(transformationHistory)

        val nfcData = BENfcCharacter(
            dimId = characterInfo.cardId.toUShort(),
            charIndex = characterInfo.charId.toUShort(),
            stage = userCharacter.stage.toByte(),
            attribute = userCharacter.attribute,
            ageInDays = userCharacter.ageInDays.toByte(),
            nextAdventureMissionStage = userCharacter.nextAdventureMissionStage.toByte(),
            mood = userCharacter.mood.toByte(),
            vitalPoints = userCharacter.vitalPoints.toUShort(),
            itemEffectMentalStateValue = beData.itemEffectMentalStateValue.toByte(),
            itemEffectMentalStateMinutesRemaining = beData.itemEffectMentalStateMinutesRemaining.toByte(),
            itemEffectActivityLevelValue = beData.itemEffectActivityLevelValue.toByte(),
            itemEffectActivityLevelMinutesRemaining = beData.itemEffectActivityLevelMinutesRemaining.toByte(),
            itemEffectVitalPointsChangeValue = beData.itemEffectVitalPointsChangeValue.toByte(),
            itemEffectVitalPointsChangeMinutesRemaining = beData.itemEffectVitalPointsChangeMinutesRemaining.toByte(),
            transformationCountdownInMinutes = userCharacter.transformationCountdown.toUShort(),
            injuryStatus = userCharacter.injuryStatus,
            trainingPp = userCharacter.trophies.toUShort(),
            currentPhaseBattlesWon = userCharacter.currentPhaseBattlesWon.toUShort(),
            currentPhaseBattlesLost = userCharacter.currentPhaseBattlesLost.toUShort(),
            totalBattlesWon = userCharacter.totalBattlesWon.toUShort(),
            totalBattlesLost = userCharacter.totalBattlesLost.toUShort(),
            activityLevel = userCharacter.activityLevel.toByte(),
            heartRateCurrent = userCharacter.heartRateCurrent.toUByte(),
            transformationHistory = paddedTransformationArray,
            vitalHistory = Array(7) {
                NfcCharacter.DailyVitals(0u, 0u, 0u, 0u)
            },
            appReserved1 = ByteArray(12) {0},
            appReserved2 = Array(3) {0u},
            trainingHp = beData.trainingHp.toUShort(),
            trainingAp = beData.trainingAp.toUShort(),
            trainingBp = beData.trainingBp.toUShort(),
            remainingTrainingTimeInMinutes = beData.remainingTrainingTimeInMinutes.toUShort(),
            abilityRarity = beData.abilityRarity,
            abilityType = beData.abilityType.toUShort(),
            abilityBranch = beData.abilityBranch.toUShort(),
            abilityReset = beData.abilityReset.toByte(),
            rank = beData.rank.toByte(),
            itemType = beData.itemType.toByte(),
            itemMultiplier = beData.itemMultiplier.toByte(),
            itemRemainingTime = beData.itemRemainingTime.toByte(),
            otp0 = byteArrayOf(8),
            otp1 = byteArrayOf(8),
            characterCreationFirmwareVersion = FirmwareVersion(
                minorVersion = beData.minorVersion.toByte(),
                majorVersion = beData.majorVersion.toByte()
            )
        )

        return nfcData
    }

    return null
}