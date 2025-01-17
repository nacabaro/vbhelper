package com.github.nacabaro.vbhelper.domain.device_data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.utils.DeviceType
import com.github.nacabaro.vbhelper.domain.characters.Character

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["charId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserCharacter (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var charId: Long,
    var stage: Int,
    var attribute: NfcCharacter.Attribute,
    var ageInDays: Int,
    var nextAdventureMissionStage: Int, // next adventure mission stage on the character's dim
    var mood: Int,
    var vitalPoints: Int,
    var transformationCountdown: Int,
    var injuryStatus: NfcCharacter.InjuryStatus,
    var trophies: Int,
    var currentPhaseBattlesWon: Int,
    var currentPhaseBattlesLost: Int,
    var totalBattlesWon: Int,
    var totalBattlesLost: Int,
    var activityLevel: Int,
    var heartRateCurrent: Int,
    var characterType: DeviceType,
    var isActive: Boolean
)