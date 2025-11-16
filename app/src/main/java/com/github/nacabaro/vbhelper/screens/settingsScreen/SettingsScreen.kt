package com.github.nacabaro.vbhelper.screens.settingsScreen

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.navigation.NavigationItems

@Composable
fun SettingsScreen(
    navController: NavController,
    settingsScreenController: SettingsScreenControllerImpl,
) {
    val context = LocalContext.current

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
            SettingsSection("NFC Communication")
            SettingsEntry(title = "Import APK", description = "Import Secrets From Vital Arena 2.1.0 APK") {
                settingsScreenController.onClickImportApk()
            }
            SettingsSection("DiM/BEm management")
            SettingsEntry(title = "Import card", description = "Import DiM/BEm card file") {
                settingsScreenController.onClickImportCard()
            }
            SettingsSection("About and credits")
            SettingsEntry(title = "Credits", description = "Credits") {
                navController.navigate(NavigationItems.Credits.route)
            }
            SettingsEntry(title = "About", description = "About") {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://github.com/nacabaro/vbhelper/"))
                context.startActivity(browserIntent)
            }
            SettingsSection("Data management")
            SettingsEntry(title = "Export data", description = "Export application database") {
                settingsScreenController.onClickOpenDirectory()
            }
            SettingsEntry(title = "Import data", description = "Import application database") {
                settingsScreenController.onClickImportDatabase()
            }
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