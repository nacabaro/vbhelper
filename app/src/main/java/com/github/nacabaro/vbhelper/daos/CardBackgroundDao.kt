package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.card.CardBackground

@Dao
interface CardBackgroundDao {
    @Insert
    suspend fun insertCardBackground(vararg cardBackground: CardBackground)

    @Query("""
        SELECT * FROM CardBackground
        WHERE cardId = :cardId
    """)
    suspend fun getCardBackground(cardId: Long): CardBackground

    @Query("""
        SELECT * FROM CardBackground
    """)
    suspend fun getBackgrounds(): List<CardBackground>

}