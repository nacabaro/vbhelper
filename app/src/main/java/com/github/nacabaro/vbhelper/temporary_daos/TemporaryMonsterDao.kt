package com.github.nacabaro.vbhelper.temporary_daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryBECharacterData
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryCharacterData
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryTransformationHistory

@Dao
interface TemporaryMonsterDao {
    @Insert
    fun insertCharacterData(temporaryCharacterData: TemporaryCharacterData): Int

    @Insert
    fun insertBECharacterData(temporaryBECharacterData: TemporaryBECharacterData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransformationHistory(vararg transformationHistory: TemporaryTransformationHistory)

    @Query("SELECT * FROM TemporaryTransformationHistory WHERE monId = :monId")
    fun getTransformationHistory(monId: Int): List<TemporaryTransformationHistory>

    @Query("SELECT * FROM TemporaryCharacterData")
    suspend fun getAllCharacters(): List<TemporaryCharacterData>
}