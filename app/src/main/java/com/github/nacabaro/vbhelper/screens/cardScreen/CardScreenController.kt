package com.github.nacabaro.vbhelper.screens.cardScreen

import com.github.nacabaro.vbhelper.dtos.CardDtos

interface CardScreenController {
    fun renameCard(cardId: Long, newName: String, onRenamed: (String) -> Unit)
    fun deleteCard(cardId: Long, onDeleted: () -> Unit)
    suspend fun getCardAdventureMissions(cardId: Long): List<CardDtos.CardAdventureWithSprites>
    suspend fun getCardProgress(cardId: Long): Int
}