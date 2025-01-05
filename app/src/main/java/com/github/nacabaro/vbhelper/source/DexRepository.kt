package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.Character
import com.github.nacabaro.vbhelper.domain.Dim
import kotlinx.coroutines.flow.Flow

class DexRepository (
    private val db: AppDatabase
) {
    suspend fun getAllDims(): List<Dim> {
        return db.dimDao().getAllDims()
    }

    suspend fun getCharactersByDimId(dimId: Int): List<Character> {
        return db.characterDao().getCharacterByDimId(dimId)
    }
}