package com.github.nacabaro.vbhelper.screens.scanScreen

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vbnfc.TagCommunicator
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.screens.scanScreen.converters.FromNfcConverter
import com.github.nacabaro.vbhelper.screens.scanScreen.converters.ToNfcConverter
import com.github.nacabaro.vbhelper.source.getCryptographicTransformerMap
import com.github.nacabaro.vbhelper.source.isMissingSecrets
import com.github.nacabaro.vbhelper.source.proto.Secrets
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ScanScreenControllerImpl(
    override val secretsFlow: Flow<Secrets>,
    private val componentActivity: ComponentActivity,
    private val registerActivityLifecycleListener: (String, ActivityLifecycleListener)->Unit,
    private val unregisterActivityLifecycleListener: (String)->Unit,
): ScanScreenController {

    private val nfcAdapter: NfcAdapter

    init {
        val maybeNfcAdapter = NfcAdapter.getDefaultAdapter(componentActivity)
        if (maybeNfcAdapter == null) {
            Toast.makeText(componentActivity, "No NFC on device!", Toast.LENGTH_SHORT).show()
        }
        nfcAdapter = maybeNfcAdapter
        checkSecrets()
    }

    override fun onClickRead(secrets: Secrets, onComplete: ()->Unit) {
        handleTag(secrets) { tagCommunicator ->
            val character = tagCommunicator.receiveCharacter()
            val resultMessage = characterFromNfc(character)
            onComplete.invoke()
            resultMessage
        }
    }

    override fun cancelRead() {
        if(nfcAdapter.isEnabled) {
            nfcAdapter.disableReaderMode(componentActivity)
        }
    }

    override fun registerActivityLifecycleListener(
        key: String,
        activityLifecycleListener: ActivityLifecycleListener
    ) {
        registerActivityLifecycleListener.invoke(key, activityLifecycleListener)
    }

    override fun unregisterActivityLifecycleListener(key: String) {
        unregisterActivityLifecycleListener.invoke(key)
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun handleTag(secrets: Secrets, handlerFunc: (TagCommunicator)->String) {
        if (!nfcAdapter.isEnabled) {
            showWirelessSettings()
        } else {
            val options = Bundle()
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            nfcAdapter.enableReaderMode(componentActivity, buildOnReadTag(secrets, handlerFunc), NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options
            )
        }
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun buildOnReadTag(secrets: Secrets, handlerFunc: (TagCommunicator)->String): (Tag)->Unit {
        return { tag->
            val nfcData = NfcA.get(tag)
            if (nfcData == null) {
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "Tag detected is not VB", Toast.LENGTH_SHORT).show()
                }
            }
            nfcData.connect()
            nfcData.use {
                val tagCommunicator = TagCommunicator.getInstance(nfcData, secrets.getCryptographicTransformerMap())
                val successText = handlerFunc(tagCommunicator)
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, successText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkSecrets() {
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            if(secretsFlow.stateIn(componentActivity.lifecycleScope).value.isMissingSecrets()) {
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "Missing Secrets. Go to settings and import Vital Arena APK", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClickWrite(
        secrets: Secrets,
        nfcCharacter: NfcCharacter,
        onComplete: () -> Unit
    ) {
        handleTag(secrets) { tagCommunicator ->
            try {
                tagCommunicator.sendCharacter(nfcCharacter)
                onComplete.invoke()
                "Sent character successfully!"
            } catch (e: Throwable) {
                Log.e("TAG", e.stackTraceToString())
                "Whoops"
            }
        }
    }

    override fun onClickCheckCard(
        secrets: Secrets,
        nfcCharacter: NfcCharacter,
        onComplete: () -> Unit
    ) {
        handleTag(secrets) { tagCommunicator ->
            tagCommunicator.prepareDIMForCharacter(nfcCharacter.dimId)
            onComplete.invoke()
            "Sent DIM successfully!"
        }
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun showWirelessSettings() {
        Toast.makeText(componentActivity, "NFC must be enabled", Toast.LENGTH_SHORT).show()
        componentActivity.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    override fun characterFromNfc(nfcCharacter: NfcCharacter): String {
        val nfcConverter = FromNfcConverter(
            componentActivity = componentActivity
        )
        return nfcConverter.addCharacter(nfcCharacter)
    }

    override suspend fun characterToNfc(characterId: Long): NfcCharacter {
        val nfcGenerator = ToNfcConverter(
            componentActivity = componentActivity
        )
        return nfcGenerator.characterToNfc(characterId)
    }
}