package com.github.nacabaro.vbhelper.screens.storageScreen

interface StorageScreenController {
    fun setActive(characterId: Long, onCompletion: () -> Unit)
    fun deleteCharacter(characterId: Long, onCompletion: () -> Unit)
}