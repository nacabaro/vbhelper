package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.card.Card
import kotlinx.coroutines.flow.Flow

class ScanRepository(
    val database: AppDatabase
) {
    fun getCardDetails(characterId: Long): Flow<Card> {
        return database.cardDao().getCardByCharacterId(characterId)
    }
}