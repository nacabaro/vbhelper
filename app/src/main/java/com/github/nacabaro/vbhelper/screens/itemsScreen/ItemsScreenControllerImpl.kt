package com.github.nacabaro.vbhelper.screens.itemsScreen

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.domain.items.Items
import com.github.nacabaro.vbhelper.utils.DeviceType

class ItemsScreenControllerImpl (
    private val database: AppDatabase
): ItemsScreenController {
    private enum class ItemTypes(val id: Int) {
        PPTraining(1),
        HPTraining(2),
        APTraining(3),
        BPTraining(4),
        AllTraining(5),
        EvoTimer(6),
        LimitTimer(7),
        Vitals(8)
    }

    override suspend fun applyItem(item: Items, characterId: Long) {
        var characterData = database.userCharacterDao().getCharacter(characterId)
        var beCharacterData: BECharacterData
        var vbCharacterData: VBCharacterData
        if (characterData.characterType == DeviceType.BEDevice) {
            beCharacterData = database.userCharacterDao().getBeData(characterId)
        } else {
            TODO("Not implemented")
            //vbCharacterData = database.userCharacterDao().getVbData(characterId)
        }

        if (item.itemIcon in 1 .. 5 && characterData.characterType == DeviceType.BEDevice) {
            beCharacterData.itemType = item.itemIcon
            beCharacterData.itemMultiplier = 3
            beCharacterData.itemRemainingTime = item.itemLength

            database
                .userCharacterDao()
                .updateBECharacterData(beCharacterData)

        } else if (item.itemIcon == ItemTypes.EvoTimer.id) {
            characterData.transformationCountdown = item.itemLength
            if (characterData.transformationCountdown < 0) {
                characterData.transformationCountdown = 0
            }

            database
                .userCharacterDao()
                .updateCharacter(characterData)

        } else if (item.itemIcon == ItemTypes.LimitTimer.id) {
            beCharacterData.remainingTrainingTimeInMinutes = item.itemLength
            if (beCharacterData.remainingTrainingTimeInMinutes > 6000) {
                beCharacterData.remainingTrainingTimeInMinutes = 6000
            }

            database
                .userCharacterDao()
                .updateBECharacterData(beCharacterData)

        } else if (item.itemIcon == ItemTypes.Vitals.id) {
            characterData.vitalPoints = item.itemLength
            if (characterData.vitalPoints < 0) {
                characterData.vitalPoints = 0
            } else if (characterData.vitalPoints > 9999) {
                characterData.vitalPoints = 9999
            }

            database
                .userCharacterDao()
                .updateCharacter(characterData)
        }

        consumeItem(item.id)
    }

    private fun consumeItem(itemId: Long) {
        database
            .itemDao()
            .useItem(itemId)
    }
}