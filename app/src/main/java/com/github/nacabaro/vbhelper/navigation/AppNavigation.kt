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
import com.github.nacabaro.vbhelper.screens.HomeScreen
import com.github.nacabaro.vbhelper.screens.ScanScreen
import com.github.nacabaro.vbhelper.screens.StorageScreen

@Composable
fun AppNavigation() {
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
                HomeScreen()
            }
            composable(BottomNavItem.Storage.route) {
                StorageScreen()
            }
            composable(BottomNavItem.Scan.route) {
                ScanScreen()
            }
            composable(BottomNavItem.Dex.route) {
                DexScreen()
            }
        }
    }
}
