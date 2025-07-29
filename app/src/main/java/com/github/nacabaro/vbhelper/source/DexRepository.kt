package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.dtos.CardDtos
import com.github.nacabaro.vbhelper.dtos.CharacterDtos

class DexRepository (
    private val db: AppDatabase
) {
    suspend fun getAllDims(): List<CardDtos.CardProgress> {
        return db.dexDao().getCardsWithProgress()
    }

    suspend fun getCharactersByDimId(cardId: Long): List<CharacterDtos.CardProgress> {
        return db.dexDao().getSingleCardProgress(cardId)
    }
}