package com.github.nacabaro.vbhelper.screens.settingsScreen.controllers

import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.utils.DeviceType

/**
 * Builds a character from a card entry, for recovering one that was lost.
 * Stats that cannot be known are given neutral defaults.
 */
class CreateCharacterController(
    private val database: AppDatabase
) {
    data class Stats(
        val ageInDays: Int = 5,
        val vitalPoints: Int = 0,
        val trophies: Int = 0,
        val battlesWon: Int = 0,
        val battlesLost: Int = 0
    )

    suspend fun createCharacter(card: Card, cardCharacterId: Long, stats: Stats): Long {
        val userDao = database.userCharacterDao()

        val characterType = if (card.isBEm) DeviceType.BEDevice else DeviceType.VBDevice

        userDao.clearActiveCharacter()

        val characterId = userDao.insertCharacterData(
            UserCharacter(
                charId = cardCharacterId,
                ageInDays = stats.ageInDays,
                mood = 50,
                vitalPoints = stats.vitalPoints,
                transformationCountdown = 1440,
                injuryStatus = NfcCharacter.InjuryStatus.None,
                trophies = stats.trophies,
                currentPhaseBattlesWon = stats.battlesWon,
                currentPhaseBattlesLost = stats.battlesLost,
                totalBattlesWon = stats.battlesWon,
                totalBattlesLost = stats.battlesLost,
                activityLevel = 0,
                heartRateCurrent = 0,
                characterType = characterType,
                isActive = true
            )
        )

        // A character always has its own form as the first history entry
        addEvolutionTrace(characterId, cardCharacterId)

        if (characterType == DeviceType.BEDevice) {
            userDao.insertBECharacterData(emptyBeData(characterId))
        } else {
            userDao.insertVBCharacterData(
                VBCharacterData(id = characterId, generation = 0, totalTrophies = stats.trophies)
            )
        }

        return characterId
    }

    /**
     * Writes the history of a character: the forms it must have gone through to reach
     * [cardCharacterId] (oldest first), then the form itself. Where several forms lead
     * to the same one, the lowest index is used.
     */
    fun addEvolutionTrace(characterId: Long, cardCharacterId: Long) {
        val characterDao = database.characterDao()
        val userDao = database.userCharacterDao()
        val dexDao = database.dexDao()

        val target = characterDao.getCardCharacterById(cardCharacterId)
        val chain = mutableListOf(target)
        while (true) {
            val previous = characterDao.getPreviousForms(chain.last().id)
                .firstOrNull { candidate -> chain.none { it.id == candidate.id } }
                ?: break
            chain.add(previous)
        }
        chain.reverse()

        // One minute apart keeps the order stable when the history is read back
        val start = System.currentTimeMillis() - chain.size * 60_000L
        chain.forEachIndexed { index, form ->
            val date = start + index * 60_000L
            userDao.insertTransformation(characterId, form.charaIndex, form.cardId, date)
            dexDao.insertCharacter(form.charaIndex, form.cardId, date)
        }
    }

    private suspend fun emptyBeData(characterId: Long): BECharacterData {
        // Reuse the firmware version of a character that really came from a bracelet
        val reference = database.userCharacterDao().getBECharacters()
            .firstOrNull()
            ?.let { database.userCharacterDao().getBeDataOrNull(it.id) }

        return BECharacterData(
            id = characterId,
            trainingHp = 0,
            trainingAp = 0,
            trainingBp = 0,
            remainingTrainingTimeInMinutes = 0,
            itemEffectMentalStateValue = 0,
            itemEffectMentalStateMinutesRemaining = 0,
            itemEffectActivityLevelValue = 0,
            itemEffectActivityLevelMinutesRemaining = 0,
            itemEffectVitalPointsChangeValue = 0,
            itemEffectVitalPointsChangeMinutesRemaining = 0,
            abilityRarity = NfcCharacter.AbilityRarity.None,
            abilityType = 0,
            abilityBranch = 0,
            abilityReset = 0,
            rank = 0,
            itemType = 0,
            itemMultiplier = 0,
            itemRemainingTime = 0,
            otp0 = "",
            otp1 = "",
            minorVersion = reference?.minorVersion ?: 0,
            majorVersion = reference?.majorVersion ?: 1
        )
    }
}
