package com.github.nacabaro.vbhelper.dtos

object CardDtos {
    data class CardProgress (
        val cardId: Long,
        val cardName: String,
        val cardLogo: ByteArray,
        val logoWidth: Int,
        val logoHeight: Int,
        val totalCharacters: Int,
        val obtainedCharacters: Int,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CardProgress

            if (cardId != other.cardId) return false
            if (cardName != other.cardName) return false
            if (!cardLogo.contentEquals(other.cardLogo)) return false
            if (logoWidth != other.logoWidth) return false
            if (logoHeight != other.logoHeight) return false
            if (totalCharacters != other.totalCharacters) return false
            if (obtainedCharacters != other.obtainedCharacters) return false

            return true
        }

        override fun hashCode(): Int {
            var result = cardId.hashCode()
            result = 31 * result + cardName.hashCode()
            result = 31 * result + cardLogo.contentHashCode()
            result = 31 * result + logoWidth
            result = 31 * result + logoHeight
            result = 31 * result + totalCharacters
            result = 31 * result + obtainedCharacters
            return result
        }
    }

    data class CardAdventureWithSprites (
        val characterName: ByteArray,
        val characterNameWidth: Int,
        val characterNameHeight: Int,
        val characterIdleSprite: ByteArray,
        val characterIdleSpriteWidth: Int,
        val characterIdleSpriteHeight: Int,
        val characterAp: Int,
        val characterBp: Int?,
        val characterDp: Int,
        val characterHp: Int,
        val steps: Int,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CardAdventureWithSprites

            if (!characterName.contentEquals(other.characterName)) return false
            if (characterNameWidth != other.characterNameWidth) return false
            if (characterNameHeight != other.characterNameHeight) return false
            if (!characterIdleSprite.contentEquals(other.characterIdleSprite)) return false
            if (characterIdleSpriteWidth != other.characterIdleSpriteWidth) return false
            if (characterIdleSpriteHeight != other.characterIdleSpriteHeight) return false
            if (characterAp != other.characterAp) return false
            if (characterBp != other.characterBp) return false
            if (characterDp != other.characterDp) return false
            if (characterHp != other.characterHp) return false
            if (steps != other.steps) return false

            return true
        }

        override fun hashCode(): Int {
            var result = characterName.contentHashCode()
            result = 31 * result + characterNameWidth
            result = 31 * result + characterNameHeight
            result = 31 * result + characterIdleSprite.contentHashCode()
            result = 31 * result + characterIdleSpriteWidth
            result = 31 * result + characterIdleSpriteHeight
            result = 31 * result + characterAp
            result = 31 * result + (characterBp ?: 0)
            result = 31 * result + characterDp
            result = 31 * result + characterHp
            result = 31 * result + steps
            return result
        }
    }

    data class CardIcon (
        val cardIcon: ByteArray,
        val cardIconWidth: Int,
        val cardIconHeight: Int
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as CardIcon

            if (!cardIcon.contentEquals(other.cardIcon)) return false
            if (cardIconWidth != other.cardIconWidth) return false
            if (cardIconHeight != other.cardIconHeight) return false

            return true
        }

        override fun hashCode(): Int {
            var result = cardIcon.contentHashCode()
            result = 31 * result + cardIconWidth
            result = 31 * result + cardIconHeight
            return result
        }
    }
}