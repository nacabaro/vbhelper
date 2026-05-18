package com.github.nacabaro.vbhelper.screens.scanScreen.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.screens.scanScreen.DetectedTransport
import com.github.nacabaro.vbhelper.screens.scanScreen.SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenController
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenController.WriteResult
import com.github.nacabaro.vbhelper.screens.scanScreen.TransferHaptics
import com.github.nacabaro.vbhelper.source.StorageRepository
import com.github.nacabaro.vbhelper.utils.BitmapData
import com.github.nacabaro.vbhelper.utils.ImageBitmapData
import com.github.nacabaro.vbhelper.utils.getImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.res.stringResource
import com.github.nacabaro.vbhelper.R

@Composable
fun WritingScreen(
    scanScreenController: ScanScreenController,
    characterId: Long,
    nfcCharacter: NfcCharacter,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
) {
    val secrets by scanScreenController.secretsFlow.collectAsState(null)
    val transferStatus by scanScreenController.transferStatusFlow.collectAsState(null)
    val detectedTransport by scanScreenController.detectedTransportFlow.collectAsState()

    val transportMessage = when (detectedTransport) {
        DetectedTransport.NFC_A -> stringResource(R.string.scan_transport_bandai_bracelet)
        DetectedTransport.ISO_DEP -> stringResource(R.string.scan_transport_vitalwear_hce)
        DetectedTransport.UNKNOWN -> null
    }

    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)
    val context = LocalContext.current

    val characterPreview by produceState<ImageBitmapData?>(initialValue = null, key1 = characterId) {
        value = withContext(Dispatchers.IO) {
            runCatching {
                storageRepository.getSingleCharacter(characterId)
            }.getOrNull()?.let { character ->
                if (character.spriteWidth > 0 && character.spriteHeight > 0 && character.spriteIdle.isNotEmpty()) {
                    BitmapData(
                        bitmap = character.spriteIdle,
                        width = character.spriteWidth,
                        height = character.spriteHeight
                    ).getImageBitmap(
                        context = context,
                        multiplier = 4,
                        obscure = false
                    )
                } else {
                    null
                }
            }
        }
    }

    var writing by remember { mutableStateOf(false) }
    var writingScreen by remember { mutableStateOf(false) }
    var writingConfirmScreen by remember { mutableStateOf(false) }
    var isDoneSendingCard by remember { mutableStateOf(false) }
    var isDoneWritingCharacter by remember { mutableStateOf(false) }
    var writeResult by remember { mutableStateOf<WriteResult?>(null) }

    DisposableEffect(writing) {
        if (writing) {
            val transferHaptics = TransferHaptics(application)
            transferHaptics.onTransferStart()

            scanScreenController.registerActivityLifecycleListener(
                SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER,
                object : ActivityLifecycleListener {
                    override fun onPause() {
                        scanScreenController.cancelRead()
                    }

                    override fun onResume() {
                        if (!isDoneSendingCard) {
                            scanScreenController.onClickCheckCard(secrets!!, nfcCharacter) {
                                isDoneSendingCard = true
                                transferHaptics.onTransferProgress()
                            }
                        } else if (!isDoneWritingCharacter) {
                            scanScreenController.onClickWrite(secrets!!, nfcCharacter, characterId) { result ->
                                writeResult = result
                                isDoneWritingCharacter = true
                                transferHaptics.onTransferProgress()
                            }
                        }
                    }
                }
            )

            if (secrets != null) {
                if (!isDoneSendingCard) {
                    scanScreenController.onClickCheckCard(secrets!!, nfcCharacter) {
                        isDoneSendingCard = true
                        transferHaptics.onTransferProgress()
                    }
                } else if (!isDoneWritingCharacter) {
                    scanScreenController.onClickWrite(secrets!!, nfcCharacter, characterId) { result ->
                        writeResult = result
                        isDoneWritingCharacter = true
                        transferHaptics.onTransferProgress()
                    }
                }
            }
        }

        onDispose {
            if (writing) {
                scanScreenController.unregisterActivityLifecycleListener(
                    SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER
                )
                scanScreenController.cancelRead()
            }
        }
    }

    if (!writingScreen) {
        writing = false
        WriteCardScreen (
            characterId = characterId,
            onClickCancel = {
                scanScreenController.cancelRead()
                onCancel()
            },
            onClickConfirm = {
                writingScreen = true
            }
        )
    } else if (!isDoneSendingCard) {
        writing = true
        ActionScreen(
            topBannerText = stringResource(R.string.sending_card_title),
            detectedTransportMessage = transportMessage,
            transferStatusMessage = transferStatus,
            characterPreview = characterPreview,
            onClickCancel = {
            scanScreenController.cancelRead()
            onCancel()
            }
        )
    } else if (!writingConfirmScreen) {
        writing = false
        WriteCharacterScreen (
            characterId = characterId,
            onClickCancel = {
                scanScreenController.cancelRead()
                onCancel()
            },
            onClickConfirm = {
                writingConfirmScreen = true
            }
        )
    } else if (!isDoneWritingCharacter) {
        writing = true
        ActionScreen(
            topBannerText = stringResource(R.string.writing_character_action_title),
            detectedTransportMessage = transportMessage,
            transferStatusMessage = transferStatus,
            characterPreview = characterPreview,
            onClickCancel = {
            isDoneSendingCard = false
            scanScreenController.cancelRead()
            onCancel()
            }
        )
    }

    var completedWriting by remember { mutableStateOf(false) }

    LaunchedEffect(isDoneSendingCard, isDoneWritingCharacter) {
        withContext(Dispatchers.IO) {
            if (isDoneSendingCard && isDoneWritingCharacter) {
                // Only delete source character if transfer was confirmed as move.
                // COPIED means transfer was sent but not confirmed, so keep source.
                if (writeResult == WriteResult.MOVE_CONFIRMED) {
                    storageRepository.deleteCharacter(characterId)
                }
                completedWriting = true
            }
        }
    }

    if (completedWriting) {
        onComplete()
    }
}