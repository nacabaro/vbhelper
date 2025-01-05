package com.github.nacabaro.vbhelper.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner

@Composable
fun SettingsScreen(
    navController: NavController
) {
    Scaffold (
        topBar = {
            TopBanner(
                text = "Settings",
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
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection("General")
            SettingsEntry(title = "Import VB key", description = "Import standard vital bracelet keys") { }
            SettingsEntry(title = "Import VB Characters key", description = "Import standard vital bracelet keys") { }
            SettingsEntry(title = "Import VB BE key", description = "Import standard vital bracelet keys") { }
            SettingsEntry(title = "Import transform functions", description = "Import standard vital bracelet keys") { }
            SettingsEntry(title = "Import decryption key", description = "Import standard vital bracelet keys") { }
            SettingsSection("DiM/BEm management")
            SettingsEntry(title = "Import DiM card", description = "Import DiM card file") { }
            SettingsEntry(title = "Import BEm card", description = "Import BEm card file") { }
            SettingsSection("About and credits")
            SettingsEntry(title = "Credits", description = "Credits") { }
            SettingsEntry(title = "About", description = "About") { }
        }
    }
}

@Composable
fun SettingsEntry(
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(text = title)
        Text(
            text = description,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
fun SettingsSection(
    title: String
) {
    Box (
        modifier = Modifier
            .padding(start = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}