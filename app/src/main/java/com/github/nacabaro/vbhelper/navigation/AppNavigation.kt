package com.github.nacabaro.vbhelper.navigation

import android.util.Log
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
import com.github.nacabaro.vbhelper.screens.HomeScreen
import com.github.nacabaro.vbhelper.screens.ScanScreen
import com.github.nacabaro.vbhelper.screens.SettingsScreen
import com.github.nacabaro.vbhelper.screens.SpriteViewer
import com.github.nacabaro.vbhelper.screens.StorageScreen

@Composable
fun AppNavigation(
    onClickRead: () -> Unit,
    onClickScan: () -> Unit,
    onClickImportCard: () -> Unit,
    isDoneReadingCharacter: Boolean
) {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { contentPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomNavItem.Home.route,
            modifier = Modifier
                .padding(contentPadding)
        ) {
            composable(BottomNavItem.Battles.route) {
                BattlesScreen()
            }
            composable(BottomNavItem.Home.route) {
                HomeScreen(
                    navController = navController
                )
            }
            composable(BottomNavItem.Storage.route) {
                StorageScreen()
            }
            composable(BottomNavItem.Scan.route) {
                onClickScan()
                ScanScreen(
                    navController = navController,
                    onClickRead = onClickRead,
                    isDoneReadingCharacter = isDoneReadingCharacter
                )
            }
            composable(BottomNavItem.Dex.route) {
                DexScreen(
                    navController = navController
                )
            }
            composable(BottomNavItem.Settings.route) {
                SettingsScreen(
                    navController = navController,
                    onClickImportCard = onClickImportCard
                )
            }
            composable(BottomNavItem.Viewer.route) {
                SpriteViewer(
                    navController = navController
                )
            }
            composable(BottomNavItem.CardView.route) {
                val dimId = it.arguments?.getString("dimId")
                Log.d("dimId", dimId.toString())
                if (dimId != null) {
                    DiMScreen(
                        navController = navController,
                        dimId = dimId.toInt()
                    )
                }
            }
        }
    }
}
