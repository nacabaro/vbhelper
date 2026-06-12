package com.github.nacabaro.vbhelper.companion.firmware

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.lifecycleScope
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.companion.common.ChannelTypes
import com.github.nacabaro.vbhelper.companion.ui.CompanionLoading
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.io.DataOutputStream

class CompanionFirmwareImportActivity : ComponentActivity() {
    enum class FirmwareImportState {
        PickFirmware,
        LoadFirmware,
    }

    private val importState = MutableStateFlow(FirmwareImportState.PickFirmware)
    private val transferPercent = MutableStateFlow(0)
    private val transferStatus = MutableStateFlow("Preparing transfer")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firmwareImportActivity = buildFirmwareFilePickLauncher()
        setContent {
            BuildScreen(firmwareImportActivity)
        }
    }

    @Composable
    fun BuildScreen(firmwareImportActivity: ActivityResultLauncher<Array<String>>) {
        val state by importState.collectAsState()
        val percent by transferPercent.collectAsState()
        val status by transferStatus.collectAsState()
        if (state == FirmwareImportState.PickFirmware) {
            LaunchedEffect(true) {
                firmwareImportActivity.launch(arrayOf("*/*"))
            }
        }
        val loadingText = if (state == FirmwareImportState.PickFirmware) {
            getString(R.string.companion_loading_select_firmware)
        } else {
            "$status ($percent%)"
        }
        CompanionLoading(loadingText = loadingText)
    }

    private fun buildFirmwareFilePickLauncher(): ActivityResultLauncher<Array<String>> {
        return registerForActivityResult(ActivityResultContracts.OpenDocument()) {
            importState.value = FirmwareImportState.LoadFirmware
            if (it == null) {
                finish()
            } else {
                Timber.i("Path: ${it.path}")
                importFirmware(it)
            }
        }
    }

    private fun importFirmware(uri: Uri) {
        transferPercent.value = 0
        transferStatus.value = "Reading firmware file"
        val channelClient = Wearable.getChannelClient(this)
        val nodeListTask = Wearable.getNodeClient(this).connectedNodes
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val nodes = nodeListTask.await()
                if (nodes.isEmpty()) {
                    transferStatus.value = getString(R.string.companion_firmware_no_watch)
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@CompanionFirmwareImportActivity, getString(R.string.companion_firmware_no_watch), Toast.LENGTH_LONG).show()
                    }
                    importState.value = FirmwareImportState.PickFirmware
                    return@launch
                }

                val firmware = contentResolver.openInputStream(uri)?.use { input ->
                    input.readBytes()
                } ?: throw IllegalStateException("Unable to read selected firmware file")

                transferPercent.value = 5
                transferStatus.value = "Sending firmware"
                val totalBytes = (firmware.size + 4).toLong() * nodes.size
                var transferredBytes = 0L
                for (node in nodes) {
                    val channel = channelClient.openChannel(node.id, ChannelTypes.FIRMWARE_DATA).await()
                    try {
                        channelClient.getOutputStream(channel).await().use {
                            val output = DataOutputStream(it)
                            output.writeInt(firmware.size)
                            transferredBytes += 4
                            transferPercent.value = if (totalBytes == 0L) 0 else ((transferredBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                            val buffer = ByteArray(4096)
                            var bytesRead: Int
                            firmware.inputStream().use { firmwareInput ->
                                while (firmwareInput.read(buffer).also { bytesRead = it } >= 0) {
                                    output.write(buffer, 0, bytesRead)
                                    transferredBytes += bytesRead
                                    transferPercent.value = if (totalBytes == 0L) 0 else ((transferredBytes * 100) / totalBytes).toInt().coerceIn(0, 100)
                                }
                            }
                            output.flush()
                        }
                    } finally {
                        runCatching { channelClient.close(channel).await() }
                    }
                }
                transferStatus.value = "Transfer complete"
                transferPercent.value = 100
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CompanionFirmwareImportActivity, getString(R.string.companion_firmware_sent_success), Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Exception) {
                Timber.e(e, "Failed to send firmware to watch")
                transferStatus.value = "Transfer failed: ${e.message ?: "unknown"}"
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@CompanionFirmwareImportActivity, "Firmware transfer failed: ${e.message ?: "unknown error"}", Toast.LENGTH_LONG).show()
                }
                importState.value = FirmwareImportState.PickFirmware
            }
        }
    }
}

