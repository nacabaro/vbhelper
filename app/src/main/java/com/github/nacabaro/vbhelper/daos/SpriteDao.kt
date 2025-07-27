package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.characters.Sprite

@Dao
interface SpriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSprite(sprite: Sprite): Long

    @Query("SELECT * FROM Sprite")
    suspend fun getAllSprites(): List<Sprite>
}