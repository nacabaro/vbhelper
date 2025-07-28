package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import com.github.nacabaro.vbhelper.dtos.CharacterDtos


@Dao
interface AdventureDao {
    @Query("""
        INSERT INTO Adventure (characterId, originalDuration, finishesAdventure)
        VALUES (:characterId, :originalDuration, strftime('%s', 'now') + :timeInSeconds)
    """)
    fun insertNewAdventure(characterId: Long, originalDuration: Long, timeInSeconds: Long)

    @Query("""
        SELECT COUNT(*) FROM Adventure
    """)
    fun getAdventureCount(): Int

    @Query("""
        SELECT
            uc.*,
            c.stage,
            c.attribute,
            s.spriteIdle1 AS spriteIdle,
            s.width AS spriteWidth,
            s.height AS spriteHeight,
            d.isBEm as isBemCard,
            a.finishesAdventure AS finishesAdventure,
            a.originalDuration AS originalTimeInMinutes
        FROM UserCharacter uc
        JOIN Character c ON uc.charId = c.id
        JOIN Sprite s ON s.id = c.spriteId
        JOIN Card d ON c.dimId = d.id
        JOIN Adventure a ON uc.id = a.characterId
    """)
    suspend fun getAdventureCharacters(): List<CharacterDtos.AdventureCharacterWithSprites>

    @Query("""
        DELETE FROM Adventure
        WHERE characterId = :characterId
    """)
    suspend fun deleteAdventure(characterId: Long)
}