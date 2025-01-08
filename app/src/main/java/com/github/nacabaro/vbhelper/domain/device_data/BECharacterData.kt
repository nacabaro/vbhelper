package com.github.nacabaro.vbhelper.domain.device_data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.cfogrady.vbnfc.data.NfcCharacter

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserCharacter::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class BECharacterData (
    @PrimaryKey(autoGenerate = true) val id: Long,
    val trainingHp: Int,
    val trainingAp: Int,
    val trainingBp: Int,
    val remainingTrainingTimeInMinutes: Int,
    val itemEffectMentalStateValue: Int,
    val itemEffectMentalStateMinutesRemaining: Int,
    val itemEffectActivityLevelValue: Int,
    val itemEffectActivityLevelMinutesRemaining: Int,
    val itemEffectVitalPointsChangeValue: Int,
    val itemEffectVitalPointsChangeMinutesRemaining: Int,
    val abilityRarity: NfcCharacter.AbilityRarity,
    val abilityType: Int,
    val abilityBranch: Int,
    val abilityReset: Int,
    val rank: Int,
    val itemType: Int,
    val itemMultiplier: Int,
    val itemRemainingTime: Int,
    val otp0: String,
    val otp1: String,
    val minorVersion: Int,
    val majorVersion: Int,
)