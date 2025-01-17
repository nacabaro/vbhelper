package com.github.nacabaro.vbhelper.screens.settingsScreen

import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.github.nacabaro.vbhelper.source.SecretsImporter
import com.github.nacabaro.vbhelper.source.SecretsRepository
import com.github.nacabaro.vbhelper.source.proto.Secrets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class SettingsScreenController(val apkFilePickLauncher: ActivityResultLauncher<Array<String>>) {

    class Factory(private val componentActivity: ComponentActivity, private val secretsImporter: SecretsImporter, private val secretsRepository: SecretsRepository) {

        fun buildSettingScreenHandlers(): SettingsScreenController {
            return SettingsScreenController(
                apkFilePickLauncher = buildFilePickerActivityLauncher(this::importApk)
            )
        }

        private fun buildFilePickerActivityLauncher(onResult : (Uri?) ->Unit): ActivityResultLauncher<Array<String>> {
            return componentActivity.registerForActivityResult(ActivityResultContracts.OpenDocument()) {
                onResult.invoke(it)
            }
        }

        private fun importApk(uri: Uri?) {
            if(uri == null) {
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "APK Import Cancelled", Toast.LENGTH_SHORT)
                        .show()
                }
                return
            }
            componentActivity.lifecycleScope.launch(Dispatchers.IO) {
                componentActivity.contentResolver.openInputStream(uri).use {
                    if(it == null) {
                        componentActivity.runOnUiThread {
                            Toast.makeText(
                                componentActivity,
                                "Selected file is empty!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@launch
                    }
                    var secrets: Secrets? = null
                    try {
                        secrets = secretsImporter.importSecrets(it)
                    } catch (e: Exception) {
                        componentActivity.runOnUiThread {
                            Toast.makeText(componentActivity, "Secrets import failed. Please only select the official Vital Arena App 2.1.0 APK.", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }
                    componentActivity.lifecycleScope.launch(Dispatchers.IO) {
                        secretsRepository.updateSecrets(secrets)
                    }.invokeOnCompletion {
                        componentActivity.runOnUiThread {
                            Toast.makeText(componentActivity, "Secrets successfully imported. Connections with devices are now possible.", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}