package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.characters.Character
import com.github.nacabaro.vbhelper.domain.characters.Sprite
import com.github.nacabaro.vbhelper.dtos.CharacterDtos

@Dao
interface CharacterDao {
    @Insert
    suspend fun insertCharacter(vararg characterData: Character)

    @Query("SELECT * FROM Character WHERE monIndex = :monIndex AND dimId = :dimId LIMIT 1")
    fun getCharacterByMonIndex(monIndex: Int, dimId: Long): Character

    @Insert
    suspend fun insertSprite(vararg sprite: Sprite)

    @Query(
        """
        SELECT 
            d.cardId as cardId,
            c.monIndex as charId,
            c.stage as stage,
            c.attribute as attribute
        FROM Character c
        JOIN UserCharacter uc ON c.id = uc.charId
        JOIN Card d ON c.dimId = d.id
        WHERE uc.id = :charId
    """
    )
    suspend fun getCharacterInfo(charId: Long): CharacterDtos.CardCharacterInfo

    @Query("""
        INSERT INTO TransformationHistory(monId, stageId, transformationDate)
        VALUES 
            (:monId, 
            (SELECT id FROM Character WHERE monIndex = :stage AND dimId = :dimId),
            :transformationDate)
    """)
    fun insertTransformation(monId: Long, stage: Int, dimId: Long, transformationDate: Long)

    @Query("""
        INSERT INTO VitalsHistory(charId, date, vitalPoints)
        VALUES 
            (:charId, 
            (:date),
            :vitalPoints)
    """)
    fun insertVitals(charId: Long, date: Long, vitalPoints: Int)

}