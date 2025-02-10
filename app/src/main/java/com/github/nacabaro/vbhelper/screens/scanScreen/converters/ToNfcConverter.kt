package com.github.nacabaro.vbhelper.screens.scanScreen.converters

import android.icu.util.Calendar
import androidx.activity.ComponentActivity
import com.github.cfogrady.vbnfc.be.BENfcCharacter
import com.github.cfogrady.vbnfc.be.FirmwareVersion
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vbnfc.vb.SpecialMission
import com.github.cfogrady.vbnfc.vb.VBNfcCharacter
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.utils.DeviceType
import java.util.Date

class ToNfcConverter(
    private val componentActivity: ComponentActivity
) {
    private val application: VBHelper = componentActivity.applicationContext as VBHelper
    private val database: AppDatabase = application.container.db

    suspend fun characterToNfc(characterId: Long): NfcCharacter {
        val app = componentActivity.applicationContext as VBHelper
        val database = app.container.db

        val userCharacter = database
            .userCharacterDao()
            .getCharacter(characterId)

        val characterInfo = database
            .characterDao()
            .getCharacterInfo(userCharacter.charId)

        val currentCardStage = database.dimDao().getCurrentStage(characterInfo.cardId)

        return if (userCharacter.characterType == DeviceType.BEDevice)
            nfcToBENfc(characterId, characterInfo, currentCardStage, userCharacter)
        else
            nfcToVBNfc(characterId, characterInfo, currentCardStage, userCharacter)
    }

    private suspend fun nfcToVBNfc(
        characterId: Long,
        characterInfo: CharacterDtos.DiMInfo,
        currentCardStage: Int,
        userCharacter: UserCharacter
    ): VBNfcCharacter {
        val vbData = database
            .userCharacterDao()
            .getVbData(characterId)

        val specialMissions = database
            .userCharacterDao()
            .getSpecialMissions(characterId)

        val paddedTransformationArray = generateTransformationHistory(characterId)

        val watchSpecialMissions = specialMissions.map {
            SpecialMission(
                goal = it.goal.toUShort(),
                id = it.watchId.toUShort(),
                progress = it.progress.toUShort(),
                status = it.status,
                timeElapsedInMinutes = it.timeElapsedInMinutes.toUShort(),
                timeLimitInMinutes = it.timeLimitInMinutes.toUShort(),
                type = it.missionType
            )
        }

        val nfcData = VBNfcCharacter(
            dimId = characterInfo.cardId.toUShort(),
            charIndex = characterInfo.charId.toUShort(),
            stage = userCharacter.stage.toByte(),
            attribute = userCharacter.attribute,
            ageInDays = userCharacter.ageInDays.toByte(),
            nextAdventureMissionStage = currentCardStage.toByte(),
            mood = userCharacter.mood.toByte(),
            vitalPoints = userCharacter.vitalPoints.toUShort(),
            transformationCountdownInMinutes = userCharacter.transformationCountdown.toUShort(),
            injuryStatus = userCharacter.injuryStatus,
            trophies = userCharacter.trophies.toUShort(),
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
            generation = vbData.generation.toUShort(),
            totalTrophies = vbData.totalTrophies.toUShort(),
            specialMissions = watchSpecialMissions.toTypedArray()
        )

        return nfcData
    }

    private suspend fun nfcToBENfc(
        characterId: Long,
        characterInfo: CharacterDtos.DiMInfo,
        currentCardStage: Int,
        userCharacter: UserCharacter
    ): BENfcCharacter {
        val beData = database
            .userCharacterDao()
            .getBeData(characterId)

        val paddedTransformationArray = generateTransformationHistory(characterId)

        val nfcData = BENfcCharacter(
            dimId = characterInfo.cardId.toUShort(),
            charIndex = characterInfo.charId.toUShort(),
            stage = userCharacter.stage.toByte(),
            attribute = userCharacter.attribute,
            ageInDays = userCharacter.ageInDays.toByte(),
            nextAdventureMissionStage = currentCardStage.toByte(),
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

    private suspend fun generateTransformationHistory(
        characterId: Long
    ): Array<NfcCharacter.Transformation> {
        val transformationHistory = database
            .userCharacterDao()
            .getTransformationHistory(characterId)!!
            .map {
                val date = Date(it.transformationDate)
                val calendar = android.icu.util.GregorianCalendar()
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

        return paddedTransformationArray
    }

    private fun padTransformationArray(
        transformationArray: Array<NfcCharacter.Transformation>
    ): Array<NfcCharacter.Transformation> {
        if (transformationArray.size >= 8) {
            return transformationArray
        }

        val paddedArray = Array(8) {
            NfcCharacter.Transformation(
                toCharIndex = 255u,
                year = 65535u,
                month = 255u,
                day = 255u
            )
        }

        System.arraycopy(transformationArray, 0, paddedArray, 0, transformationArray.size)
        return paddedArray
    }
}