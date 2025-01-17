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

    @Query("SELECT * FROM Card")
    suspend fun getAllDims(): List<Card>

    @Query("SELECT * FROM Card WHERE dimId = :id")
    fun getDimById(id: Int): Card?
}