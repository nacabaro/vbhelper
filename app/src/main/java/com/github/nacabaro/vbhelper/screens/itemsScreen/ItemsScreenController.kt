package com.github.nacabaro.vbhelper.screens.itemsScreen

import com.github.nacabaro.vbhelper.domain.items.Items

interface ItemsScreenController {
    suspend fun applyItem(item: Items, characterId: Long)
}