package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import com.github.nacabaro.vbhelper.dtos.CardDtos
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import kotlinx.coroutines.flow.Flow

@Dao
interface DexDao {
    @Query(
        """
        INSERT OR IGNORE INTO Dex(id, discoveredOn)
        VALUES (
            (SELECT id FROM CardCharacter WHERE charaIndex = :charIndex AND cardId = :cardId), 
            :discoveredOn
        )
    """
    )
    fun insertCharacter(charIndex: Int, cardId: Long, discoveredOn: Long)

    @Query(
        """
        SELECT 
            c.id AS id,
            s.spriteIdle1 AS spriteIdle,
            s.width AS spriteWidth,
            s.height AS spriteHeight,
            c.nameSprite AS nameSprite,
            c.nameWidth AS nameSpriteWidth,
            c.nameHeight AS nameSpriteHeight,
            d.discoveredOn AS discoveredOn,
            c.baseHp as baseHp,
            c.baseBp as baseBp,
            c.baseAp as baseAp,
            c.stage as stage,
            c.attribute as attribute
        FROM CardCharacter c
        JOIN Sprite s ON c.spriteId = s.id
        LEFT JOIN dex d ON c.id = d.id
        WHERE c.cardId = :cardId
    """
    )
    fun getSingleCardProgress(cardId: Long): Flow<List<CharacterDtos.CardCharaProgress>>

    @Query(
        """
        SELECT 
            c.id as cardId,
            c.name as cardName,
            c.logo as cardLogo,
            c.logoWidth as logoWidth,
            c.logoHeight as logoHeight, 
            (SELECT COUNT(*) FROM CardCharacter cc WHERE cc.cardId = c.id) AS totalCharacters,
            (SELECT COUNT(*) FROM Dex d JOIN CardCharacter cc ON d.id = cc.id WHERE cc.cardId = c.id AND d.discoveredOn IS NOT NULL) AS obtainedCharacters
        FROM Card c
    """
    )
    fun getCardsWithProgress(): Flow<List<CardDtos.CardProgress>>
}