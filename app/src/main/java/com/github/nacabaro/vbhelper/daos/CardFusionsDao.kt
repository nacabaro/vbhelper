package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CardFusionsDao {
    @Query("""
        INSERT INTO
            CardFusions (
                fromCharaId,
                attribute1Fusion,
                attribute2Fusion,
                attribute3Fusion,
                attribute4Fusion
            )
        SELECT
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :fromCharaId),
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :toCharaIdAttr1),
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :toCharaIdAttr2),
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :toCharaIdAttr3),
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :toCharaIdAttr4)            
    """)
    suspend fun insertNewFusion(
        cardId: Long,
        fromCharaId: Int,
        toCharaIdAttr1: Int,
        toCharaIdAttr2: Int,
        toCharaIdAttr3: Int,
        toCharaIdAttr4: Int
    )
}