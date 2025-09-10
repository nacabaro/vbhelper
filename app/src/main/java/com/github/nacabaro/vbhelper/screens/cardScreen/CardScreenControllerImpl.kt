package com.github.nacabaro.vbhelper.screens.cardScreen

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.CardDtos
import kotlinx.coroutines.launch

class CardScreenControllerImpl(
    private val componentActivity: ComponentActivity,
) : CardScreenController {
    private val application = componentActivity.applicationContext as VBHelper
    private val database = application.container.db

    override fun renameCard(cardId: Long, newName: String, onRenamed: (String) -> Unit) {
        componentActivity.lifecycleScope.launch {
            database
                .cardDao()
                .renameCard(cardId.toInt(), newName)

            onRenamed(newName)
        }
    }

    override fun deleteCard(cardId: Long, onDeleted: () -> Unit) {
        componentActivity.lifecycleScope.launch {
            database
                .cardDao()
                .deleteCard(cardId)

            onDeleted()
        }
    }

    override suspend fun getCardAdventureMissions(cardId: Long): List<CardDtos.CardAdventureWithSprites> {
        return database
            .cardAdventureDao()
            .getAdventureForCard(cardId)
    }

    override suspend fun getCardProgress(cardId: Long): Int {
        return database
            .cardProgressDao()
            .getCardProgress(cardId)
    }
}