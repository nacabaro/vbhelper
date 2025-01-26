package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import com.github.nacabaro.vbhelper.utils.DeviceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ItemsScreenControllerImpl (
    private val context: ComponentActivity,
): ItemsScreenController {
    private var database: AppDatabase

    enum class ItemTypes(val id: Int) {
        PPTraining(1),
        HPTraining(2),
        APTraining(3),
        BPTraining(4),
        AllTraining(5),
        EvoTimer(6),
        LimitTimer(7),
        Vitals(8)
    }

    init {
        val application = context.applicationContext as VBHelper
        database = application.container.db
    }

    override fun applyItem(itemId: Long, characterId: Long, onCompletion: () -> Unit) {
        context.lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val item = getItem(itemId)
                val characterData = database.userCharacterDao().getCharacter(characterId)
                val beCharacterData: BECharacterData
                //var vbCharacterData: VBCharacterData

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
                    characterData.transformationCountdown += item.itemLength
                    if (characterData.transformationCountdown < 0) {
                        characterData.transformationCountdown = 0
                    }

                    // VB does not like it when the transformationCountdown is 0
                    if (characterData.characterType == DeviceType.VBDevice &&
                        characterData.transformationCountdown <= 0 ) {
                        characterData.transformationCountdown = 1
                    }

                    database
                        .userCharacterDao()
                        .updateCharacter(characterData)

                } else if (item.itemIcon == ItemTypes.LimitTimer.id) {
                    beCharacterData.remainingTrainingTimeInMinutes += item.itemLength
                    if (beCharacterData.remainingTrainingTimeInMinutes > 6000) {
                        beCharacterData.remainingTrainingTimeInMinutes = 6000
                    }

                    database
                        .userCharacterDao()
                        .updateBECharacterData(beCharacterData)

                } else if (item.itemIcon == ItemTypes.Vitals.id) {
                    characterData.vitalPoints += item.itemLength
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

                context.runOnUiThread {
                    onCompletion()
                }
            }
        }
    }

    private fun getItem(itemId: Long): ItemDtos.ItemsWithQuantities {
        return database
            .itemDao()
            .getItem(itemId)
    }

    private fun consumeItem(itemId: Long) {
        database
            .itemDao()
            .useItem(itemId)
    }
}