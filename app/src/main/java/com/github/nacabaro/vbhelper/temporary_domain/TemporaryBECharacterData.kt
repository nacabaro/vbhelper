package com.github.nacabaro.vbhelper.temporary_domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.cfogrady.vbnfc.be.FirmwareVersion
import com.github.cfogrady.vbnfc.data.NfcCharacter

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TemporaryCharacterData::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TemporaryBECharacterData (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val trainingHp: UShort,
    val trainingAp: UShort,
    val trainingBp: UShort,
    val remainingTrainingTimeInMinutes: UShort,
    val itemEffectMentalStateValue: Byte,
    val itemEffectMentalStateMinutesRemaining: Byte,
    val itemEffectActivityLevelValue: Byte,
    val itemEffectActivityLevelMinutesRemaining: Byte,
    val itemEffectVitalPointsChangeValue: Byte,
    val itemEffectVitalPointsChangeMinutesRemaining: Byte,
    val abilityRarity: NfcCharacter.AbilityRarity,
    val abilityType: UShort,
    val abilityBranch: UShort,
    val abilityReset: Byte,
    val rank: Byte,
    val itemType: Byte,
    val itemMultiplier: Byte,
    val itemRemainingTime: Byte,
    val otp0: String,
    val otp1: String,
    var characterCreationFirmwareVersion: FirmwareVersion,
)