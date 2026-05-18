package com.github.nacabaro.vbhelper.companion.validation

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ValidatedCardDao {
    @Query("select cardId from ${ValidatedCardEntity.TABLE}")
    suspend fun getIds(): List<Int>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(validatedCardEntity: ValidatedCardEntity)
}

