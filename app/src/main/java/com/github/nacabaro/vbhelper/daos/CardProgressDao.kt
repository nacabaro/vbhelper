package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.github.nacabaro.vbhelper.domain.card.CardProgress

@Dao
interface CardProgressDao {
    @Upsert
    fun updateDimProgress(vararg cardProgresses: CardProgress)

    @Query(
        "SELECT currentStage FROM CardProgress WHERE cardId = :cardId"
    )
    fun getCardProgress(cardId: Long): Int
}