package com.github.nacabaro.vbhelper.companion.transfer

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.github.nacabaro.vbhelper.R

/**
 * Compatibility endpoint kept for legacy links/intents from older companion flows.
 * Character transfer remains watch-driven and is not supported from phone companion UI.
 */
class CompanionCharacterImportActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Toast.makeText(
            this,
            getString(R.string.companion_transfer_disabled_message),
            Toast.LENGTH_LONG,
        ).show()
        finish()
    }
}

