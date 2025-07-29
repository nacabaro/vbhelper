package com.github.nacabaro.vbhelper.screens.scanScreen.converters

import androidx.activity.ComponentActivity
import com.github.cfogrady.vbnfc.be.BENfcCharacter
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vbnfc.vb.VBNfcCharacter
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.domain.card.CardProgress
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.SpecialMissions
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.utils.DeviceType
import java.util.GregorianCalendar

class FromNfcConverter (
    componentActivity: ComponentActivity
) {
    private val application = componentActivity.applicationContext as VBHelper
    private val database = application.container.db



    fun addCharacter(nfcCharacter: NfcCharacter): String {
        val cardData = database
            .cardDao()
            .getDimById(nfcCharacter.dimId.toInt())

        if (cardData == null)
            return "Card not found"

        val cardCharData = database
            .characterDao()
            .getCharacterByMonIndex(nfcCharacter.charIndex.toInt(), cardData.id)

        updateCardProgress(nfcCharacter, cardData)

        val characterData = UserCharacter(
            charId = cardCharData.id,
            ageInDays = nfcCharacter.ageInDays.toInt(),
            mood = nfcCharacter.mood.toInt(),
            vitalPoints = nfcCharacter.vitalPoints.toInt(),
            transformationCountdown = nfcCharacter.transformationCountdownInMinutes.toInt(),
            injuryStatus = nfcCharacter.injuryStatus,
            trophies = nfcCharacter.trophies.toInt(),
            currentPhaseBattlesWon = nfcCharacter.currentPhaseBattlesWon.toInt(),
            currentPhaseBattlesLost = nfcCharacter.currentPhaseBattlesLost.toInt(),
            totalBattlesWon = nfcCharacter.totalBattlesWon.toInt(),
            totalBattlesLost = nfcCharacter.totalBattlesLost.toInt(),
            activityLevel = nfcCharacter.activityLevel.toInt(),
            heartRateCurrent = nfcCharacter.heartRateCurrent.toInt(),
            characterType = when (nfcCharacter) {
                is BENfcCharacter -> DeviceType.BEDevice
                else -> DeviceType.VBDevice
            },
            isActive = true
        )

        updateCardProgress(cardData, nfcCharacter)

        database
            .userCharacterDao()
            .clearActiveCharacter()

        val characterId: Long = database
            .userCharacterDao()
            .insertCharacterData(characterData)

        if (nfcCharacter is BENfcCharacter) {
            addBeCharacterToDatabase(
                characterId = characterId,
                nfcCharacter = nfcCharacter
            )
        } else if (nfcCharacter is VBNfcCharacter) {
            addVbCharacterToDatabase(
                characterId = characterId,
                nfcCharacter = nfcCharacter
            )
        }

        addTransformationHistoryToDatabase(
            characterId = characterId,
            nfcCharacter = nfcCharacter,
            dimData = cardData
        )

        return "Done reading character!"
    }



    private fun updateCardProgress(
        nfcCharacter: NfcCharacter,
        cardData: Card
    ) {
        val currentCardProgress = CardProgress(
            cardId = cardData.cardId,
            currentStage = nfcCharacter.nextAdventureMissionStage.toInt(),
            unlocked = nfcCharacter.nextAdventureMissionStage.toInt() > cardData.stageCount
        )

        database
            .cardProgressDao()
            .updateDimProgress(currentCardProgress)
    }



    private fun updateCardProgress(
        cardData: Card,
        nfcCharacter: NfcCharacter,
    ) {
        val cardProgress = CardProgress(
            cardId = cardData.cardId,
            currentStage = nfcCharacter.nextAdventureMissionStage.toInt(),
            unlocked = nfcCharacter.nextAdventureMissionStage.toInt() > cardData.stageCount
        )

        database
            .cardProgressDao()
            .updateDimProgress(cardProgress)
    }



    private fun addVbCharacterToDatabase(
        characterId: Long,
        nfcCharacter: VBNfcCharacter
    ) {
        val extraCharacterData = VBCharacterData(
            id = characterId,
            generation = nfcCharacter.generation.toInt(),
            totalTrophies = nfcCharacter.totalTrophies.toInt()
        )

        database
            .userCharacterDao()
            .insertVBCharacterData(extraCharacterData)

        addSpecialMissionsToDatabase(nfcCharacter, characterId)

        addVitalsHistoryToDatabase(characterId, nfcCharacter)
    }



    private fun addSpecialMissionsToDatabase(
        nfcCharacter: VBNfcCharacter,
        characterId: Long
    ) {
        val specialMissionsWatch = nfcCharacter.specialMissions
        val specialMissionsDb = specialMissionsWatch.map { item ->
            SpecialMissions(
                characterId = characterId,
                goal = item.goal.toInt(),
                watchId = item.id.toInt(),
                progress = item.progress.toInt(),
                status = item.status,
                timeElapsedInMinutes = item.timeElapsedInMinutes.toInt(),
                timeLimitInMinutes = item.timeLimitInMinutes.toInt(),
                missionType = item.type,
            )
        }

        database
            .userCharacterDao()
            .insertSpecialMissions(*specialMissionsDb.toTypedArray())
    }



    private fun addBeCharacterToDatabase(
        characterId: Long,
        nfcCharacter: BENfcCharacter
    ) {
        val extraCharacterData = BECharacterData(
            id = characterId,
            trainingHp = nfcCharacter.trainingHp.toInt(),
            trainingAp = nfcCharacter.trainingAp.toInt(),
            trainingBp = nfcCharacter.trainingBp.toInt(),
            remainingTrainingTimeInMinutes = nfcCharacter.remainingTrainingTimeInMinutes.toInt(),
            itemEffectActivityLevelValue = nfcCharacter.itemEffectActivityLevelValue.toInt(),
            itemEffectMentalStateValue = nfcCharacter.itemEffectMentalStateValue.toInt(),
            itemEffectMentalStateMinutesRemaining = nfcCharacter.itemEffectMentalStateMinutesRemaining.toInt(),
            itemEffectActivityLevelMinutesRemaining = nfcCharacter.itemEffectActivityLevelMinutesRemaining.toInt(),
            itemEffectVitalPointsChangeValue = nfcCharacter.itemEffectVitalPointsChangeValue.toInt(),
            itemEffectVitalPointsChangeMinutesRemaining = nfcCharacter.itemEffectVitalPointsChangeMinutesRemaining.toInt(),
            abilityRarity = nfcCharacter.abilityRarity,
            abilityType = nfcCharacter.abilityType.toInt(),
            abilityBranch = nfcCharacter.abilityBranch.toInt(),
            abilityReset = nfcCharacter.abilityReset.toInt(),
            rank = nfcCharacter.abilityReset.toInt(),
            itemType = nfcCharacter.itemType.toInt(),
            itemMultiplier = nfcCharacter.itemMultiplier.toInt(),
            itemRemainingTime = nfcCharacter.itemRemainingTime.toInt(),
            otp0 = "", //nfcCharacter.value!!.otp0.toString(),
            otp1 = "", //nfcCharacter.value!!.otp1.toString(),
            minorVersion = nfcCharacter.characterCreationFirmwareVersion.minorVersion.toInt(),
            majorVersion = nfcCharacter.characterCreationFirmwareVersion.majorVersion.toInt(),
        )

        database
            .userCharacterDao()
            .insertBECharacterData(extraCharacterData)
    }



    private fun addVitalsHistoryToDatabase(
        characterId: Long,
        nfcCharacter: NfcCharacter
    ) {
        val vitalsHistoryWatch = nfcCharacter.vitalHistory
        vitalsHistoryWatch.map { item ->
            val date = GregorianCalendar(
                item.year.toInt(),
                item.month.toInt(),
                item.day.toInt()
            )
                .time
                .time

            database
                .characterDao()
                .insertVitals(
                    characterId,
                    date,
                    item.vitalsGained.toInt()
                )
        }
    }


    private fun addTransformationHistoryToDatabase(
        characterId: Long,
        nfcCharacter: NfcCharacter,
        dimData: Card
    ) {
        val transformationHistoryWatch = nfcCharacter.transformationHistory
        transformationHistoryWatch.map { item ->
            if (item.toCharIndex.toInt() != 255) {
                val date = GregorianCalendar(
                    item.year.toInt(),
                    item.month.toInt(),
                    item.day.toInt()
                )
                    .time
                    .time

                database
                    .characterDao()
                    .insertTransformation(
                        characterId,
                        item.toCharIndex.toInt(),
                        dimData.id,
                        date
                    )

                database
                    .dexDao()
                    .insertCharacter(
                        item.toCharIndex.toInt(),
                        dimData.id,
                        date
                    )
            }
        }
    }
}