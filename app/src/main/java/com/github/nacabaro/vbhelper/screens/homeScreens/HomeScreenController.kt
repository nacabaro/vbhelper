package com.github.nacabaro.vbhelper.screens.homeScreens

interface HomeScreenController {
    fun didAdventureMissionsFinish(onCompletion: (Boolean) -> Unit)
}