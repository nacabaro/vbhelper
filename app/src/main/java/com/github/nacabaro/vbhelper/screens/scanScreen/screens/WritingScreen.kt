package com.github.nacabaro.vbhelper.screens.scanScreen.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.screens.scanScreen.SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenController
import androidx.compose.ui.res.stringResource
import com.github.nacabaro.vbhelper.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun WritingScreen(
    scanScreenController: ScanScreenController,
    characterId: Long,
    nfcCharacter: NfcCharacter,
    onComplete: () -> Unit,
    onCancel: () -> Unit,
) {
    val secrets by scanScreenController.secretsFlow.collectAsState(null)

    var writing by remember { mutableStateOf(false) }
    var writingScreen by remember { mutableStateOf(false) }
    var writingConfirmScreen by remember { mutableStateOf(false) }
    var isDoneSendingCard by remember { mutableStateOf(false) }
    var isDoneWritingCharacter by remember { mutableStateOf(false) }
    // In an HCE transfer the whole character is sent and the local copy is deleted in the
    // first step (onClickCheckCard). There is no physical "write character" step, so once the
    // character no longer exists we must finish instead of advancing to WriteCharacterScreen
    // (which would query the now-deleted character and crash).
    var characterGone by remember { mutableStateOf(false) }

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
                            scanScreenController.onClickCheckCard(secrets!!, nfcCharacter) {
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
                    scanScreenController.onClickCheckCard(secrets!!, nfcCharacter) {
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
    } else if (characterGone) {
        // HCE transfer finished in step 1; nothing to render, completion handled below.
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

    // Once the card has been sent, detect whether this was an HCE transfer: if the local
    // character is already gone, the controller fully handled send + delete in step 1.
    LaunchedEffect(isDoneSendingCard) {
        if (isDoneSendingCard) {
            val exists = withContext(Dispatchers.IO) {
                scanScreenController.characterExists(characterId)
            }
            characterGone = !exists
        }
    }

    LaunchedEffect(isDoneSendingCard, isDoneWritingCharacter, characterGone) {
        // Complete when either the physical two-step write is done, or the HCE transfer
        // already removed the local character in step 1.
        if ((isDoneSendingCard && isDoneWritingCharacter) || characterGone) {
            // Deletion is owned by ScanScreenControllerImpl and only happens after a
            // successful transfer (HCE: COMMIT acked by the watch in step 1; physical:
            // write succeeded in step 2). This screen just advances the UI.
            completedWriting = true
        }
    }

    if (completedWriting) {
        onComplete()
    }
}