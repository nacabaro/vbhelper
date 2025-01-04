package com.github.nacabaro.vbhelper.temporary_domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vbnfc.data.NfcCharacter

@Entity
data class TemporaryCharacterData (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dimId: Int,
    var charIndex: Int,
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
)