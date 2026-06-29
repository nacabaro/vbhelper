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
        val isInAdventure: Boolean,
        val active: Boolean
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CharacterWithSprites

            if (id != other.id) return false
            if (charId != other.charId) return false
            if (stage != other.stage) return false
            if (attribute != other.attribute) return false
            if (ageInDays != other.ageInDays) return false
            if (mood != other.mood) return false
            if (vitalPoints != other.vitalPoints) return false
            if (transformationCountdown != other.transformationCountdown) return false
            if (injuryStatus != other.injuryStatus) return false
            if (trophies != other.trophies) return false
            if (currentPhaseBattlesWon != other.currentPhaseBattlesWon) return false
            if (currentPhaseBattlesLost != other.currentPhaseBattlesLost) return false
            if (totalBattlesWon != other.totalBattlesWon) return false
            if (totalBattlesLost != other.totalBattlesLost) return false
            if (activityLevel != other.activityLevel) return false
            if (heartRateCurrent != other.heartRateCurrent) return false
            if (characterType != other.characterType) return false
            if (!spriteIdle.contentEquals(other.spriteIdle)) return false
            if (!spriteIdle2.contentEquals(other.spriteIdle2)) return false
            if (spriteWidth != other.spriteWidth) return false
            if (spriteHeight != other.spriteHeight) return false
            if (!nameSprite.contentEquals(other.nameSprite)) return false
            if (nameSpriteWidth != other.nameSpriteWidth) return false
            if (nameSpriteHeight != other.nameSpriteHeight) return false
            if (isBemCard != other.isBemCard) return false
            if (isInAdventure != other.isInAdventure) return false
            if (active != other.active) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + charId.hashCode()
            result = 31 * result + stage
            result = 31 * result + attribute.hashCode()
            result = 31 * result + ageInDays
            result = 31 * result + mood
            result = 31 * result + vitalPoints
            result = 31 * result + transformationCountdown
            result = 31 * result + injuryStatus.hashCode()
            result = 31 * result + trophies
            result = 31 * result + currentPhaseBattlesWon
            result = 31 * result + currentPhaseBattlesLost
            result = 31 * result + totalBattlesWon
            result = 31 * result + totalBattlesLost
            result = 31 * result + activityLevel
            result = 31 * result + heartRateCurrent
            result = 31 * result + characterType.hashCode()
            result = 31 * result + spriteIdle.contentHashCode()
            result = 31 * result + spriteIdle2.contentHashCode()
            result = 31 * result + spriteWidth
            result = 31 * result + spriteHeight
            result = 31 * result + nameSprite.contentHashCode()
            result = 31 * result + nameSpriteWidth
            result = 31 * result + nameSpriteHeight
            result = 31 * result + isBemCard.hashCode()
            result = 31 * result + isInAdventure.hashCode()
            result = 31 * result + active.hashCode()
            return result
        }
    }

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
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as TransformationHistory

            if (id != other.id) return false
            if (!spriteIdle.contentEquals(other.spriteIdle)) return false
            if (spriteWidth != other.spriteWidth) return false
            if (spriteHeight != other.spriteHeight) return false
            if (monIndex != other.monIndex) return false
            if (transformationDate != other.transformationDate) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + spriteIdle.contentHashCode()
            result = 31 * result + spriteWidth
            result = 31 * result + spriteHeight
            result = 31 * result + monIndex
            result = 31 * result + transformationDate.hashCode()
            return result
        }
    }

    data class TransformationHistoryExport(
        val stageId: Long,
        val monIndex: Int,
        val cardName: String,
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
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CardCharaProgress

            if (id != other.id) return false
            if (!spriteIdle.contentEquals(other.spriteIdle)) return false
            if (spriteWidth != other.spriteWidth) return false
            if (spriteHeight != other.spriteHeight) return false
            if (!nameSprite.contentEquals(other.nameSprite)) return false
            if (nameSpriteWidth != other.nameSpriteWidth) return false
            if (nameSpriteHeight != other.nameSpriteHeight) return false
            if (discoveredOn != other.discoveredOn) return false
            if (baseHp != other.baseHp) return false
            if (baseBp != other.baseBp) return false
            if (baseAp != other.baseAp) return false
            if (stage != other.stage) return false
            if (attribute != other.attribute) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + spriteIdle.contentHashCode()
            result = 31 * result + spriteWidth
            result = 31 * result + spriteHeight
            result = 31 * result + nameSprite.contentHashCode()
            result = 31 * result + nameSpriteWidth
            result = 31 * result + nameSpriteHeight
            result = 31 * result + (discoveredOn?.hashCode() ?: 0)
            result = 31 * result + baseHp
            result = 31 * result + baseBp
            result = 31 * result + baseAp
            result = 31 * result + stage
            result = 31 * result + attribute.hashCode()
            return result
        }
    }

    data class CardProgress(
        val id: Long,
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CardProgress

            if (id != other.id) return false
            if (!spriteIdle.contentEquals(other.spriteIdle)) return false
            if (spriteWidth != other.spriteWidth) return false
            if (spriteHeight != other.spriteHeight) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + spriteIdle.contentHashCode()
            result = 31 * result + spriteWidth
            result = 31 * result + spriteHeight
            return result
        }
    }

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
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as AdventureCharacterWithSprites

            if (id != other.id) return false
            if (charId != other.charId) return false
            if (stage != other.stage) return false
            if (attribute != other.attribute) return false
            if (ageInDays != other.ageInDays) return false
            if (mood != other.mood) return false
            if (vitalPoints != other.vitalPoints) return false
            if (transformationCountdown != other.transformationCountdown) return false
            if (injuryStatus != other.injuryStatus) return false
            if (trophies != other.trophies) return false
            if (currentPhaseBattlesWon != other.currentPhaseBattlesWon) return false
            if (currentPhaseBattlesLost != other.currentPhaseBattlesLost) return false
            if (totalBattlesWon != other.totalBattlesWon) return false
            if (totalBattlesLost != other.totalBattlesLost) return false
            if (activityLevel != other.activityLevel) return false
            if (heartRateCurrent != other.heartRateCurrent) return false
            if (characterType != other.characterType) return false
            if (!spriteIdle.contentEquals(other.spriteIdle)) return false
            if (spriteWidth != other.spriteWidth) return false
            if (spriteHeight != other.spriteHeight) return false
            if (isBemCard != other.isBemCard) return false
            if (finishesAdventure != other.finishesAdventure) return false
            if (originalTimeInMinutes != other.originalTimeInMinutes) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + charId.hashCode()
            result = 31 * result + stage
            result = 31 * result + attribute.hashCode()
            result = 31 * result + ageInDays
            result = 31 * result + mood
            result = 31 * result + vitalPoints
            result = 31 * result + transformationCountdown
            result = 31 * result + injuryStatus.hashCode()
            result = 31 * result + trophies
            result = 31 * result + currentPhaseBattlesWon
            result = 31 * result + currentPhaseBattlesLost
            result = 31 * result + totalBattlesWon
            result = 31 * result + totalBattlesLost
            result = 31 * result + activityLevel
            result = 31 * result + heartRateCurrent
            result = 31 * result + characterType.hashCode()
            result = 31 * result + spriteIdle.contentHashCode()
            result = 31 * result + spriteWidth
            result = 31 * result + spriteHeight
            result = 31 * result + isBemCard.hashCode()
            result = 31 * result + finishesAdventure.hashCode()
            result = 31 * result + originalTimeInMinutes.hashCode()
            return result
        }
    }

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
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as EvolutionRequirementsWithSpritesAndObtained

            if (charaId != other.charaId) return false
            if (fromCharaId != other.fromCharaId) return false
            if (!spriteIdle.contentEquals(other.spriteIdle)) return false
            if (spriteWidth != other.spriteWidth) return false
            if (spriteHeight != other.spriteHeight) return false
            if (discoveredOn != other.discoveredOn) return false
            if (requiredTrophies != other.requiredTrophies) return false
            if (requiredVitals != other.requiredVitals) return false
            if (requiredBattles != other.requiredBattles) return false
            if (requiredWinRate != other.requiredWinRate) return false
            if (changeTimerHours != other.changeTimerHours) return false
            if (requiredAdventureLevelCompleted != other.requiredAdventureLevelCompleted) return false

            return true
        }

        override fun hashCode(): Int {
            var result = charaId.hashCode()
            result = 31 * result + fromCharaId.hashCode()
            result = 31 * result + spriteIdle.contentHashCode()
            result = 31 * result + spriteWidth
            result = 31 * result + spriteHeight
            result = 31 * result + (discoveredOn?.hashCode() ?: 0)
            result = 31 * result + requiredTrophies
            result = 31 * result + requiredVitals
            result = 31 * result + requiredBattles
            result = 31 * result + requiredWinRate
            result = 31 * result + changeTimerHours
            result = 31 * result + requiredAdventureLevelCompleted
            return result
        }
    }

    data class FusionsWithSpritesAndObtained(
        val charaId: Long,
        val fromCharaId: Long,
        val spriteIdle: ByteArray,
        val spriteWidth: Int,
        val spriteHeight: Int,
        val discoveredOn: Long?,
        val fusionAttribute: NfcCharacter.Attribute
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as FusionsWithSpritesAndObtained

            if (charaId != other.charaId) return false
            if (fromCharaId != other.fromCharaId) return false
            if (!spriteIdle.contentEquals(other.spriteIdle)) return false
            if (spriteWidth != other.spriteWidth) return false
            if (spriteHeight != other.spriteHeight) return false
            if (discoveredOn != other.discoveredOn) return false
            if (fusionAttribute != other.fusionAttribute) return false

            return true
        }

        override fun hashCode(): Int {
            var result = charaId.hashCode()
            result = 31 * result + fromCharaId.hashCode()
            result = 31 * result + spriteIdle.contentHashCode()
            result = 31 * result + spriteWidth
            result = 31 * result + spriteHeight
            result = 31 * result + (discoveredOn?.hashCode() ?: 0)
            result = 31 * result + fusionAttribute.hashCode()
            return result
        }
    }
}
