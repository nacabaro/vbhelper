package com.github.nacabaro.vbhelper.screens.homeScreens

import com.github.cfogrady.vbnfc.vb.SpecialMission
import com.github.nacabaro.vbhelper.dtos.ItemDtos

interface HomeScreenController {
    fun didAdventureMissionsFinish(onCompletion: (Boolean) -> Unit)
    fun clearSpecialMission(missionId: Long, missionCompletion: SpecialMission.Status, onCleared: (ItemDtos.PurchasedItem?, Int?) -> Unit)
}