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
        val spriteIdle2: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
        val nameSprite: ByteArray,
        val nameSpriteWidth: Int,
        val nameSpriteHeight: Int,
        val isBemCard: Boolean,
        val isInAdventure: Boolean
    )

    data class CardCharacterInfo(
        val cardId: Long,
        val charId: Int,
        val stage: Int,
        val attribute: NfcCharacter.Attribute,
        val currentStage: Int
    )

    data class TransformationHistory(
        val id: Long,
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
        val monIndex: Int,
        val transformationDate: Long
    )

    data class CardCharaProgress(
        val id: Long,
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
        val nameSprite: ByteArray,
        val nameSpriteWidth: Int,
        val nameSpriteHeight: Int,
        val discoveredOn: Long?,
        val baseHp: Int,
        val baseBp: Int,
        val baseAp: Int,
        val stage: Int,
        val attribute: NfcCharacter.Attribute,
    )

    data class CardProgress(
        val id: Long,
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
    )

    data class AdventureCharacterWithSprites(
        var id: Long = 0,
        var charId: Long,
        var stage: Int,
        var attribute: NfcCharacter.Attribute,
        var ageInDays: Int,
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

    data class EvolutionRequirementsWithSpritesAndObtained(
        val charaId: Long,
        val fromCharaId: Long,
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
        val discoveredOn: Long?,
        val requiredTrophies: Int,
        val requiredVitals: Int,
        val requiredBattles: Int,
        val requiredWinRate: Int,
        val changeTimerHours: Int,
        val requiredAdventureLevelCompleted: Int
    )
}