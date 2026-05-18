package com.github.nacabaro.vbhelper.screens.settingsScreen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
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

            SettingsSection(title = stringResource(R.string.settings_section_companion_tools))
            SettingsEntry(
                title = stringResource(R.string.settings_companion_import_card_image_title),
                description = stringResource(R.string.settings_companion_import_card_image_desc)
            ) {
                settingsScreenController.onClickCompanionImportCardImage()
            }
            SettingsEntry(
                title = stringResource(R.string.settings_companion_import_firmware_title),
                description = stringResource(R.string.settings_companion_import_firmware_desc)
            ) {
                settingsScreenController.onClickCompanionImportFirmware()
            }
            SettingsEntry(
                title = stringResource(R.string.settings_companion_send_watch_logs_title),
                description = stringResource(R.string.settings_companion_send_watch_logs_desc)
            ) {
                settingsScreenController.onClickCompanionSendWatchLogs()
            }
            SettingsEntry(
                title = stringResource(R.string.settings_companion_send_phone_logs_title),
                description = stringResource(R.string.settings_companion_send_phone_logs_desc)
            ) {
                settingsScreenController.onClickCompanionSendPhoneLogs()
            }
            SettingsSwitchEntry(
                title = stringResource(R.string.settings_enable_dim_to_bem_title),
                description = stringResource(R.string.settings_enable_dim_to_bem_desc),
                isEnabled = settingsScreenController,
                onToggle = { settingsScreenController.onToggleDimToBemConversion(it) }
            )
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
fun SettingsSwitchEntry(
    title: String,
    description: String,
    isEnabled: SettingsScreenControllerImpl,
    onToggle: (Boolean) -> Unit
) {
    val dimToBemEnabled by isEnabled.dimToBemConversionEnabled.collectAsState(false)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            Text(text = title)
            Text(
                text = description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.outline
            )
        }
        Switch(
            checked = dimToBemEnabled,
            onCheckedChange = onToggle
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
