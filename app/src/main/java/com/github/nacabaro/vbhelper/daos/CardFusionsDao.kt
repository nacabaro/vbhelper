package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query

@Dao
interface CardFusionsDao {
    @Query(
        """
        INSERT INTO
            CardFusions (
                fromCharaId,
                toVirusFusion,
                toDataFusion,
                toVaccineFusion,
                toFreeFusion
            )
        SELECT
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :fromCharaId),
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :toCharaIdVirus),
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :toCharaIdData),
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :toCharaIdVaccine),
            (SELECT id FROM CardCharacter WHERE cardId = :cardId AND charaIndex = :toCharaIdFree)            
    """
    )
    suspend fun insertNewFusion(
        cardId: Long,
        fromCharaId: Int,
        toCharaIdVirus: Int,
        toCharaIdData: Int,
        toCharaIdVaccine: Int,
        toCharaIdFree: Int
    )
}