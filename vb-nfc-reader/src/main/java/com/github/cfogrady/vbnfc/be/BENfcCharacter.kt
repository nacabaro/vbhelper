package com.github.cfogrady.vbnfc.be

import com.github.cfogrady.vbnfc.data.NfcCharacter
import java.util.Arrays
import java.util.Objects

class BENfcCharacter(
    dimId: UShort,
    charIndex: UShort,
    stage: Byte,
    attribute: Attribute,
    ageInDays: Byte,
    nextAdventureMissionStage: Byte,
    mood: Byte,
    vitalPoints: UShort,
    transformationCountdownInMinutes: UShort,
    injuryStatus: InjuryStatus,
    trainingPp: UShort,
    currentPhaseBattlesWon: UShort,
    currentPhaseBattlesLost: UShort,
    totalBattlesWon: UShort,
    totalBattlesLost: UShort,
    activityLevel: Byte,
    heartRateCurrent: UByte,
    transformationHistory: Array<Transformation>,
    var trainingHp: UShort,
    var trainingAp: UShort,
    var trainingBp: UShort,
    var remainingTrainingTimeInMinutes: UShort,
    var itemEffectMentalStateValue: Byte,
    var itemEffectMentalStateMinutesRemaining: Byte,
    var itemEffectActivityLevelValue: Byte,
    var itemEffectActivityLevelMinutesRemaining: Byte,
    var itemEffectVitalPointsChangeValue: Byte,
    var itemEffectVitalPointsChangeMinutesRemaining: Byte,
    var abilityRarity: AbilityRarity,
    var abilityType: UShort,
    var abilityBranch: UShort,
    var abilityReset: Byte,
    var rank: Byte,
    var itemType: Byte,
    var itemMultiplier: Byte,
    var itemRemainingTime: Byte,
    internal val otp0: ByteArray, // OTP matches the character to the dim
    internal val otp1: ByteArray, // OTP matches the character to the dim
    var characterCreationFirmwareVersion: FirmwareVersion,
    var appReserved1: ByteArray, // this is a 12 byte array reserved for new app features, a custom app should be able to safely use this for custom features
    var appReserved2: Array<UShort>, // this is a 3 element array reserved for new app features, a custom app should be able to safely use this for custom features
) :
    NfcCharacter(
        dimId = dimId,
        charIndex = charIndex,
        stage = stage,
        attribute = attribute,
        ageInDays = ageInDays,
        nextAdventureMissionStage = nextAdventureMissionStage,
        mood = mood,
        vitalPoints = vitalPoints,
        transformationCountdown = transformationCountdownInMinutes,
        injuryStatus = injuryStatus,
        trophies = trainingPp,
        currentPhaseBattlesWon = currentPhaseBattlesWon,
        currentPhaseBattlesLost = currentPhaseBattlesLost,
        totalBattlesWon = totalBattlesWon,
        totalBattlesLost = totalBattlesLost,
        activityLevel = activityLevel,
        heartRateCurrent = heartRateCurrent,
        transformationHistory = transformationHistory,
    )
{
    fun getTrainingPp(): UShort {
        return trophies
    }

    fun setTrainingPp(trainingPp: UShort) {
        trophies = trainingPp
    }

    fun getWinPercentage(): Byte {
        val totalBatles = currentPhaseBattlesWon + currentPhaseBattlesLost
        if (totalBatles == 0u) {
            return 0
        }
        return ((100u * currentPhaseBattlesWon) / totalBatles).toByte()
    }



    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BENfcCharacter
        if(!super.equals(other)) return false

        if (trainingHp != other.trainingHp) return false
        if (trainingAp != other.trainingAp) return false
        if (trainingBp != other.trainingBp) return false
        if (remainingTrainingTimeInMinutes != other.remainingTrainingTimeInMinutes) return false
        if (itemEffectMentalStateValue != other.itemEffectMentalStateValue) return false
        if (itemEffectMentalStateMinutesRemaining != other.itemEffectMentalStateMinutesRemaining) return false
        if (itemEffectActivityLevelValue != other.itemEffectActivityLevelValue) return false
        if (itemEffectActivityLevelMinutesRemaining != other.itemEffectActivityLevelMinutesRemaining) return false
        if (itemEffectVitalPointsChangeValue != other.itemEffectVitalPointsChangeValue) return false
        if (itemEffectVitalPointsChangeMinutesRemaining != other.itemEffectVitalPointsChangeMinutesRemaining) return false
        if (abilityRarity != other.abilityRarity) return false
        if (abilityType != other.abilityType) return false
        if (abilityBranch != other.abilityBranch) return false
        if (abilityReset != other.abilityReset) return false
        if (rank != other.rank) return false
        if (itemType != other.itemType) return false
        if (itemMultiplier != other.itemMultiplier) return false
        if (itemRemainingTime != other.itemRemainingTime) return false
        if (!otp0.contentEquals(other.otp0)) return false
        if (!otp1.contentEquals(other.otp1)) return false
        if (characterCreationFirmwareVersion != other.characterCreationFirmwareVersion) return false
        if (!appReserved1.contentEquals(other.appReserved1)) return false
        if (!appReserved2.contentEquals(other.appReserved2)) return false

        return true
    }

    override fun hashCode(): Int {
        return Objects.hash(
            super.hashCode(),
            trainingHp,
            trainingAp,
            trainingBp,
            remainingTrainingTimeInMinutes,
            itemEffectMentalStateValue,
            itemEffectMentalStateMinutesRemaining,
            itemEffectActivityLevelValue,
            itemEffectActivityLevelMinutesRemaining,
            itemEffectVitalPointsChangeValue,
            itemEffectVitalPointsChangeMinutesRemaining,
            abilityRarity,
            abilityType,
            abilityBranch,
            abilityReset,
            rank,
            itemType,
            itemMultiplier,
            itemRemainingTime,
            otp0.contentHashCode(),
            otp1.contentHashCode(),
            characterCreationFirmwareVersion,
            appReserved1.contentHashCode(),
            appReserved2.contentHashCode())
    }

    @OptIn(ExperimentalStdlibApi::class)
    override fun toString(): String {
        return """
${super.toString()}
BENfcCharacter(
    trainingHp=$trainingHp,
    trainingAp=$trainingAp,
    trainingBp=$trainingBp,
    remainingTrainingTimeInMinutes=$remainingTrainingTimeInMinutes,
    itemEffectMentalStateValue=$itemEffectMentalStateValue,
    itemEffectMentalStateMinutesRemaining=$itemEffectMentalStateMinutesRemaining,
    itemEffectActivityLevelValue=$itemEffectActivityLevelValue,
    itemEffectActivityLevelMinutesRemaining=$itemEffectActivityLevelMinutesRemaining,
    itemEffectVitalPointsChangeValue=$itemEffectVitalPointsChangeValue,
    itemEffectVitalPointsChangeMinutesRemaining=$itemEffectVitalPointsChangeMinutesRemaining,
    abilityRarity=$abilityRarity,
    abilityType=$abilityType,
    abilityBranch=$abilityBranch,
    abilityReset=$abilityReset,
    rank=$rank,
    itemType=$itemType,
    itemMultiplier=$itemMultiplier,
    itemRemainingTime=$itemRemainingTime,
    otp0=${otp0.toHexString()},
    otp1=${otp1.toHexString()},
    characterCreationFirmwareVersion=$characterCreationFirmwareVersion,
    appReserved1=${appReserved1.contentToString()},
    appReserved2=${appReserved2.contentToString()}
)"""
    }


}
