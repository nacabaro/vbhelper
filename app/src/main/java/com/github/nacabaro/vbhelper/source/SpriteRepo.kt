package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.Sprites

class SpriteRepo (
    private val db: AppDatabase
) {
    suspend fun getAllSprites(): List<Sprites> {
        return db.characterDao().getAllSprites()
    }
}
