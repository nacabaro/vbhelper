package com.github.nacabaro.vbhelper.screens.homeScreens

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.utils.DeviceType
import kotlinx.coroutines.flow.first

/**
 * Moves a character to its next form. The requirements are checked by the caller.
 */
class EvolutionController(
    private val database: AppDatabase
) {
    suspend fun evolve(characterId: Long, targetCardCharacterId: Long) {
        val userDao = database.userCharacterDao()
        val target = database.characterDao().getCardCharacterById(targetCardCharacterId)

        val nextEvolutions = database.characterDao()
            .getEvolutionRequirementsForCard(targetCardCharacterId)
            .first()
        val now = System.currentTimeMillis()

        database.runInTransaction {
            val character = userDao.getCharacterSync(characterId)

            character.charId = target.id
            // A new phase starts: its battle counters and, on VB, its vitals and trophies
            character.currentPhaseBattlesWon = 0
            character.currentPhaseBattlesLost = 0
            if (character.characterType == DeviceType.VBDevice) {
                character.vitalPoints = 0
                character.trophies = 0
            }

            val nextTimerMinutes = nextEvolutions.minOfOrNull { it.changeTimerHours * 60 } ?: 0
            // VB does not like it when the transformationCountdown is 0
            character.transformationCountdown =
                if (character.characterType == DeviceType.VBDevice) maxOf(nextTimerMinutes, 1)
                else nextTimerMinutes

            userDao.updateCharacter(character)

            userDao.insertTransformation(characterId, target.charaIndex, target.cardId, now)
            database.dexDao().insertCharacter(target.charaIndex, target.cardId, now)
        }
    }
}
