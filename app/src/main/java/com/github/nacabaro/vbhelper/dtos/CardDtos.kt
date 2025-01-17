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
}