package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.TransformationHistory
import com.github.nacabaro.vbhelper.dtos.CharacterDtos

@Dao
interface UserCharacterDao {
    @Insert
    fun insertCharacterData(characterData: UserCharacter): Long

    @Insert
    fun insertBECharacterData(characterData: BECharacterData)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTransformationHistory(vararg transformationHistory: TransformationHistory)

    @Query("SELECT * FROM TransformationHistory WHERE monId = :monId")
    fun getTransformationHistory(monId: Int): List<TransformationHistory>

    @Query("""
        SELECT
            uc.*,
            c.sprite1 AS spriteIdle,
            c.spritesWidth AS spriteWidth,
            c.spritesHeight AS spriteHeight
        FROM UserCharacter uc
        JOIN Character c ON uc.charId = c.id
        """)
    suspend fun getAllCharacters(): List<CharacterDtos.CharacterWithSprites>

    @Query("SELECT * FROM UserCharacter WHERE id = :id")
    suspend fun getCharacter(id: Long): UserCharacter
}