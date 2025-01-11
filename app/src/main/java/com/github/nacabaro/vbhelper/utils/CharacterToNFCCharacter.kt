package com.github.nacabaro.vbhelper.utils

import android.content.Context
import com.github.cfogrady.vbnfc.be.BENfcCharacter
import com.github.cfogrady.vbnfc.be.FirmwareVersion
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.DeviceType
import com.github.nacabaro.vbhelper.source.StorageRepository

suspend fun characterToNfc(context: Context, characterId: Long): NfcCharacter? {
    val app = context.applicationContext as VBHelper
    val database = app.container.db
    val storageRepository = StorageRepository(database)
    val userCharacter = storageRepository.getSingleCharacter(characterId)
    val characterInfo = storageRepository.getCharacterData(characterId)

    if (userCharacter.characterType == DeviceType.BEDevice) {
        val beData = storageRepository.getCharacterBeData(characterId)
        val transformationHistory = storageRepository
            .getTransformationHistory(characterId)
            .map {
                NfcCharacter.Transformation(
                    toCharIndex = it.toCharIndex.toUByte(),
                    year = it.year.toUShort(),
                    month = it.month.toUByte(),
                    day = it.day.toUByte()
                )
            }.toTypedArray()

        // Maybe this is the issue?
        val dummyVitalHistory = arrayOf<NfcCharacter.DailyVitals>()

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
            transformationHistory = transformationHistory,
            vitalHistory = arrayOf(),
            appReserved1 = byteArrayOf(),
            appReserved2 = Array(2, { 0u }),
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