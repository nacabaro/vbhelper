package com.github.nacabaro.vbhelper.screens.homeScreens

import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.nacabaro.vbhelper.di.VBHelper
import kotlinx.coroutines.launch
import java.time.Instant

class HomeScreenControllerImpl(
    private val componentActivity: ComponentActivity,
): HomeScreenController {
    private val application = componentActivity.applicationContext as VBHelper
    private val database = application.container.db

    override fun didAdventureMissionsFinish(onCompletion: (Boolean) -> Unit) {
        componentActivity.lifecycleScope.launch {
            val currentTime = Instant.now().epochSecond
            val adventureCharacters = database
                .adventureDao()
                .getAdventureCharacters()

            val finishedAdventureCharacters = adventureCharacters.filter { character ->
                character.timeLeft <= currentTime
            }

            onCompletion(finishedAdventureCharacters.isNotEmpty())
        }
    }
}