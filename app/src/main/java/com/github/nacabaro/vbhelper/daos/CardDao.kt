package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.card.Card

@Dao
interface CardDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewCard(card: Card): Long

    @Query("SELECT * FROM Card WHERE cardId = :id")
    fun getCardByCardId(id: Int): List<Card>

    @Query("SELECT * FROM Card WHERE id = :id")
    fun getCardById(id: Long): Card?

    @Query("""
        SELECT ca.* 
        FROM Card ca
        JOIN Character ch ON ca.id = ch.dimId
        JOIN UserCharacter uc ON ch.id = uc.charId
        WHERE uc.id = :id
    """)
    suspend fun getCardByCharacterId(id: Long): Card

    @Query("UPDATE Card SET name = :newName WHERE id = :id")
    suspend fun renameCard(id: Int, newName: String)

    @Query("DELETE FROM Card WHERE id = :id")
    suspend fun deleteCard(id: Long)
}