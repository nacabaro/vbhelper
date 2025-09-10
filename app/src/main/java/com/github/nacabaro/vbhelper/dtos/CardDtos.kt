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
    )

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
    )
}