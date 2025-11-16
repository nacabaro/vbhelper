package com.github.nacabaro.vbhelper.screens.scanScreen.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.screens.cardScreen.ChooseCard
import com.github.nacabaro.vbhelper.screens.scanScreen.SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenController

@Composable
fun ReadingScreen(
    scanScreenController: ScanScreenController,
    onCancel: () -> Unit,
    onComplete: () -> Unit
) {
    val secrets by scanScreenController.secretsFlow.collectAsState(null)

    var cardsRead by remember { mutableStateOf<List<Card>?>(null) }

    var readingScreen by remember { mutableStateOf(false) }
    var isDoneReadingCharacter by remember { mutableStateOf(false) }
    var cardSelectScreen by remember { mutableStateOf(false) }

    DisposableEffect(readingScreen) {
        if(readingScreen) {
            scanScreenController.registerActivityLifecycleListener(
                SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER,
                object: ActivityLifecycleListener {
                    override fun onPause() {
                        scanScreenController.cancelRead()
                    }

                    override fun onResume() {
                        scanScreenController.onClickRead(
                            secrets = secrets!!,
                            onComplete = {
                                isDoneReadingCharacter = true
                            },
                            onMultipleCards = { cards ->
                                cardsRead = cards
                                readingScreen = false
                                cardSelectScreen = true
                                isDoneReadingCharacter = true
                            }
                        )
                    }
                }
            )
            scanScreenController.onClickRead(
                secrets = secrets!!,
                onComplete = {
                    isDoneReadingCharacter = true
                },
                onMultipleCards = { cards ->
                    cardsRead = cards
                    readingScreen = false
                    cardSelectScreen = true
                    isDoneReadingCharacter = true
                }
            )
        }
        onDispose {
            if(readingScreen) {
                scanScreenController.unregisterActivityLifecycleListener(
                    SCAN_SCREEN_ACTIVITY_LIFECYCLE_LISTENER
                )
                scanScreenController.cancelRead()
            }
        }
    }

    if (isDoneReadingCharacter && !cardSelectScreen) {
        readingScreen = false
        onComplete()
    }

    if (!readingScreen) {
        ReadCharacterScreen(
            onClickConfirm = {
                readingScreen = true
            },
            onClickCancel = {
                onCancel()
            }
        )
    }

    if (readingScreen) {
        ActionScreen("Reading character") {
            readingScreen = false
            scanScreenController.cancelRead()
            onCancel()
        }
    } else if (cardSelectScreen) {
        ChooseCard(
            cards = cardsRead!!,
            onCardSelected = { card ->
                cardSelectScreen = false
                scanScreenController.flushCharacter(card.id)
            }
        )
    }
}