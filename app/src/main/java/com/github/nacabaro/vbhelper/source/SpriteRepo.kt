package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.characters.Sprite

class SpriteRepo (
    private val db: AppDatabase
) {
    suspend fun getAllSprites(): List<Sprite> {
        return db.characterDao().getAllSprites()
    }
}
