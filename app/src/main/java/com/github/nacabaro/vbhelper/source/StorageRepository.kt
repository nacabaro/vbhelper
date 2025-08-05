package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.SpecialMissions
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import com.github.nacabaro.vbhelper.dtos.ItemDtos

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

    suspend fun getTransformationHistory(characterId: Long): List<CharacterDtos.TransformationHistory>? {
        return db.userCharacterDao().getTransformationHistory(characterId)
    }

    suspend fun getCharacterVbData(id: Long): VBCharacterData {
        return db.userCharacterDao().getVbData(id)
    }

    suspend fun getSpecialMissions(id: Long): List<SpecialMissions> {
        return db.userCharacterDao().getSpecialMissions(id)
    }

    suspend fun getItem(id: Long): ItemDtos.ItemsWithQuantities {
        return db.itemDao().getItem(id)
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

    suspend fun getBEBEmCharacters(): List<CharacterDtos.CharacterWithSprites> {
        return db.userCharacterDao().getBEBemCharacters()
    }

    suspend fun getVBCharacters(): List<CharacterDtos.CharacterWithSprites> {
        return db.userCharacterDao().getVBDimCharacters()
    }
}