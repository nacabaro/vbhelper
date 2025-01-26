package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import com.github.nacabaro.vbhelper.dtos.CharacterDtos


@Dao
interface AdventureDao {
    @Query("""
        INSERT INTO Adventure (characterId, finishesAdventure)
        VALUES (:characterId, strftime('%s', 'now') + :timeInSeconds)
    """)
    fun insertNewAdventure(characterId: Long, timeInSeconds: Long)

    @Query("""
        SELECT COUNT(*) FROM Adventure
    """)
    fun getAdventureCount(): Int

    @Query("""
        SELECT
            uc.*,
            c.sprite1 AS spriteIdle,
            c.spritesWidth AS spriteWidth,
            c.spritesHeight AS spriteHeight,
            d.isBEm as isBemCard,
            a.finishesAdventure AS timeLeft
        FROM UserCharacter uc
        JOIN Character c ON uc.charId = c.id
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