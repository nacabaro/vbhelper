package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.dtos.CharacterDtos

class StorageRepository (
    private val db: AppDatabase
) {
    suspend fun getAllCharacters(): List<CharacterDtos.CharacterWithSprites> {
        return db.userCharacterDao().getAllCharacters()
    }

    suspend fun getSingleCharacter(id: Long): UserCharacter {
        return db.userCharacterDao().getCharacter(id)
    }
}