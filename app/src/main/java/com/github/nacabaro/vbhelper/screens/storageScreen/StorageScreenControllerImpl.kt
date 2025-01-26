package com.github.nacabaro.vbhelper.screens.storageScreen

import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.nacabaro.vbhelper.di.VBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StorageScreenControllerImpl(
    private val componentActivity: ComponentActivity
): StorageScreenController {
    private val application = componentActivity.applicationContext as VBHelper
    private val database = application.container.db

    override fun setActive(characterId: Long, onCompletion: () -> Unit) {
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            database.userCharacterDao().clearActiveCharacter()
            database.userCharacterDao().setActiveCharacter(characterId)

            componentActivity.runOnUiThread {
                Toast.makeText(
                    componentActivity,
                    "Active character updated!",
                    Toast.LENGTH_SHORT
                ).show()
                onCompletion()
            }
        }
    }
}