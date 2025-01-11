package com.github.nacabaro.vbhelper.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.navigation.NavigationItems

@Composable
fun HomeScreen(
    navController: NavController
) {
    Scaffold (
        topBar = {
            TopBanner(
                text = "VB Helper",
                onGearClick = {
                    navController.navigate(NavigationItems.Settings.route)
                }
            )
        }
    ) { contentPadding ->
        Box (
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            Text("Home Screen")
        }
    }
}
