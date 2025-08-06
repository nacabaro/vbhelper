package com.github.nacabaro.vbhelper.screens.cardScreen

interface CardScreenController {
    fun renameCard(cardId: Long, newName: String, onRenamed: (String) -> Unit)
    fun deleteCard(cardId: Long, onDeleted: () -> Unit)
}