package com.github.nacabaro.vbhelper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vitalwear.protos.Character
import com.github.nacabaro.vbhelper.navigation.AppNavigation
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.navigation.AppNavigationHandlers
import com.github.nacabaro.vbhelper.screens.homeScreens.HomeScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.itemsScreen.ItemsScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.settingsScreen.SettingsScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.adventureScreen.AdventureScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.cardScreen.CardScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.spriteViewer.SpriteViewerControllerImpl
import com.github.nacabaro.vbhelper.screens.storageScreen.StorageScreenControllerImpl
import com.github.nacabaro.vbhelper.source.VitalWearCharacterImporter
import com.github.nacabaro.vbhelper.ui.theme.VBHelperTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {
    private val onActivityLifecycleListeners = HashMap<String, ActivityLifecycleListener>()
    private var initialRoute: String? = null

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
            this::unregisterActivityLifecycleListener,
            application.container.db,
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
        handleImportIntent(intent)
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
        // Optionally, you may want to trigger navigation here if needed
        handleImportIntent(intent)
    }

    private fun handleImportIntent(intent: Intent?) {
        val importUri = extractVitalWearImportUri(intent) ?: return
        val application = applicationContext as VBHelper

        lifecycleScope.launch(Dispatchers.IO) {
            val result = runCatching {
                contentResolver.openInputStream(importUri)?.use { inputStream ->
                    val character = Character.parseFrom(inputStream)
                    VitalWearCharacterImporter(application.container.db).importCharacter(character)
                } ?: VitalWearCharacterImporter.ImportResult(
                    success = false,
                    message = "VitalWear import file could not be opened."
                )
            }.getOrElse {
                VitalWearCharacterImporter.ImportResult(
                    success = false,
                    message = "VitalWear import failed: ${it.message ?: "Unknown error"}"
                )
            }

            runOnUiThread {
                Toast.makeText(this@MainActivity, result.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun extractVitalWearImportUri(intent: Intent?): Uri? {
        if (intent == null) {
            return null
        }

        val isVitalWearImport = intent.type == VITALWEAR_CHARACTER_MIME
        if (!isVitalWearImport) {
            return null
        }

        return when (intent.action) {
            Intent.ACTION_SEND -> {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }
            Intent.ACTION_VIEW -> intent.data
            else -> null
        }
    }

    private fun getInitialRouteFromIntent(intent: Intent?): String? {
        if (intent == null) return null
        val data = intent.data
        if (intent.action == Intent.ACTION_VIEW && data != null) {
            if (data.scheme == "vbhelper" && data.host == "auth") {
                return "Battle"
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

    companion object {
        private const val VITALWEAR_CHARACTER_MIME = "application/x-vitalwear-character"
    }
}
