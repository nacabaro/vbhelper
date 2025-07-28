package com.github.nacabaro.vbhelper

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.github.nacabaro.vbhelper.navigation.AppNavigation
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.navigation.AppNavigationHandlers
import com.github.nacabaro.vbhelper.screens.homeScreens.HomeScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.itemsScreen.ItemsScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.settingsScreen.SettingsScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.adventureScreen.AdventureScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.spriteViewer.SpriteViewerControllerImpl
import com.github.nacabaro.vbhelper.screens.storageScreen.StorageScreenControllerImpl
import com.github.nacabaro.vbhelper.ui.theme.VBHelperTheme


class MainActivity : ComponentActivity() {
    private val onActivityLifecycleListeners = HashMap<String, ActivityLifecycleListener>()

    private fun registerActivityLifecycleListener(key: String, activityLifecycleListener: ActivityLifecycleListener) {
        if( onActivityLifecycleListeners[key] != null) {
            throw IllegalStateException("Key is already in use")
        }
        onActivityLifecycleListeners[key] = activityLifecycleListener
    }

    private fun unregisterActivityLifecycleListener(key: String) {
        onActivityLifecycleListeners.remove(key)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val application = applicationContext as VBHelper
        val scanScreenController = ScanScreenControllerImpl(
            application.container.dataStoreSecretsRepository.secretsFlow,
            this,
            this::registerActivityLifecycleListener,
            this::unregisterActivityLifecycleListener
        )
        val settingsScreenController = SettingsScreenControllerImpl(this)
        val itemsScreenController = ItemsScreenControllerImpl(this)
        val adventureScreenController = AdventureScreenControllerImpl(this)
        val storageScreenController = StorageScreenControllerImpl(this)
        val homeScreenController = HomeScreenControllerImpl(this)
        val spriteViewerController = SpriteViewerControllerImpl(this)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            VBHelperTheme {
                MainApplication(
                    scanScreenController = scanScreenController,
                    settingsScreenController = settingsScreenController,
                    itemsScreenController = itemsScreenController,
                    adventureScreenController = adventureScreenController,
                    homeScreenController = homeScreenController,
                    storageScreenController = storageScreenController,
                    spriteViewerController = spriteViewerController
                )
            }
        }

        Log.i("MainActivity", "Activity onCreated")
    }

    override fun onPause() {
        super.onPause()
        Log.i("MainActivity", "onPause")
        for(activityListener in onActivityLifecycleListeners) {
            activityListener.value.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("MainActivity", "Resume")
        for(activityListener in onActivityLifecycleListeners) {
            activityListener.value.onResume()
        }
    }

    @Composable
    private fun MainApplication(
        scanScreenController: ScanScreenControllerImpl,
        settingsScreenController: SettingsScreenControllerImpl,
        itemsScreenController: ItemsScreenControllerImpl,
        adventureScreenController: AdventureScreenControllerImpl,
        storageScreenController: StorageScreenControllerImpl,
        homeScreenController: HomeScreenControllerImpl,
        spriteViewerController: SpriteViewerControllerImpl
        ) {
        AppNavigation(
            applicationNavigationHandlers = AppNavigationHandlers(
                settingsScreenController,
                scanScreenController,
                itemsScreenController,
                adventureScreenController,
                storageScreenController,
                homeScreenController,
                spriteViewerController
            )
        )
    }
}
