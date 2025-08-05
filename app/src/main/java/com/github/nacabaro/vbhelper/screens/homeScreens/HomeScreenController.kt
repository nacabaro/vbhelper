package com.github.nacabaro.vbhelper.screens.homeScreens

import com.github.nacabaro.vbhelper.dtos.ItemDtos

interface HomeScreenController {
    fun didAdventureMissionsFinish(onCompletion: (Boolean) -> Unit)
    fun clearSpecialMission(missionId: Long, onCleared: (ItemDtos.PurchasedItem) -> Unit)
}