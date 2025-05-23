package com.github.nacabaro.vbhelper.dtos

import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.utils.DeviceType


object CharacterDtos {
    data class CharacterWithSprites(
        var id: Long = 0,
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
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
        val nameSprite: ByteArray,
        val nameSpriteWidth: Int,
        val nameSpriteHeight: Int,
        val isBemCard: Boolean,
        val isInAdventure: Boolean
    )

    data class DiMInfo(
        val cardId: Int,
        val charId: Int
    )

    data class TransformationHistory(
        val id: Long,
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
        val monIndex: Int,
        val transformationDate: Long
    )

    data class CardProgress(
        val id: Long,
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
        val discoveredOn: Long?
    )

    data class AdventureCharacterWithSprites(
        var id: Long = 0,
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
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
        val isBemCard: Boolean,
        val finishesAdventure: Long,
        val originalTimeInMinutes: Long
    )
}