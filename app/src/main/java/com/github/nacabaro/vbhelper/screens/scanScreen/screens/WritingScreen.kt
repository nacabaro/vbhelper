package com.github.nacabaro.vbhelper.screens.scanScreen.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.screens.scanScreen.SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenController
import com.github.nacabaro.vbhelper.source.StorageRepository
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

    val application = LocalContext.current.applicationContext as VBHelper
    val storageRepository = StorageRepository(application.container.db)

    var writing by remember { mutableStateOf(false) }
    var writingScreen by remember { mutableStateOf(false) }
    var writingConfirmScreen by remember { mutableStateOf(false) }
    var isDoneSendingCard by remember { mutableStateOf(false) }
    var isDoneWritingCharacter by remember { mutableStateOf(false) }
    // VitalWear (HCE) completes the whole transfer, including the local delete,
    // on the first scan. When set, the second scan is skipped entirely and this
    // screen must not delete or query the character again.
    var hceTransferComplete by remember { mutableStateOf(false) }

    DisposableEffect(writing) {
        if (writing) {
            scanScreenController.registerActivityLifecycleListener(
                SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER,
                object : ActivityLifecycleListener {
                    override fun onPause() {
                        scanScreenController.cancelRead()
                    }

                    override fun onResume() {
                        if (!isDoneSendingCard) {
                            scanScreenController.onClickCheckCard(secrets!!, nfcCharacter) { transferComplete ->
                                if (transferComplete) {
                                    hceTransferComplete = true
                                    isDoneWritingCharacter = true
                                }
                                isDoneSendingCard = true
                            }
                        } else if (!isDoneWritingCharacter) {
                            scanScreenController.onClickWrite(secrets!!, nfcCharacter) {
                                isDoneWritingCharacter = true
                            }
                        }
                    }
                }
            )

            if (secrets != null) {
                if (!isDoneSendingCard) {
                    scanScreenController.onClickCheckCard(secrets!!, nfcCharacter) { transferComplete ->
                        if (transferComplete) {
                            hceTransferComplete = true
                            isDoneWritingCharacter = true
                        }
                        isDoneSendingCard = true
                    }
                } else if (!isDoneWritingCharacter) {
                    scanScreenController.onClickWrite(secrets!!, nfcCharacter) {
                        isDoneWritingCharacter = true
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
        ActionScreen( topBannerText = stringResource(R.string.sending_card_title)) {
            scanScreenController.cancelRead()
            onCancel()
        }
    } else if (hceTransferComplete) {
        // Transfer already finished on the first scan (VitalWear): the character
        // no longer exists locally, so none of the second-step screens may run.
        writing = false
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
        ActionScreen(topBannerText = stringResource(R.string.writing_character_action_title)) {
            isDoneSendingCard = false
            scanScreenController.cancelRead()
            onCancel()
        }
    }

    var completedWriting by remember { mutableStateOf(false) }

    LaunchedEffect(isDoneSendingCard, isDoneWritingCharacter) {
        withContext(Dispatchers.IO) {
            if (isDoneSendingCard && isDoneWritingCharacter) {
                if (!hceTransferComplete) {
                    // Physical bracelet: the character leaves the app only after
                    // both write steps succeed. HCE already deleted it on scan one.
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