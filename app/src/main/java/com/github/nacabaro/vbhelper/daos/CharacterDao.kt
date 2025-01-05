package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.Character
import com.github.nacabaro.vbhelper.domain.Sprites
import org.w3c.dom.CharacterData

@Dao
interface CharacterDao {
    @Insert
    suspend fun insertCharacter(vararg characterData: Character)

    @Query("SELECT * FROM Character")
    suspend fun getAllCharacters(): List<Character>

    @Query("SELECT * FROM Character WHERE dimId = :dimId")
    suspend fun getCharacterByDimId(dimId: Int): List<Character>

    @Insert
    suspend fun insertSprite(vararg sprite: Sprites)

    @Query("SELECT * FROM Sprites")
    suspend fun getAllSprites(): List<Sprites>
}