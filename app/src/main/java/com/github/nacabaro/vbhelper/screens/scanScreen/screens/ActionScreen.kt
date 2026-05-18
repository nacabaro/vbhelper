package com.github.nacabaro.vbhelper.screens.scanScreen.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.nacabaro.vbhelper.screens.scanScreen.TransferHaptics
import com.github.nacabaro.vbhelper.utils.ImageBitmapData

@Composable
fun ActionScreen(
    topBannerText: String,
    detectedTransportMessage: String?,
    transferStatusMessage: String?,
    characterPreview: ImageBitmapData? = null,
    onClickCancel: () -> Unit,
    isTransferring: Boolean = true,
) {
    val context = LocalContext.current
    val transferHaptics = remember(context) { TransferHaptics(context) }

    LaunchedEffect(transferStatusMessage) {
        if (transferStatusMessage != null && transferStatusMessage.contains("progress", ignoreCase = true)) {
            transferHaptics.onTransferProgress()
        }
    }

    if (transferStatusMessage?.contains("complete", ignoreCase = true) == true) {
        TransferCompleteScreen(
            topBannerText = topBannerText,
            resultMessage = transferStatusMessage,
            onClickOk = onClickCancel,
            isSuccess = !transferStatusMessage.contains("fail", ignoreCase = true)
        )
    } else if (isTransferring) {
        TransferAnimationScreen(
            topBannerText = topBannerText,
            detectedTransportMessage = detectedTransportMessage,
            transferStatusMessage = transferStatusMessage,
            characterPreview = characterPreview,
            onClickCancel = onClickCancel,
            isTransferring = true
        )
    } else {
        TransferAnimationScreen(
            topBannerText = topBannerText,
            detectedTransportMessage = detectedTransportMessage,
            transferStatusMessage = transferStatusMessage,
            characterPreview = characterPreview,
            onClickCancel = onClickCancel,
            isTransferring = false
        )
    }
}