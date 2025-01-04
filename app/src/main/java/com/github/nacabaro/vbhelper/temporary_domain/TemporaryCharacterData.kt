package com.github.nacabaro.vbhelper.temporary_domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.github.cfogrady.vbnfc.data.NfcCharacter

/*
dimId=16,
charIndex=8,
stage=4,
attribute=Free,
ageInDays=0,
nextAdventureMissionStage=9,
mood=99,
vitalPoints=9999,
transformationCountdown=1101,
injuryStatus=None,
trophies=0,
currentPhaseBattlesWon=19,
currentPhaseBattlesLost=4,
totalBattlesWon=36,
totalBattlesLost=10,
activityLevel=0,
heartRateCurrent=71,
*/


@Entity
data class TemporaryCharacterData (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val dimId: UShort,
    var charIndex: UShort,
    var stage: Byte,
    var attribute: NfcCharacter.Attribute,
    var ageInDays: Byte,
    var nextAdventureMissionStage: Byte, // next adventure mission stage on the character's dim
    var mood: Byte,
    var vitalPoints: UShort,
    var transformationCountdown: UShort,
    var injuryStatus: NfcCharacter.InjuryStatus,
    var trophies: UShort,
    var currentPhaseBattlesWon: UShort,
    var currentPhaseBattlesLost: UShort,
    var totalBattlesWon: UShort,
    var totalBattlesLost: UShort,
    var activityLevel: Byte,
    var heartRateCurrent: UByte,
    var transformationHistory: Int
)