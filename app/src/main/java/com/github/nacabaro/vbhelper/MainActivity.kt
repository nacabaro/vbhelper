package com.github.nacabaro.vbhelper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.nacabaro.vbhelper.navigation.AppNavigation
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.navigation.AppNavigationHandlers
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.screens.homeScreens.HomeScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.itemsScreen.ItemsScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.settingsScreen.SettingsScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.adventureScreen.AdventureScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.cardScreen.CardScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.spriteViewer.SpriteViewerControllerImpl
import com.github.nacabaro.vbhelper.screens.storageScreen.StorageScreenControllerImpl
import com.github.nacabaro.vbhelper.ui.theme.VBHelperTheme
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers


class MainActivity : ComponentActivity() {
    private val onActivityLifecycleListeners = HashMap<String, ActivityLifecycleListener>()
    private var initialRoute: String? by mutableStateOf(null)

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
        val cardScreenController = CardScreenControllerImpl(this)

        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        initialRoute = getInitialRouteFromIntent(intent)

        setContent {
            VBHelperTheme {
                MainApplication(
                    scanScreenController = scanScreenController,
                    settingsScreenController = settingsScreenController,
                    itemsScreenController = itemsScreenController,
                    adventureScreenController = adventureScreenController,
                    homeScreenController = homeScreenController,
                    storageScreenController = storageScreenController,
                    spriteViewerController = spriteViewerController,
                    cardScreenController = cardScreenController,
                    initialRoute = initialRoute
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        initialRoute = getInitialRouteFromIntent(intent)
    }

    private fun getInitialRouteFromIntent(intent: Intent?): String? {
        if (intent == null) return null
        val data = intent.data
        if (intent.action == Intent.ACTION_VIEW && data != null) {
            val isAppAuthCallback = data.scheme == "vbhelper" && data.host == "auth"
            val isLocalhostAuthCallback =
                (data.scheme == "http" || data.scheme == "https") &&
                    (data.host == "localhost" || data.host == "127.0.0.1") &&
                    data.path?.startsWith("/authenticate") == true
            val token = data.getQueryParameter("c") ?: data.getQueryParameter("token")
            val hasAuthToken = !token.isNullOrEmpty()

            if (isAppAuthCallback || isLocalhostAuthCallback || hasAuthToken) {
                if (!token.isNullOrEmpty()) {
                    val application = applicationContext as VBHelper
                    val authRepository = com.github.nacabaro.vbhelper.battle.BattleAuthContainer(this).authRepository
                    val userId = data.getQueryParameter("userId")?.toLongOrNull()
                    
                    lifecycleScope.launch(Dispatchers.IO) {
                        authRepository.setAuthenticated(
                            isAuthenticated = true,
                            nacatechToken = token,
                            userId = userId
                        )
                    }
                }
                return NavigationItems.Battles.route
            }
        }
        return null
    }

    @Composable
    private fun MainApplication(
        scanScreenController: ScanScreenControllerImpl,
        settingsScreenController: SettingsScreenControllerImpl,
        itemsScreenController: ItemsScreenControllerImpl,
        adventureScreenController: AdventureScreenControllerImpl,
        storageScreenController: StorageScreenControllerImpl,
        homeScreenController: HomeScreenControllerImpl,
        spriteViewerController: SpriteViewerControllerImpl,
        cardScreenController: CardScreenControllerImpl,
        initialRoute: String? = null
    ) {
        AppNavigation(
            applicationNavigationHandlers = AppNavigationHandlers(
                settingsScreenController,
                scanScreenController,
                itemsScreenController,
                adventureScreenController,
                storageScreenController,
                homeScreenController,
                spriteViewerController,
                cardScreenController
            ),
            initialRoute = initialRoute
        )
    }
}
