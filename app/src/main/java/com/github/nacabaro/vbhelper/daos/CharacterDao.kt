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
        WHERE c.id = :charId
    """
    )
    suspend fun getCharacterInfo(charId: Long): CharacterDtos.CardCharacterInfo
}