package com.github.nacabaro.vbhelper.screens.scanScreen

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import timber.log.Timber

class TransferHaptics(context: Context) {
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(VibratorManager::class.java)
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    fun onTransferStart() {
        vibrate(VibrationEffect.createOneShot(100L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun onTransferProgress() {
        vibrate(VibrationEffect.createOneShot(50L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun onTransferComplete() {
        vibrate(VibrationEffect.createWaveform(longArrayOf(0L, 100L, 80L, 150L), -1))
    }

    fun onTransferFailed() {
        vibrate(VibrationEffect.createOneShot(250L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    fun onDeviceDetected() {
        vibrate(VibrationEffect.createOneShot(80L, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun vibrate(effect: VibrationEffect) {
        val deviceVibrator = vibrator ?: return
        if (!deviceVibrator.hasVibrator()) {
            return
        }
        runCatching {
            deviceVibrator.vibrate(effect)
        }.onFailure { error ->
            Timber.w(error, "Transfer haptics vibration skipped")
        }
    }
}

