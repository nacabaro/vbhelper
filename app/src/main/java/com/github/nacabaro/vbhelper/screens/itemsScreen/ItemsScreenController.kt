package com.github.nacabaro.vbhelper.screens.itemsScreen


interface ItemsScreenController {
    fun applyItem(itemId: Long, characterId: Long, onCompletion: () -> Unit)
}