package com.github.nacabaro.vbhelper.screens.settingsScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.navigation.NavigationItems

@Composable
fun CreditsScreen(
    navController: NavController
) {
    Scaffold (
        topBar = {
            TopBanner(
                text = "Credits",
                onBackClick = {
                    navController.popBackStack()
                },
                onGearClick = {
                    navController.navigate(NavigationItems.Viewer.route)
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { contentPadding ->
        Column (
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
        ) {
            SettingsSection("Reverse engineering")
            SettingsEntry(title = "cyanic", description = "Reversed the firmware and helped us during development.") { }
            SettingsSection("Application development")
            SettingsEntry(title = "cfogrady", description = "Developed vb-lib-nfc and part of this application.") { }
            SettingsEntry(title = "nacabaro", description = "Developed this application.") { }
            SettingsEntry(title = "lightheel", description = "Developing the battling part for this application, including server. Still in the works.") { }
            SettingsEntry(title = "shvstrz", description = "Designing the app icon in SVG.") { }
        }
    }
}