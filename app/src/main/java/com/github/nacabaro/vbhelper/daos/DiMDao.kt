package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.characters.Card

@Dao
interface DiMDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewDim(card: Card): Long

    @Query("SELECT * FROM Card WHERE cardId = :id")
    fun getDimById(id: Int): Card?

    @Query(
        """
        UPDATE Card
        SET currentStage = :currentStage
        WHERE cardId = :id
    """
    )
    fun updateCurrentStage(id: Int, currentStage: Int)

    @Query("""
        SELECT currentStage 
        FROM Card
        WHERE cardId = :id
    """)
    fun getCurrentStage(id: Int): Int
}