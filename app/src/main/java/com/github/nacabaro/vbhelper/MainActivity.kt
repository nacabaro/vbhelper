package com.github.nacabaro.vbhelper

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.nacabaro.vbhelper.navigation.AppNavigation
import com.github.cfogrady.vbnfc.CryptographicTransformer
import com.github.cfogrady.vbnfc.TagCommunicator
import com.github.cfogrady.vbnfc.data.DeviceType
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.ui.theme.VBHelperTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var deviceToCryptographicTransformers: Map<UShort, CryptographicTransformer>

    private var nfcCharacter = MutableStateFlow<NfcCharacter?>(null)

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    override fun onCreate(savedInstanceState: Bundle?) {
        deviceToCryptographicTransformers = getMapOfCryptographicTransformers()

        val maybeNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (maybeNfcAdapter == null) {
            Toast.makeText(this, "No NFC on device!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        nfcAdapter = maybeNfcAdapter


        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VBHelperTheme {
                MainApplication()
            }
        }
    }

    @Composable
    private fun MainApplication() {
        var isDoneReadingCharacter by remember { mutableStateOf(false) }
        AppNavigation(
            isDoneReadingCharacter = isDoneReadingCharacter,
            onClickRead = {
                handleTag {
                    val character = it.receiveCharacter()
                    nfcCharacter.value = character
                    isDoneReadingCharacter = true
                    "Done reading character"
                }
            }
        )
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun getMapOfCryptographicTransformers(): Map<UShort, CryptographicTransformer> {
        return mapOf(
            Pair(DeviceType.VitalBraceletBEDeviceType,
                CryptographicTransformer(readableHmacKey1 = resources.getString(com.github.cfogrady.vbnfc.R.string.password1),
                    readableHmacKey2 = resources.getString(com.github.cfogrady.vbnfc.R.string.password2),
                    aesKey = resources.getString(com.github.cfogrady.vbnfc.R.string.decryptionKey),
                    substitutionCipher = resources.getIntArray(com.github.cfogrady.vbnfc.R.array.substitutionArray))),
//            Pair(DeviceType.VitalSeriesDeviceType,
//                CryptographicTransformer(hmacKey1 = resources.getString(R.string.password1),
//                    hmacKey2 = resources.getString(R.string.password2),
//                    decryptionKey = resources.getString(R.string.decryptionKey),
//                    substitutionCipher = resources.getIntArray(R.array.substitutionArray))),
//            Pair(DeviceType.VitalCharactersDeviceType,
//                CryptographicTransformer(hmacKey1 = resources.getString(R.string.password1),
//                    hmacKey2 = resources.getString(R.string.password2),
//                    decryptionKey = resources.getString(R.string.decryptionKey),
//                    substitutionCipher = resources.getIntArray(R.array.substitutionArray)))
        )
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun showWirelessSettings() {
        Toast.makeText(this, "NFC must be enabled", Toast.LENGTH_SHORT).show()
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun buildOnReadTag(handlerFunc: (TagCommunicator)->String): (Tag)->Unit {
        return { tag->
            val nfcData = NfcA.get(tag)
            if (nfcData == null) {
                runOnUiThread {
                    Toast.makeText(this, "Tag detected is not VB", Toast.LENGTH_SHORT).show()
                }
            }
            nfcData.connect()
            nfcData.use {
                val tagCommunicator = TagCommunicator.getInstance(nfcData, deviceToCryptographicTransformers)
                val successText = handlerFunc(tagCommunicator)
                runOnUiThread {
                    Toast.makeText(this, successText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun handleTag(handlerFunc: (TagCommunicator)->String) {
        if (!nfcAdapter.isEnabled) {
            showWirelessSettings()
        } else {
            val options = Bundle()
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            nfcAdapter.enableReaderMode(this, buildOnReadTag(handlerFunc), NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options
            )
        }
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    override fun onPause() {
        super.onPause()
        if (nfcAdapter.isEnabled) {
            nfcAdapter.disableReaderMode(this)
        }
    }
}
