package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.dtos.CardDtos
import com.github.nacabaro.vbhelper.dtos.CharacterDtos
import kotlinx.coroutines.flow.Flow

class DexRepository (
    private val db: AppDatabase
) {
    fun getAllDims(): Flow<List<CardDtos.CardProgress>> {
        return db.dexDao().getCardsWithProgress()
    }

    fun getCharactersByCardId(cardId: Long): Flow<List<CharacterDtos.CardCharaProgress>> {
        return db.dexDao().getSingleCardProgress(cardId)
    }

    fun getCharacterPossibleTransformations(characterId: Long): Flow<List<CharacterDtos.EvolutionRequirementsWithSpritesAndObtained>> {
        return db.characterDao().getEvolutionRequirementsForCard(characterId)
    }

    fun getCharacterPossibleFusions(characterId: Long): Flow<List<CharacterDtos.FusionsWithSpritesAndObtained>> {
        return db.cardFusionsDao().getFusionsForCharacter(characterId)
    }
}