package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.dtos.CharacterDtos

class StorageRepository (
    private val db: AppDatabase
) {
    suspend fun getAllCharacters(): List<CharacterDtos.CharacterWithSprites> {
        return db.userCharacterDao().getAllCharacters()
    }

    suspend fun getSingleCharacter(id: Long): CharacterDtos.CharacterWithSprites {
        return db.userCharacterDao().getCharacterWithSprites(id)
    }

    suspend fun getCharacterBeData(id: Long): BECharacterData {
        return db.userCharacterDao().getBeData(id)
    }

    fun getTransformationHistory(characterId: Long): List<CharacterDtos.TransformationHistory>? {
        return db.userCharacterDao().getTransformationHistory(characterId)
    }

    suspend fun getCharacterData(id: Long): CharacterDtos.DiMInfo {
        return db.characterDao().getCharacterInfo(id)
    }

    suspend fun getActiveCharacter(): CharacterDtos.CharacterWithSprites? {
        return db.userCharacterDao().getActiveCharacter()
    }

    fun deleteCharacter(id: Long) {
        return db.userCharacterDao().deleteCharacterById(id)
    }

    suspend fun getAdventureCharacters(): List<CharacterDtos.AdventureCharacterWithSprites> {
        return db.adventureDao().getAdventureCharacters()
    }
}