package com.github.nacabaro.vbhelper.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.github.nacabaro.vbhelper.screens.BattlesScreen
import com.github.nacabaro.vbhelper.screens.DexScreen
import com.github.nacabaro.vbhelper.screens.DiMScreen
import com.github.nacabaro.vbhelper.screens.homeScreens.HomeScreen
import com.github.nacabaro.vbhelper.screens.ItemsScreen
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreen
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.settingsScreen.SettingsScreen
import com.github.nacabaro.vbhelper.screens.SpriteViewer
import com.github.nacabaro.vbhelper.screens.StorageScreen
import com.github.nacabaro.vbhelper.screens.settingsScreen.SettingsScreenControllerImpl

data class AppNavigationHandlers(
    val settingsScreenController: SettingsScreenControllerImpl,
    val scanScreenController: ScanScreenControllerImpl,
)

@Composable
fun AppNavigation(
    applicationNavigationHandlers: AppNavigationHandlers,
    onClickImportCard: () -> Unit,
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = NavigationItems.Home.route,
            modifier = Modifier
                .padding(contentPadding)
        ) {
            composable(NavigationItems.Battles.route) {
                BattlesScreen()
            }
            composable(NavigationItems.Home.route) {
                HomeScreen(
                    navController = navController
                )
            }
            composable(NavigationItems.Storage.route) {
                StorageScreen(
                    navController = navController
                )
            }
            composable(NavigationItems.Scan.route) {
                val characterIdString = it.arguments?.getString("characterId")
                val characterId = characterIdString?.toLongOrNull()

                ScanScreen(
                    navController = navController,
                    scanScreenController = applicationNavigationHandlers.scanScreenController,
                    characterId = characterId
                )
            }
            composable(NavigationItems.Dex.route) {
                DexScreen(
                    navController = navController
                )
            }
            composable(NavigationItems.Settings.route) {
                SettingsScreen(
                    navController = navController,
                    settingsScreenController = applicationNavigationHandlers.settingsScreenController,
                    onClickImportCard = onClickImportCard,
                )
            }
            composable(NavigationItems.Viewer.route) {
                SpriteViewer(
                    navController = navController
                )
            }
            composable(NavigationItems.CardView.route) {
                val cardId = it.arguments?.getString("cardId")
                if (cardId != null) {
                    DiMScreen(
                        navController = navController,
                        dimId = cardId.toLong()
                    )
                }
            }
            composable(NavigationItems.Items.route) {
                ItemsScreen(
                    navController = navController
                )
            }
        }
    }
}
