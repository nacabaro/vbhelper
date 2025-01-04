package com.github.cfogrady.vbnfc.be

import android.util.Log
import com.github.cfogrady.vbnfc.ChecksumCalculator
import com.github.cfogrady.vbnfc.CryptographicTransformer
import com.github.cfogrady.vbnfc.NfcDataTranslator
import com.github.cfogrady.vbnfc.TagCommunicator
import com.github.cfogrady.vbnfc.copyIntoUShortArray
import com.github.cfogrady.vbnfc.data.DeviceSubType
import com.github.cfogrady.vbnfc.data.DeviceType
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vbnfc.data.NfcHeader
import com.github.cfogrady.vbnfc.getUInt16
import com.github.cfogrady.vbnfc.toByteArray
import java.nio.ByteOrder

class BENfcDataTranslator(
    override val cryptographicTransformer: CryptographicTransformer,
    private val checksumCalculator: ChecksumCalculator = ChecksumCalculator()
): NfcDataTranslator {

    companion object {

        const val OPERATION_PAGE: Byte = 0x6

        // CHARACTER
        const val APP_RESERVED_START = 0
        const val APP_RESERVED_SIZE = 12
        const val INJURY_STATUS_IDX = 64
        const val APP_RESERVED_2_START = 66
        const val APP_RESERVED_2_SIZE = 3 //3 ushorts
        const val CHARACTER_INDEX_IDX = 72
        const val DIM_ID_IDX = 74
        const val PHASE_IDX = 76
        const val ATTRIBUTE_IDX = 77
        const val AGE_IN_DAYS_IDX = 78 // always 0 on BE :(
        const val TRAINING_PP_IDX = 96
        const val CURRENT_BATTLES_WON_IDX = 98
        const val CURRENT_BATTLES_LOST_IDX = 100
        const val TOTAL_BATTLES_WON_IDX = 102
        const val TOTAL_BATTLES_LOST_IDX = 104
        const val WIN_PCT_IDX = 106 // unused
        const val CHARACTER_CREATION_FIRMWARE_VERSION_IDX = 108
        const val NEXT_ADVENTURE_MISSION_STAGE_IDX = 128
        const val MOOD_IDX = 129
        const val ACTIVITY_LEVEL_IDX = 130
        const val HEART_RATE_CURRENT_IDX = 131
        const val VITAL_POINTS_IDX = 132
        const val ITEM_EFFECT_MENTAL_STATE_VALUE_IDX = 134
        const val ITEM_EFFECT_MENTAL_STATE_MINUTES_REMAINING_IDX = 135
        const val ITEM_EFFECT_ACTIVITY_LEVEL_VALUE_IDX = 136
        const val ITEM_EFFECT_ACTIVITY_LEVEL_MINUTES_REMAINING_IDX = 137
        const val ITEM_EFFECT_VITAL_POINTS_CHANGE_VALUE_IDX = 138
        const val ITEM_EFFECT_VITAL_POINTS_CHANGE_MINUTES_REMAINING_IDX = 139
        // 140 reserved
        const val TRANSFORMATION_COUNT_DOWN_IDX = 141
        const val TRANSFORMATION_HISTORY_START = 208
        const val TRAINING_HP_IDX = 256
        const val TRAINING_AP_IDX = 258
        const val TRAINING_BP_IDX = 260
        const val TRAINING_TIME_IDX = 266
        const val RANK_IDX = 288
        const val ABILITY_RARITY_IDX = 290
        const val ABILITY_TYPE_IDX = 292
        const val ABILITY_BRANCH_IDX = 294
        const val ABILITY_RESET_IDX = 296
        const val ITEM_TYPE_IDX = 299
        const val ITEM_MULTIPLIER_IDX = 300
        const val ITEM_REMAINING_TIME_IDX = 301
        const val OTP_START_IDX = 352
        const val OTP_END_IDX = 359
        const val OTP2_START_IDX = 368
        const val OTP2_END_IDX = 375

        // DEVICE
        const val VITAL_POINTS_CURRENT_IDX = 160
        const val FIMRWARE_VERSION_IDX = 380


    }

    // setCharacterInByteArray takes the BENfcCharacter and modifies the byte array with character
    // data. At the time of writing this is used to write a parsed character into fresh unparsed
    // device data when sending a character back to the device.
    override fun setCharacterInByteArray(
        character: NfcCharacter,
        bytes: ByteArray
    ) {
        val beCharacter = character as BENfcCharacter
        beCharacter.appReserved1.copyInto(bytes,
            APP_RESERVED_START, 0,
            APP_RESERVED_SIZE
        )
        beCharacter.injuryStatus.ordinal.toUShort().toByteArray(bytes,
            INJURY_STATUS_IDX, ByteOrder.BIG_ENDIAN)
        for(i in 0..<APP_RESERVED_2_SIZE) {
            val index = APP_RESERVED_2_START + 2*i
            beCharacter.appReserved2[i].toByteArray(bytes, index, ByteOrder.BIG_ENDIAN)
        }
        beCharacter.charIndex.toByteArray(bytes, CHARACTER_INDEX_IDX, ByteOrder.BIG_ENDIAN)
        beCharacter.dimId.toByteArray(bytes, DIM_ID_IDX, ByteOrder.BIG_ENDIAN)
        bytes[PHASE_IDX] = beCharacter.stage
        bytes[ATTRIBUTE_IDX] = beCharacter.attribute.ordinal.toByte()
        bytes[AGE_IN_DAYS_IDX] = beCharacter.ageInDays
        beCharacter.getTrainingPp().toByteArray(bytes, TRAINING_PP_IDX, ByteOrder.BIG_ENDIAN)
        beCharacter.currentPhaseBattlesWon.toByteArray(bytes,
            CURRENT_BATTLES_WON_IDX, ByteOrder.BIG_ENDIAN)
        beCharacter.currentPhaseBattlesLost.toByteArray(bytes,
            CURRENT_BATTLES_LOST_IDX, ByteOrder.BIG_ENDIAN)
        beCharacter.totalBattlesWon.toByteArray(bytes,
            TOTAL_BATTLES_WON_IDX, ByteOrder.BIG_ENDIAN)
        beCharacter.totalBattlesLost.toByteArray(bytes,
            TOTAL_BATTLES_LOST_IDX, ByteOrder.BIG_ENDIAN)
        bytes[WIN_PCT_IDX] = beCharacter.getWinPercentage()
        bytes[NEXT_ADVENTURE_MISSION_STAGE_IDX] = beCharacter.nextAdventureMissionStage
        bytes[MOOD_IDX] = beCharacter.mood
        bytes[ACTIVITY_LEVEL_IDX] = beCharacter.activityLevel
        bytes[HEART_RATE_CURRENT_IDX] = beCharacter.heartRateCurrent.toByte()
        beCharacter.vitalPoints.toByteArray(bytes, VITAL_POINTS_IDX, ByteOrder.BIG_ENDIAN)
        bytes[ITEM_EFFECT_MENTAL_STATE_VALUE_IDX] = beCharacter.itemEffectMentalStateValue
        bytes[ITEM_EFFECT_MENTAL_STATE_MINUTES_REMAINING_IDX] = beCharacter.itemEffectMentalStateMinutesRemaining
        bytes[ITEM_EFFECT_ACTIVITY_LEVEL_VALUE_IDX] = beCharacter.itemEffectActivityLevelValue
        bytes[ITEM_EFFECT_ACTIVITY_LEVEL_MINUTES_REMAINING_IDX] = beCharacter.itemEffectActivityLevelMinutesRemaining
        bytes[ITEM_EFFECT_VITAL_POINTS_CHANGE_VALUE_IDX] = beCharacter.itemEffectVitalPointsChangeValue
        bytes[ITEM_EFFECT_VITAL_POINTS_CHANGE_MINUTES_REMAINING_IDX] = beCharacter.itemEffectVitalPointsChangeMinutesRemaining
        beCharacter.transformationCountdown.toByteArray(bytes,
            TRANSFORMATION_COUNT_DOWN_IDX, ByteOrder.BIG_ENDIAN)
        transformationHistoryToByteArray(beCharacter.transformationHistory, bytes)
        beCharacter.trainingHp.toByteArray(bytes, TRAINING_HP_IDX, ByteOrder.BIG_ENDIAN)
        beCharacter.trainingAp.toByteArray(bytes, TRAINING_AP_IDX, ByteOrder.BIG_ENDIAN)
        beCharacter.trainingBp.toByteArray(bytes, TRAINING_BP_IDX, ByteOrder.BIG_ENDIAN)
        beCharacter.remainingTrainingTimeInMinutes.toByteArray(bytes,
            TRAINING_TIME_IDX, ByteOrder.BIG_ENDIAN)
        bytes[ABILITY_RARITY_IDX] = beCharacter.abilityRarity.ordinal.toByte()
        beCharacter.abilityType.toByteArray(bytes, ABILITY_TYPE_IDX, ByteOrder.BIG_ENDIAN)
        beCharacter.abilityBranch.toByteArray(bytes, ABILITY_BRANCH_IDX, ByteOrder.BIG_ENDIAN)
        bytes[ABILITY_RESET_IDX] = beCharacter.abilityReset
        bytes[RANK_IDX] = beCharacter.rank
        bytes[ITEM_TYPE_IDX] = beCharacter.itemType
        bytes[ITEM_MULTIPLIER_IDX] = beCharacter.itemMultiplier
        bytes[ITEM_REMAINING_TIME_IDX] = beCharacter.itemRemainingTime
        beCharacter.otp0.copyInto(bytes, OTP_START_IDX, 0, beCharacter.otp0.size)
        beCharacter.otp1.copyInto(bytes, OTP2_START_IDX, 0, beCharacter.otp1.size)
        bytes[CHARACTER_CREATION_FIRMWARE_VERSION_IDX] = beCharacter.characterCreationFirmwareVersion.majorVersion
        bytes[CHARACTER_CREATION_FIRMWARE_VERSION_IDX+1] = beCharacter.characterCreationFirmwareVersion.minorVersion
    }

    // finalizeByteArrayFormat finalizes the byte array for BE NFC format by setting all the
    // checksums, and duplicating the duplicate memory pages.
    override fun finalizeByteArrayFormat(bytes: ByteArray) {
        checksumCalculator.recalculateChecksums(bytes)
        performPageBlockDuplications(bytes)
    }

    override fun getOperationCommandBytes(header: NfcHeader, operation: Byte): ByteArray {
        return byteArrayOf(TagCommunicator.NFC_WRITE_COMMAND, OPERATION_PAGE, header.status, operation, header.dimIdBytes[0], header.dimIdBytes[1])
    }

    // parses a BENfcCharacter from a ByteArray produced by the TagCommunicator
    override fun parseNfcCharacter(bytes: ByteArray): BENfcCharacter {
        return BENfcCharacter(
            appReserved1 = bytes.sliceArray(APP_RESERVED_START..<(APP_RESERVED_START + APP_RESERVED_SIZE)),
            injuryStatus = NfcCharacter.InjuryStatus.entries[bytes.getUInt16(INJURY_STATUS_IDX, ByteOrder.BIG_ENDIAN).toInt()],
            appReserved2 = bytes.copyIntoUShortArray(APP_RESERVED_2_START, APP_RESERVED_2_SIZE),
            charIndex = bytes.getUInt16(CHARACTER_INDEX_IDX, ByteOrder.BIG_ENDIAN),
            dimId = bytes.getUInt16(DIM_ID_IDX, ByteOrder.BIG_ENDIAN),
            stage = bytes[PHASE_IDX],
            attribute = NfcCharacter.Attribute.entries[bytes[ATTRIBUTE_IDX].toInt()],
            ageInDays = bytes[AGE_IN_DAYS_IDX],
            trainingPp = bytes.getUInt16(TRAINING_PP_IDX, ByteOrder.BIG_ENDIAN),
            currentPhaseBattlesWon = bytes.getUInt16(CURRENT_BATTLES_WON_IDX, ByteOrder.BIG_ENDIAN),
            currentPhaseBattlesLost = bytes.getUInt16(CURRENT_BATTLES_LOST_IDX, ByteOrder.BIG_ENDIAN),
            totalBattlesWon = bytes.getUInt16(TOTAL_BATTLES_WON_IDX, ByteOrder.BIG_ENDIAN),
            totalBattlesLost = bytes.getUInt16(TOTAL_BATTLES_LOST_IDX, ByteOrder.BIG_ENDIAN),
            nextAdventureMissionStage = bytes[NEXT_ADVENTURE_MISSION_STAGE_IDX],
            mood = bytes[MOOD_IDX],
            activityLevel = bytes[ACTIVITY_LEVEL_IDX],
            heartRateCurrent = bytes[HEART_RATE_CURRENT_IDX].toUByte(),
            vitalPoints = bytes.getUInt16(VITAL_POINTS_IDX, ByteOrder.BIG_ENDIAN),
            itemEffectMentalStateValue = bytes[ITEM_EFFECT_MENTAL_STATE_VALUE_IDX],
            itemEffectMentalStateMinutesRemaining = bytes[ITEM_EFFECT_MENTAL_STATE_MINUTES_REMAINING_IDX],
            itemEffectActivityLevelValue = bytes[ITEM_EFFECT_ACTIVITY_LEVEL_VALUE_IDX],
            itemEffectActivityLevelMinutesRemaining = bytes[ITEM_EFFECT_ACTIVITY_LEVEL_MINUTES_REMAINING_IDX],
            itemEffectVitalPointsChangeValue = bytes[ITEM_EFFECT_VITAL_POINTS_CHANGE_VALUE_IDX],
            itemEffectVitalPointsChangeMinutesRemaining = bytes[ITEM_EFFECT_VITAL_POINTS_CHANGE_MINUTES_REMAINING_IDX],
            transformationCountdownInMinutes = bytes.getUInt16(TRANSFORMATION_COUNT_DOWN_IDX, ByteOrder.BIG_ENDIAN),
            transformationHistory = buildTransformationHistory(bytes),
            trainingHp = bytes.getUInt16(TRAINING_HP_IDX, ByteOrder.BIG_ENDIAN),
            trainingAp = bytes.getUInt16(TRAINING_AP_IDX, ByteOrder.BIG_ENDIAN),
            trainingBp = bytes.getUInt16(TRAINING_BP_IDX, ByteOrder.BIG_ENDIAN),
            remainingTrainingTimeInMinutes = bytes.getUInt16(TRAINING_TIME_IDX, ByteOrder.BIG_ENDIAN),
            abilityRarity = NfcCharacter.AbilityRarity.entries[bytes[ABILITY_RARITY_IDX].toInt()],
            abilityType = bytes.getUInt16(ABILITY_TYPE_IDX, ByteOrder.BIG_ENDIAN),
            abilityBranch = bytes.getUInt16(ABILITY_BRANCH_IDX, ByteOrder.BIG_ENDIAN),
            abilityReset = bytes[ABILITY_RESET_IDX],
            rank = bytes[RANK_IDX],
            itemType = bytes[ITEM_TYPE_IDX],
            itemMultiplier = bytes[ITEM_MULTIPLIER_IDX],
            itemRemainingTime = bytes[ITEM_REMAINING_TIME_IDX],
            otp0 = bytes.sliceArray(OTP_START_IDX..OTP_END_IDX),
            otp1 = bytes.sliceArray(OTP2_START_IDX..OTP2_END_IDX),
            characterCreationFirmwareVersion = FirmwareVersion(
                majorVersion = bytes[CHARACTER_CREATION_FIRMWARE_VERSION_IDX],
                minorVersion = bytes[CHARACTER_CREATION_FIRMWARE_VERSION_IDX+1]),
        )
    }

    override fun parseHeader(headerBytes: ByteArray): NfcHeader {
        Log.i(TagCommunicator.TAG, "Bytes in header: ${headerBytes.size}")
        val header = NfcHeader(
            deviceId = DeviceType.VitalBraceletBEDeviceType,
            deviceSubType = DeviceSubType.Original,
            vbCompatibleTagIdentifier = headerBytes.sliceArray(0..3), // this is a magic number used to verify that the tag is a VB.
            status = headerBytes[8],
            operation = headerBytes[9],
            dimIdBytes = headerBytes.sliceArray(10..11),
            appFlag = headerBytes[12],
            nonce = headerBytes.sliceArray(13..15)
        )
        Log.i(TagCommunicator.TAG, "Header: $header")
        return header
    }

    // a block being 4 pages
    private val firstIndicesOfBlocksToCopy = intArrayOf(32, 64, 96, 128, 256, 416)
    private fun performPageBlockDuplications(data: ByteArray) {
        for (firstIndex in firstIndicesOfBlocksToCopy) {
            for (i in firstIndex..firstIndex + 15) {
                data[i+16] = data[i]
            }
        }
    }

    private fun transformationHistoryToByteArray(transformationHistory: Array<NfcCharacter.Transformation>, bytes: ByteArray) {
        if (transformationHistory.size != 8) {
            throw IllegalArgumentException("Transformation History must be exactly size 8")
        }
        for (phase in 0..<transformationHistory.size) {
            var rootIdx = phase*4 + TRANSFORMATION_HISTORY_START
            if (phase > 2) {
                rootIdx += 4 // we skip 220-223 for some reason
            }
            if (phase > 5) {
                rootIdx += 4 // we skip 236-239 for some reason
            }
            bytes[rootIdx] = transformationHistory[phase].toCharIndex
            bytes[rootIdx+1] = transformationHistory[phase].yearsSince1988
            bytes[rootIdx+2] = transformationHistory[phase].month
            bytes[rootIdx+3] = transformationHistory[phase].day
        }
    }

    private fun buildTransformationHistory(data: ByteArray): Array<NfcCharacter.Transformation> {
        val transformationHistory = Array<NfcCharacter.Transformation>(8) { phase ->
            var rootIdx = phase*4 + TRANSFORMATION_HISTORY_START
            if (phase > 2) {
                rootIdx += 4 // we skip 220-223 for some reason
            }
            if (phase > 5) {
                rootIdx += 4 // we skip 236-239 for some reason
            }
            NfcCharacter.Transformation(
                toCharIndex = data[rootIdx],
                yearsSince1988 = data[rootIdx+1],
                month = data[rootIdx+2],
                day = data[rootIdx+3]
            )
        }
        return transformationHistory
    }
}