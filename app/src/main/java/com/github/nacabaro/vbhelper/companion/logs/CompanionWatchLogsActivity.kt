package com.github.nacabaro.vbhelper.companion.logs

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.companion.ui.CompanionConfirmation
import com.github.nacabaro.vbhelper.companion.ui.CompanionLoading
import com.github.nacabaro.vbhelper.di.VBHelper

class CompanionWatchLogsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activity = this
        setContent {
            var accepted by remember { mutableStateOf(false) }
            if (!accepted) {
                CompanionConfirmation(prompt = stringResource(R.string.companion_logs_warning)) {
                    if (it) {
                        accepted = true
                    } else {
                        finish()
                    }
                }
            } else {
                CompanionLoading(loadingText = stringResource(R.string.settings_companion_send_watch_logs_title))
                LaunchedEffect(true) {
                    (application as VBHelper).companionLogService.fetchWatchLogs(activity)
                }
            }
        }
    }
}


