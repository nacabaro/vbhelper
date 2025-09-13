package com.github.nacabaro.vbhelper.screens.scanScreen.converters

import android.util.Log
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
import com.github.nacabaro.vbhelper.domain.device_data.VitalsHistory
import com.github.nacabaro.vbhelper.utils.DeviceType
import java.util.GregorianCalendar

class FromNfcConverter (
    componentActivity: ComponentActivity
) {
    private val application = componentActivity.applicationContext as VBHelper
    private val database = application.container.db
    
    
    fun addCharacterUsingCard(
        nfcCharacter: NfcCharacter,
        cardId: Long
    ): String {
        val cardData = database
            .cardDao()
            .getCardById(cardId)

        if (cardData == null) {
            return "Card not found"
        }

        return insertCharacter(nfcCharacter, cardData)
    }
    

    fun addCharacter(
        nfcCharacter: NfcCharacter,
        onMultipleCards: (List<Card>, NfcCharacter) -> Unit
    ): String {
        val appReservedCardId = nfcCharacter
            .appReserved2[0].toLong()

        var cardData: Card? =  null

        if (appReservedCardId != 0L) {
            val fetchedCard = database
                .cardDao()
                .getCardById(appReservedCardId)

            if (fetchedCard == null) {
                return "Card not found"
            } else if (fetchedCard.cardId == nfcCharacter.dimId.toInt()) {
                cardData = fetchedCard
            }
        }

        if (cardData == null) {
            val allCards = database
                .cardDao()
                .getCardByCardId(nfcCharacter.dimId.toInt())

            if (allCards.isEmpty())
                return "Card not found"

            if (allCards.size > 1) {
                onMultipleCards(allCards, nfcCharacter)
                return "Multiple cards found"
            }

            cardData = allCards[0]
        }

        return insertCharacter(nfcCharacter, cardData)
    }



    private fun insertCharacter(
        nfcCharacter: NfcCharacter,
        cardData: Card
    ): String {
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

        addVitalsHistoryToDatabase(
            characterId = characterId,
            nfcCharacter = nfcCharacter
        )

        return "Done reading character!"
    }
    


    private fun updateCardProgress(
        nfcCharacter: NfcCharacter,
        cardData: Card
    ) {
        database
            .cardProgressDao()
            .updateCardProgress(
                currentStage = nfcCharacter.nextAdventureMissionStage.toInt(),
                cardId = cardData.id,
                unlocked = nfcCharacter.nextAdventureMissionStage.toInt() > cardData.stageCount,
            )
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
        val vitalsHistory = vitalsHistoryWatch.map { historyElement ->
            Log.d("VitalsHistory", "${historyElement.year.toInt()} ${historyElement.month.toInt()} ${historyElement.day.toInt()}")
            VitalsHistory(
                charId = characterId,
                year = historyElement.year.toInt(),
                month = historyElement.month.toInt(),
                day = historyElement.day.toInt(),
                vitalPoints = historyElement.vitalsGained.toInt()
            )
        }

        database
            .userCharacterDao()
            .insertVitals(*vitalsHistory.toTypedArray())
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
                    .userCharacterDao()
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