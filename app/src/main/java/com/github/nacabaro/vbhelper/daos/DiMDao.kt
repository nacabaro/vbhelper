package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.Dim
import kotlinx.coroutines.flow.Flow

@Dao
interface DiMDao {
    @Insert
    suspend fun insertNewDim(dim: Dim)

    @Query("SELECT * FROM Dim")
    fun getAllDims(): Flow<List<Dim>>
}