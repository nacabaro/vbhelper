package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import kotlinx.coroutines.flow.Flow

@Dao
interface CardFusionsDao {
    @Query("""
        INSERT INTO
            CardFusions (
                fromCharaId,
                attribute,
                toCharaId
            )
        SELECT
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :fromCharaId),
            :attribute,
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :toCharaId)
    """)
    suspend fun insertNewFusion(
        cardId: Long,
        fromCharaId: Int,
        attribute: NfcCharacter.Attribute,
        toCharaId: Int
    )

    @Query("""
        SELECT 
            cf.toCharaId as charaId,
            cf.fromCharaId as fromCharaId,
            s.spriteIdle1 as spriteIdle,
            cc.attribute as attribute,
            s.width as spriteWidth,
            s.height as spriteHeight,
            d.discoveredOn as discoveredOn,
            cf.attribute as fusionAttribute
        FROM CardFusions cf
        JOIN CardCharacter cc ON cc.id = cf.toCharaId
        JOIN Sprite s ON s.id = cc.id
        LEFT JOIN Dex d ON d.id = cc.id
        WHERE cf.fromCharaId = :charaId
        ORDER BY cc.charaIndex
    """)
    fun getFusionsForCharacter(charaId: Long): Flow<List<CharacterDtos.FusionsWithSpritesAndObtained>>
}