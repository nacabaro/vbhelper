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

    @Query("""
        SELECT 
            c.id AS id,
            c.sprite1 AS spriteIdle,
            c.spritesWidth AS spriteWidth,
            c.spritesHeight AS spriteHeight,
            c.monIndex AS monIndex, 
            t.transformationDate AS transformationDate
        FROM TransformationHistory t 
        JOIN Character c ON c.id = t.stageId
        WHERE monId = :monId
    """)
    fun getTransformationHistory(monId: Long): List<CharacterDtos.TransformationHistory>?

    @Query("""
        SELECT
            uc.*,
            c.sprite1 AS spriteIdle,
            c.spritesWidth AS spriteWidth,
            c.spritesHeight AS spriteHeight,
            d.isBEm as isBemCard
        FROM UserCharacter uc
        JOIN Character c ON uc.charId = c.id
        JOIN Dim d ON c.dimId = d.id
        """)
    suspend fun getAllCharacters(): List<CharacterDtos.CharacterWithSprites>

    @Query("SELECT * FROM UserCharacter WHERE id = :id")
    suspend fun getCharacter(id: Long): UserCharacter

    @Query("SELECT * FROM BECharacterData WHERE id = :id")
    suspend fun getBeData(id: Long): BECharacterData

    @Query("""
        SELECT
            uc.*,
            c.sprite1 AS spriteIdle,
            c.spritesWidth AS spriteWidth,
            c.spritesHeight AS spriteHeight,
            d.isBEm as isBemCard
        FROM UserCharacter uc
        JOIN Character c ON uc.charId = c.id
        JOIN Dim d ON c.dimId = d.id
        WHERE uc.isActive = 1
        LIMIT 1
    """)
    suspend fun getActiveCharacter(): CharacterDtos.CharacterWithSprites?

    @Query("DELETE FROM UserCharacter WHERE id = :id")
    fun deleteCharacterById(id: Long)

    @Query("UPDATE UserCharacter SET isActive = 0 WHERE isActive = 1")
    fun clearActiveCharacter()

    @Query("UPDATE UserCharacter SET isActive = 1 WHERE id = :id")
    fun setActiveCharacter(id: Long)
}