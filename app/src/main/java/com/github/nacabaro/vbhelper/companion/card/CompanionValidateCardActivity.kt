package com.github.nacabaro.vbhelper.companion.card

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.MifareUltralight
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.github.nacabaro.vbhelper.R
import com.github.nacabaro.vbhelper.companion.ui.CompanionLoading
import kotlinx.coroutines.flow.MutableStateFlow
import timber.log.Timber

class CompanionValidateCardActivity : ComponentActivity(), NfcAdapter.ReaderCallback {
    companion object {
        const val CARD_VALIDATED_KEY = "CARD_VALIDATED"
    }

    enum class CardValidationState {
        WaitingForVBConnect,
        ValidateCardOnVB,
        Success,
    }

    private lateinit var nfcAdapter: NfcAdapter
    private val validationStateFlow = MutableStateFlow(CardValidationState.WaitingForVBConnect)
    private var cardIdToValidate: UShort = 0u

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val maybeNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        val inputCardId = intent.getIntExtra(CARD_VALIDATED_KEY, -1)
        if (inputCardId == -1) {
            Timber.e("CompanionValidateCardActivity called without card number!")
            finish()
            return
        }
        cardIdToValidate = inputCardId.toUShort()
        if (maybeNfcAdapter == null) {
            Toast.makeText(this, getString(R.string.scan_no_nfc_on_device), Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        nfcAdapter = maybeNfcAdapter
        setContent {
            DisplayContent()
        }
    }

    @Composable
    fun DisplayContent() {
        val validationState by validationStateFlow.collectAsState()
        when (validationState) {
            CardValidationState.WaitingForVBConnect -> {
                CompanionLoading(loadingText = stringResource(R.string.companion_validation_connect_vb))
            }
            CardValidationState.ValidateCardOnVB -> {
                CompanionLoading(loadingText = stringResource(R.string.companion_validation_insert_card))
            }
            CardValidationState.Success -> {
                LaunchedEffect(true) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent().apply {
                            putExtra(CARD_VALIDATED_KEY, cardIdToValidate.toInt())
                        }
                        setResult(RESULT_OK, intent)
                        finish()
                    }, 1000)
                }
                Text(text = stringResource(R.string.companion_validation_success))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!nfcAdapter.isEnabled) {
            showWirelessSettings()
        } else {
            nfcAdapter.enableReaderMode(this, this, NfcAdapter.FLAG_READER_NFC_A, Bundle())
        }
    }

    override fun onPause() {
        super.onPause()
        if (this::nfcAdapter.isInitialized) {
            nfcAdapter.disableReaderMode(this)
        }
    }

    private fun showWirelessSettings() {
        Toast.makeText(this, getString(R.string.scan_nfc_must_be_enabled), Toast.LENGTH_SHORT).show()
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    override fun onTagDiscovered(tag: Tag?) {
        if (tag == null) {
            Timber.w("Tag discovery returned null tag")
            return
        }
        when (validationStateFlow.value) {
            CardValidationState.WaitingForVBConnect -> {
                val nfcData = MifareUltralight.get(tag)
                if (nfcData == null) {
                    Timber.w("Unsupported NFC tech for validation")
                    return
                }
                val writeSuccessful = runCatching {
                    nfcData.connect()
                    nfcData.use {
                        val vbData = VBNfcData(nfcData)
                        vbData.writeCardCheck(nfcData, cardIdToValidate)
                    }
                    true
                }.onFailure {
                    Timber.e(it, "Failed during first validation NFC step")
                    runOnUiThread {
                        Toast.makeText(
                            this,
                            "Failed to validate card on bracelet. Please retry.",
                            Toast.LENGTH_SHORT,
                        ).show()
                    }
                }.getOrDefault(false)
                if (writeSuccessful) {
                    validationStateFlow.value = CardValidationState.ValidateCardOnVB
                }
            }
            CardValidationState.ValidateCardOnVB -> {
                val nfcData = MifareUltralight.get(tag)
                if (nfcData == null) {
                    Timber.w("Unsupported NFC tech for validation")
                    return
                }
                runCatching {
                    nfcData.connect()
                    nfcData.use {
                        val vbData = VBNfcData(nfcData)
                        if (vbData.wasCardIdValidated(cardIdToValidate)) {
                            validationStateFlow.value = CardValidationState.Success
                        }
                    }
                }.onFailure {
                    Timber.e(it, "Failed during second validation NFC step")
                }
            }
            else -> Timber.w("Incorrect state")
        }
    }
}

