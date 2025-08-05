package com.github.nacabaro.vbhelper.screens.itemsScreen

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vbnfc.vb.SpecialMission
import com.github.nacabaro.vbhelper.domain.device_data.SpecialMissions
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
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
        Vitals(8),
        Step8k(9),
        Step4k(10),
        Vitals1000(11),
        Vitals250(12),
        Battle20(13),
        Battle5(14),
        Win10(15),
        Win4(16)
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
                var beCharacterData: BECharacterData? = null
                var vbCharacterData: VBCharacterData? = null

                if (characterData.characterType == DeviceType.BEDevice) {
                    beCharacterData = database.userCharacterDao().getBeData(characterId)
                } else if (characterData.characterType == DeviceType.VBDevice) {
                    vbCharacterData = database.userCharacterDao().getVbData(characterId)
                }

                if (
                    item.itemIcon in 1 .. 5 &&
                    characterData.characterType == DeviceType.BEDevice &&
                    beCharacterData != null
                ) {
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

                } else if (
                    item.itemIcon == ItemTypes.LimitTimer.id &&
                    characterData.characterType == DeviceType.BEDevice &&
                    beCharacterData != null
                ) {
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

                } else if (item.itemIcon in ItemTypes.Step8k.id  .. ItemTypes.Win4.id &&
                    characterData.characterType == DeviceType.VBDevice &&
                    vbCharacterData != null
                ) {
                    applySpecialMission(item.itemIcon, item.itemLength, characterId)
                }

                consumeItem(item.id)

                context.runOnUiThread {
                    onCompletion()
                }
            }
        }
    }

    private suspend fun applySpecialMission(itemIcon: Int, itemLength: Int, characterId: Long) {
        // Hello, it's me, naca! No! I don't like this, I'll see how I can improve it later on...
        val specialMissionType = when (itemIcon) {
            ItemTypes.Step8k.id -> SpecialMission.Type.STEPS
            ItemTypes.Step4k.id -> SpecialMission.Type.STEPS
            ItemTypes.Vitals1000.id -> SpecialMission.Type.VITALS
            ItemTypes.Vitals250.id -> SpecialMission.Type.VITALS
            ItemTypes.Battle20.id -> SpecialMission.Type.BATTLES
            ItemTypes.Battle5.id -> SpecialMission.Type.BATTLES
            ItemTypes.Win10.id -> SpecialMission.Type.WINS
            ItemTypes.Win4.id -> SpecialMission.Type.WINS
            else -> SpecialMission.Type.NONE
        }

        val specialMissionGoal = when (itemIcon) {
            ItemTypes.Step8k.id -> 8000
            ItemTypes.Step4k.id -> 4000
            ItemTypes.Vitals1000.id -> 1000
            ItemTypes.Vitals250.id -> 250
            ItemTypes.Battle20.id -> 20
            ItemTypes.Battle5.id -> 5
            ItemTypes.Win10.id -> 10
            ItemTypes.Win4.id -> 4
            else -> 0
        }

        val availableSpecialMissions = database
            .userCharacterDao()
            .getSpecialMissions(characterId)

        var firstUnavailableMissionSlot: Long = 0
        var watchId = 0

        for ((index, mission) in availableSpecialMissions.withIndex()) {
            if (
                mission.status == SpecialMission.Status.UNAVAILABLE
            ) {
                firstUnavailableMissionSlot = mission.id
                watchId = index + 1
            }
        }

        val newSpecialMission = SpecialMissions(
            id = firstUnavailableMissionSlot,
            characterId = characterId,
            missionType = specialMissionType,
            goal = specialMissionGoal,
            timeLimitInMinutes = itemLength,
            watchId = watchId,
            status = SpecialMission.Status.AVAILABLE,
            progress = 0,
            timeElapsedInMinutes = 0
        )

        database
            .userCharacterDao()
            .insertSpecialMissions(newSpecialMission)
    }

    private suspend fun getItem(itemId: Long): ItemDtos.ItemsWithQuantities {
        return database
            .itemDao()
            .getItem(itemId)
    }

    private suspend fun consumeItem(itemId: Long) {
        database
            .itemDao()
            .useItem(itemId)
    }
}