package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.github.nacabaro.vbhelper.domain.characters.Character
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

    @Upsert
    fun updateCharacter(character: UserCharacter)

    @Upsert
    fun updateBECharacterData(characterData: BECharacterData)

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

    @Query(
        """
        SELECT
            uc.*,
            c.stage,
            c.attribute,
            c.sprite1 AS spriteIdle,
            c.spritesWidth AS spriteWidth,
            c.spritesHeight AS spriteHeight,
            c.name as nameSprite,
            c.nameWidth as nameSpriteWidth,
            c.nameHeight as nameSpriteHeight,
            d.isBEm as isBemCard,
            a.characterId = uc.id as isInAdventure
        FROM UserCharacter uc
        JOIN Character c ON uc.charId = c.id
        JOIN Card d ON c.dimId = d.id
        LEFT JOIN Adventure a ON a.characterId = uc.id
        """
    )
    suspend fun getAllCharacters(): List<CharacterDtos.CharacterWithSprites>

    @Query(
        """
        SELECT
            uc.*,
            c.stage,
            c.attribute,
            c.sprite1 AS spriteIdle,
            c.spritesWidth AS spriteWidth,
            c.spritesHeight AS spriteHeight,
            c.name as nameSprite,
            c.nameWidth as nameSpriteWidth,
            c.nameHeight as nameSpriteHeight,
            d.isBEm as isBemCard,
            a.characterId = uc.id as isInAdventure
        FROM UserCharacter uc
        JOIN Character c ON uc.charId = c.id
        JOIN Card d ON c.dimId = d.id
        LEFT JOIN Adventure a ON a.characterId = uc.id
        WHERE uc.id = :id
    """)
    suspend fun getCharacterWithSprites(id: Long): CharacterDtos.CharacterWithSprites

    @Query("SELECT * FROM UserCharacter WHERE id = :id")
    suspend fun getCharacter(id: Long): UserCharacter

    @Query("SELECT * FROM BECharacterData WHERE id = :id")
    suspend fun getBeData(id: Long): BECharacterData

    @Query(
        """
        SELECT
            uc.*,
            c.stage,
            c.attribute,
            c.sprite1 AS spriteIdle,
            c.spritesWidth AS spriteWidth,
            c.spritesHeight AS spriteHeight,
            c.name as nameSprite,
            c.nameWidth as nameSpriteWidth,
            c.nameHeight as nameSpriteHeight,
            d.isBEm as isBemCard,
            a.characterId as isInAdventure            
        FROM UserCharacter uc
        JOIN Character c ON uc.charId = c.id
        JOIN Card d ON c.dimId = d.id
        LEFT JOIN Adventure a ON a.characterId = uc.id
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

    @Query(
        """
        SELECT c.*
        FROM Character c
        join UserCharacter uc on c.id = uc.charId
        where uc.id = :charId
        LIMIT 1
        """
    )
    suspend fun getCharacterInfo(charId: Long): Character
}