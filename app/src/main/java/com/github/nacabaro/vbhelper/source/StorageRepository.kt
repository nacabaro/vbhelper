package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryCharacterData

class StorageRepository (
    private val db: AppDatabase
) {
    suspend fun getAllCharacters(): List<TemporaryCharacterData> {
        return db.temporaryMonsterDao().getAllCharacters()
    }

    suspend fun getSingleCharacter(id: Long): TemporaryCharacterData {
        return db.temporaryMonsterDao().getCharacter(id)
    }
}