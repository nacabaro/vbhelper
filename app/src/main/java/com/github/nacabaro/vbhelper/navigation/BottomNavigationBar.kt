package com.github.nacabaro.vbhelper.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.res.stringResource

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(
        NavigationItems.Items,
        NavigationItems.Battles,
        NavigationItems.Home,
        NavigationItems.Dex,
        NavigationItems.Storage,
    )

    NavigationBar {
        val currentBackStackEntry = navController.currentBackStackEntryAsState()
        val currentRoute = currentBackStackEntry.value?.destination?.route

        items.forEach { item ->
            NavigationBarItem (
                icon = { Icon(painter = painterResource(item.icon), contentDescription = stringResource(item.label)) },
                label = { Text(text = stringResource(item.label)) },
                selected = currentRoute == item.route,
                onClick = {
                    if (item == NavigationItems.Home) {
                        // Home should always show the Home root, not a restored nested route
                        // like Settings that was opened from Home previously.
                        val poppedToHome = navController.popBackStack(
                            NavigationItems.Home.route,
                            inclusive = false,
                        )
                        if (!poppedToHome) {
                            navController.navigate(NavigationItems.Home.route) {
                                popUpTo(navController.graph.startDestinationId) { inclusive = false }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    } else if (item == NavigationItems.Storage) {
                        // Adventure is launched from Storage; tapping Storage again should always
                        // bring the user back to the Storage root instead of appearing stuck.
                        val poppedToStorage = navController.popBackStack(
                            NavigationItems.Storage.route,
                            inclusive = false,
                        )
                        if (!poppedToStorage) {
                            navController.navigate(NavigationItems.Storage.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    } else {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                }
            )
        }
    }
}