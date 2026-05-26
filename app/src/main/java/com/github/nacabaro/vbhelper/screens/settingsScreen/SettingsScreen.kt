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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.github.nacabaro.vbhelper.components.TopBanner
import com.github.nacabaro.vbhelper.navigation.NavigationItems
import com.github.nacabaro.vbhelper.R


@Composable
fun SettingsScreen(
    navController: NavController,
    settingsScreenController: SettingsScreenControllerImpl,
) {
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopBanner(
                text = stringResource(R.string.settings_title),
                onBackClick = {
                    navController.popBackStack()
                }
            )
        },
        modifier = Modifier
            .fillMaxSize()
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(top = contentPadding.calculateTopPadding())
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SettingsSection(title = stringResource(R.string.settings_section_nfc))
            SettingsEntry(
                title = stringResource(R.string.settings_import_apk_title),
                description = stringResource(R.string.settings_import_apk_desc)
            ) {
                settingsScreenController.onClickImportApk()
            }

            SettingsSection(title = stringResource(R.string.settings_section_dim_bem))
            SettingsEntry(
                title = stringResource(R.string.settings_import_card_title),
                description = stringResource(R.string.settings_import_card_desc)
            ) {
                settingsScreenController.onClickImportCard()
            }

            SettingsSection(title = stringResource(R.string.settings_section_about))
            SettingsEntry(
                title = stringResource(R.string.settings_credits_title),
                description = stringResource(R.string.settings_credits_desc)
            ) {
                navController.navigate(NavigationItems.Credits.route)
            }
            SettingsEntry(
                title = stringResource(R.string.settings_about_title),
                description = stringResource(R.string.settings_about_desc)
            ) {
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://github.com/nacabaro/vbhelper/")
                )
                context.startActivity(browserIntent)
            }

            SettingsSection(title = stringResource(R.string.settings_section_data))
            SettingsEntry(
                title = stringResource(R.string.settings_export_data_title),
                description = stringResource(R.string.settings_export_data_desc)
            ) {
                settingsScreenController.onClickOpenDirectory()
            }
            SettingsEntry(
                title = stringResource(R.string.settings_import_data_title),
                description = stringResource(R.string.settings_import_data_desc)
            ) {
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
    Box(
        modifier = Modifier
            .padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
