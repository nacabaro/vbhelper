package com.github.nacabaro.vbhelper.screens.settingsScreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.R

@Composable
fun CreditsScreen(
    navController: NavController
) {
    Scaffold (
        topBar = {
            TopBanner(
                text = stringResource(R.string.credits_title),
                onBackClick = {
                    navController.popBackStack()
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
            SettingsSection(stringResource(R.string.credits_section_reverse_engineering))
            SettingsEntry(title = "cyanic", description = stringResource(R.string.credits_cyanic_description)) { }
            SettingsSection(stringResource(R.string.credits_section_app_development))
            SettingsEntry(title = "cfogrady", description = stringResource(R.string.credits_cfogrady_description)) { }
            SettingsEntry(title = "nacabaro", description = stringResource(R.string.credits_nacabaro_description)) { }
            SettingsEntry(title = "lightheel", description = stringResource(R.string.credits_lightheel_description)) { }
            SettingsEntry(title = "shvstrz", description = stringResource(R.string.credits_shvstrz_description)) { }
        }
    }
}