package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import com.github.nacabaro.vbhelper.dtos.CardDtos

@Dao
interface CardAdventureDao {
    @Query("""
        INSERT INTO 
            CardAdventure (cardId, characterId, steps, bossAp, bossHp, bossDp, bossBp)
        SELECT
            :cardId,
            cc.id,
            :steps,
            :bossAp,
            :bossHp,
            :bossDp,
            :bossBp
        FROM
            CardCharacter cc
        WHERE
            cc.charaIndex = :characterId AND
            cc.cardId = :cardId
    """)
    suspend fun insertNewAdventure(
        cardId: Long,
        characterId: Int,
        steps: Int,
        bossAp: Int,
        bossHp: Int,
        bossDp: Int,
        bossBp: Int?
    )

    @Query("""
        SELECT
            cc.nameSprite as characterName,
            cc.nameWidth as characterNameWidth,
            cc.nameHeight as characterNameHeight,
            s.spriteIdle1 as characterIdleSprite,
            s.width as characterIdleSpriteWidth,
            s.height as characterIdleSpriteHeight,
            ca.bossAp as characterAp,
            ca.bossBp as characterBp,
            ca.bossDp as characterDp,
            ca.bossHp as characterHp,
            ca.steps as steps
        FROM CardCharacter cc
        JOIN Sprite s ON cc.spriteId = s.id
        JOIN CardAdventure ca ON cc.id = ca.characterId
        WHERE
            cc.cardId = :cardId
    """)
    suspend fun getAdventureForCard(
        cardId: Long
    ): List<CardDtos.CardAdventureWithSprites>
}