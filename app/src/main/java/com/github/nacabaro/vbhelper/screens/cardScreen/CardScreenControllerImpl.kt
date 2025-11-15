package com.github.nacabaro.vbhelper.screens.cardScreen

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.dtos.CardDtos
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import kotlinx.coroutines.flow.Flow
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

    override fun getCardAdventureMissions(cardId: Long): Flow<List<CardDtos.CardAdventureWithSprites>> {
        return database
            .cardAdventureDao()
            .getAdventureForCard(cardId)
    }

    override fun getCardProgress(cardId: Long): Flow<Int> {
        return database
            .cardProgressDao()
            .getCardProgress(cardId)
    }

    override fun getFusionsForCharacters(characterId: Long): Flow<List<CharacterDtos.FusionsWithSpritesAndObtained>> {
        return database
            .cardFusionsDao()
            .getFusionsForCharacter(characterId)
    }

}