package com.github.nacabaro.vbhelper.navigation

import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
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
                    navController.navigate(item.route) {
                        // Always route tab clicks to each tab's root screen.
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                            saveState = false
                        }
                        launchSingleTop = true
                        restoreState = false
                    }
                }
            )
        }
    }
}