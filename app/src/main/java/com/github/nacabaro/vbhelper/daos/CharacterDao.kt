package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.card.CardCharacter
import com.github.nacabaro.vbhelper.domain.characters.Sprite
import com.github.nacabaro.vbhelper.dtos.CharacterDtos

@Dao
interface CharacterDao {
    @Insert
    suspend fun insertCharacter(vararg characterData: CardCharacter)

    @Query("SELECT * FROM CardCharacter WHERE charaIndex = :monIndex AND cardId = :dimId LIMIT 1")
    fun getCharacterByMonIndex(monIndex: Int, dimId: Long): CardCharacter

    @Insert
    suspend fun insertSprite(vararg sprite: Sprite)

    @Query(
        """
        SELECT 
            d.cardId as cardId,
            c.charaIndex as charId,
            c.stage as stage,
            c.attribute as attribute
        FROM CardCharacter c
        JOIN UserCharacter uc ON c.id = uc.charId
        JOIN Card d ON c.cardId = d.id
        WHERE c.id = :charId
    """
    )
    suspend fun getCharacterInfo(charId: Long): CharacterDtos.CardCharacterInfo

    @Query(
        """
        INSERT INTO CardTransformations (charaId, requiredVitals, requiredTrophies, requiredBattles, requiredWinRate, changeTimerHours, requiredAdventureLevelCompleted, toCharaId)
        SELECT
            (SELECT id FROM CardCharacter WHERE charaIndex = :fromChraraIndex AND cardId = :cardId),
            :requiredVitals,
            :requiredTrophies,
            :requiredBattles,
            :requiredWinRate,
            :changeTimerHours,
            :requiredAdventureLevelCompleted,
            (SELECT id FROM CardCharacter WHERE charaIndex = :toChraraIndex AND cardId = :cardId)
    """
    )
    suspend fun insertPossibleTransformation(
        fromChraraIndex: Int,
        toChraraIndex: Int,
        cardId: Long,
        requiredVitals: Int,
        requiredTrophies: Int,
        requiredBattles: Int,
        changeTimerHours: Int,
        requiredWinRate: Int,
        requiredAdventureLevelCompleted: Int
    )

    @Query(
        """
        SELECT 
            pt.charaId as fromCharaId,
            pt.toCharaId as charaId,
            s.spriteIdle1 as spriteIdle,
            s.width as spriteWidth,
            s.height as spriteHeight,
            d.discoveredOn as discoveredOn,
            pt.requiredTrophies as requiredTrophies,
            pt.requiredVitals as requiredVitals,
            pt.requiredBattles as requiredBattles,
            pt.requiredWinRate as requiredWinRate,
            pt.changeTimerHours as changeTimerHours,
            pt.requiredAdventureLevelCompleted as requiredAdventureLevelCompleted
        FROM
            CardTransformations pt
        JOIN CardCharacter c on pt.toCharaId = c.id
        JOIN Sprite s ON s.id = c.spriteId
        LEFT JOIN Dex d ON d.id = pt.toCharaId
        WHERE
            c.cardId = :cardId
    """
    )
    suspend fun getEvolutionRequirementsForCard(cardId: Long): List<CharacterDtos.EvolutionRequirementsWithSpritesAndObtained>
}