package com.github.nacabaro.vbhelper.screens.adventureScreen

import com.github.nacabaro.vbhelper.dtos.ItemDtos

interface AdventureScreenController {
    fun sendCharacterToAdventure(characterId: Long, timeInMinutes: Long)
    fun getItemFromAdventure(characterId: Long, onResult: (ItemDtos.PurchasedItem) -> Unit)
    fun cancelAdventure(characterId: Long, onResult: () -> Unit)
}