package com.github.nacabaro.vbhelper.screens.scanScreen.screens

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.screens.cardScreen.ChooseCard
import com.github.nacabaro.vbhelper.screens.scanScreen.DetectedTransport
import com.github.nacabaro.vbhelper.screens.scanScreen.SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenController
import com.github.nacabaro.vbhelper.screens.scanScreen.TransferHaptics
import com.github.nacabaro.vbhelper.R

private const val TAG = "ReadingScreen"

@Composable
fun ReadingScreen(
    scanScreenController: ScanScreenController,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val transferHaptics = remember(context) { TransferHaptics(context) }
    val secrets by scanScreenController.secretsFlow.collectAsState(null)
    val transferStatus by scanScreenController.transferStatusFlow.collectAsState(null)
    val detectedTransport by scanScreenController.detectedTransportFlow.collectAsState()

    val transportMessage = when (detectedTransport) {
        DetectedTransport.NFC_A -> stringResource(R.string.scan_transport_bandai_bracelet)
        DetectedTransport.ISO_DEP -> stringResource(R.string.scan_transport_vitalwear_hce)
        DetectedTransport.UNKNOWN -> null
    }

    var cardsRead by remember { mutableStateOf<List<Card>?>(null) }

    var readingScreen by remember { mutableStateOf(false) }
    var isDoneReadingCharacter by remember { mutableStateOf(false) }
    var cardSelectScreen by remember { mutableStateOf(false) }

    LaunchedEffect(readingScreen) {
        if (readingScreen) {
            transferHaptics.onTransferStart()
        }
    }

    fun startReadIfNeeded() {
        val availableSecrets = secrets
        if (availableSecrets == null) {
            Log.d(TAG, "startReadIfNeeded: skipped (no secrets)")
            return
        }
        if (!readingScreen || isDoneReadingCharacter || cardSelectScreen) {
            Log.d(
                TAG,
                "startReadIfNeeded: skipped (readingScreen=$readingScreen, done=$isDoneReadingCharacter, cardSelect=$cardSelectScreen)"
            )
            return
        }

        Log.d(TAG, "startReadIfNeeded: arming onClickRead")
        scanScreenController.onClickRead(
            secrets = availableSecrets,
            onComplete = {
                Log.d(TAG, "onClickRead.onComplete: marking read complete")
                transferHaptics.onTransferComplete()
                readingScreen = false
                isDoneReadingCharacter = true
            },
            onMultipleCards = { cards ->
                Log.d(TAG, "onClickRead.onMultipleCards: showing card select (count=${cards.size})")
                transferHaptics.onTransferProgress()
                cardsRead = cards
                readingScreen = false
                cardSelectScreen = true
                isDoneReadingCharacter = true
            }
        )
    }

    DisposableEffect(readingScreen) {
        if(readingScreen) {
            Log.d(TAG, "DisposableEffect: readingScreen=true, registering lifecycle listener")
            scanScreenController.registerActivityLifecycleListener(
                SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER,
                object: ActivityLifecycleListener {
                    override fun onPause() {
                        Log.d(TAG, "lifecycle.onPause: cancelRead")
                        scanScreenController.cancelRead()
                    }

                    override fun onResume() {
                        Log.d(TAG, "lifecycle.onResume: attempting startReadIfNeeded")
                        startReadIfNeeded()
                    }
                }
            )
            startReadIfNeeded()
        }
        onDispose {
            if(readingScreen) {
                Log.d(TAG, "DisposableEffect.onDispose: unregister listener + cancelRead")
                scanScreenController.unregisterActivityLifecycleListener(
                    SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER
                )
                scanScreenController.cancelRead()
            } else {
                Log.d(TAG, "DisposableEffect.onDispose: readingScreen=false, nothing to cancel")
            }
        }
    }

    if (isDoneReadingCharacter && !cardSelectScreen) {
        Log.d(TAG, "state gate: done read without cardSelect, calling onComplete")
        readingScreen = false
        onComplete()
    }

    if (!readingScreen) {
        ReadCharacterScreen(
            onClickConfirm = {
                Log.d(TAG, "ReadCharacterScreen.onClickConfirm: entering reading screen")
                readingScreen = true
            },
            onClickCancel = {
                Log.d(TAG, "ReadCharacterScreen.onClickCancel: user cancelled")
                onCancel()
            }
        )
    }

    if (readingScreen) {
        ActionScreen(
            topBannerText = stringResource(R.string.reading_character_title),
            detectedTransportMessage = transportMessage,
            transferStatusMessage = transferStatus,
            onClickCancel = {
            Log.d(TAG, "ActionScreen.onCancel: cancelRead + onCancel")
            readingScreen = false
            scanScreenController.cancelRead()
            onCancel()
            }
        )
    } else if (cardSelectScreen) {
        ChooseCard(
            cards = cardsRead!!,
            onCardSelected = { card ->
                Log.d(TAG, "ChooseCard.onCardSelected: selected cardId=${card.id}")
                cardSelectScreen = false
                scanScreenController.flushCharacter(card.id)
            }
        )
    }
}