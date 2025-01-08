package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.Dim

@Dao
interface DiMDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertNewDim(dim: Dim): Long

    @Query("SELECT * FROM Dim")
    suspend fun getAllDims(): List<Dim>

    @Query("SELECT * FROM Dim WHERE dimId = :id")
    fun getDimById(id: Int): Dim?
}