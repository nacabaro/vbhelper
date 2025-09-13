package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.card.CardProgress
import com.github.nacabaro.vbhelper.dtos.CharacterDtos

@Dao
interface CardProgressDao {
    @Query("""
        UPDATE CardProgress 
        SET 
            currentStage = :currentStage, 
            unlocked = :unlocked
        WHERE cardId = :cardId AND
            currentStage < :currentStage
    """)
    fun updateCardProgress(currentStage: Int, cardId: Long, unlocked: Boolean)

    @Query(
        "SELECT currentStage FROM CardProgress WHERE cardId = :cardId"
    )
    fun getCardProgress(cardId: Long): Int

    @Insert
    fun insertCardProgress(cardProgress: CardProgress)
}