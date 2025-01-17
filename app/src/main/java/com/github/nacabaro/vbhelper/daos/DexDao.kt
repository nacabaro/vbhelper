package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import com.github.nacabaro.vbhelper.dtos.CardDtos
import com.github.nacabaro.vbhelper.dtos.CharacterDtos

@Dao
interface DexDao {
    @Query("""
        INSERT OR IGNORE INTO Dex(id, discoveredOn)
        VALUES (
            (SELECT id FROM Character WHERE monIndex = :charIndex AND dimId = :cardId), 
            :discoveredOn
        )
    """)
    fun insertCharacter(charIndex: Int, cardId: Long, discoveredOn: Long)

    @Query("""
        SELECT 
            c.id AS id,
            c.sprite1 AS spriteIdle,
            c.spritesWidth AS spriteWidth,
            c.spritesHeight AS spriteHeight,
            d.discoveredOn AS discoveredOn
        FROM character c
        LEFT JOIN dex d ON c.id = d.id
        WHERE c.dimId = :cardId
    """)
    suspend fun getSingleCardProgress(cardId: Long): List<CharacterDtos.CardProgress>

    @Query("""
        SELECT 
            c.id as cardId,
            c.name as cardName,
            c.logo as cardLogo,
            c.logoWidth as logoWidth,
            c.logoHeight as logoHeight, 
            (SELECT COUNT(*) FROM Character cc WHERE cc.dimId = c.id) AS totalCharacters,
            (SELECT COUNT(*) FROM Dex d JOIN Character cc ON d.id = cc.id WHERE cc.dimId = c.id AND d.discoveredOn IS NOT NULL) AS obtainedCharacters
        FROM Card c
    """)
    suspend fun getCardsWithProgress(): List<CardDtos.CardProgress>
}