package com.github.nacabaro.vbhelper.screens.scanScreen

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.nfc.tech.NfcA
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vbnfc.TagCommunicator
import com.github.cfogrady.vbnfc.be.BENfcCharacter
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vbnfc.vb.VBNfcCharacter
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.screens.scanScreen.converters.FromNfcConverter
import com.github.nacabaro.vbhelper.screens.scanScreen.converters.ToNfcConverter
import com.github.nacabaro.vbhelper.source.VitalWearCharacterExporter
import com.github.nacabaro.vbhelper.source.VitalWearCharacterImporter
import com.github.nacabaro.vbhelper.source.getCryptographicTransformerMap
import com.github.nacabaro.vbhelper.source.isMissingSecrets
import com.github.nacabaro.vbhelper.source.proto.Secrets
import com.github.nacabaro.vbhelper.transfer.hce.VitalWearHceReaderClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import com.github.nacabaro.vbhelper.R

class ScanScreenControllerImpl(
    override val secretsFlow: Flow<Secrets>,
    private val componentActivity: ComponentActivity,
    private val registerActivityLifecycleListener: (String, ActivityLifecycleListener)->Unit,
    private val unregisterActivityLifecycleListener: (String)->Unit,
    private val database: AppDatabase,
): ScanScreenController {
    private var lastScannedCharacter: NfcCharacter? = null
    private var pendingExportCharacterId: Long? = null
    private val nfcAdapter: NfcAdapter

    init {
        val maybeNfcAdapter = NfcAdapter.getDefaultAdapter(componentActivity)
        if (maybeNfcAdapter == null) {
            Toast.makeText(componentActivity,  componentActivity.getString(R.string.scan_no_nfc_on_device), Toast.LENGTH_SHORT).show()
        }
        nfcAdapter = maybeNfcAdapter
        checkSecrets()
    }

    override fun onClickRead(secrets: Secrets, onComplete: ()->Unit, onMultipleCards: (List<Card>) -> Unit) {
        handleTag(
            secrets,
            handlerFunc = { tagCommunicator ->
                try {
                    val character = tagCommunicator.receiveCharacter()
                    val resultMessage = characterFromNfc(character) { cards, nfcCharacter ->
                        lastScannedCharacter = nfcCharacter
                        onMultipleCards(cards)
                    }
                    onComplete.invoke()
                    resultMessage
                } catch (e: Exception) {
                    Log.e("NFC_READ", "Error reading character from NFC", e)
                    componentActivity.runOnUiThread {
                        Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_error_generic) + ": " + (e.message ?: e.javaClass.simpleName), Toast.LENGTH_LONG).show()
                    }
                    onComplete.invoke()
                    componentActivity.getString(R.string.scan_error_generic)
                }
            },
            hceHandler = { isoDep ->
                try {
                    val client = VitalWearHceReaderClient(isoDep)
                    val importer = VitalWearCharacterImporter(database)
                    // Read the character first and only COMMIT (which makes the watch
                    // drop its copy) if the import actually succeeds. Otherwise the
                    // character stays on the watch and nothing is lost.
                    var importResult: VitalWearCharacterImporter.ImportResult? = null
                    val committed = client.moveCharacterFromWatch { character ->
                        val result = importer.importCharacter(character)
                        importResult = result
                        result.success
                    }
                    val message = importResult?.message
                        ?: componentActivity.getString(R.string.scan_error_generic)
                    Log.i("NFC_READ", "VitalWear HCE import committed=$committed: $message")
                    componentActivity.runOnUiThread {
                        Toast.makeText(componentActivity, message, Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("NFC_READ", "Error reading character from VitalWear HCE", e)
                    componentActivity.runOnUiThread {
                        Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_error_generic) + ": " + (e.message ?: e.javaClass.simpleName), Toast.LENGTH_LONG).show()
                    }
                } finally {
                    onComplete()
                }
            }
        )
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

    private fun handleTag(
        secrets: Secrets,
        handlerFunc: (TagCommunicator) -> String,
        hceHandler: ((IsoDep) -> Unit)? = null,
    ) {
        if (!nfcAdapter.isEnabled) {
            showWirelessSettings()
        } else {
            val options = Bundle()
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            nfcAdapter.enableReaderMode(
                componentActivity,
                buildOnReadTag(secrets, handlerFunc, hceHandler),
                NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options
            )
        }
    }

    private fun buildOnReadTag(
        secrets: Secrets,
        handlerFunc: (TagCommunicator) -> String,
        hceHandler: ((IsoDep) -> Unit)? = null,
    ): (Tag) -> Unit {
        return { tag ->
            val isoDep = IsoDep.get(tag)
            if (isoDep != null && hceHandler != null) {
                // VitalWear watch via HCE / ISO-DEP
                isoDep.connect()
                isoDep.use { hceHandler(isoDep) }
            } else {
                // Physical Vital Bracelet via raw NFC-A
                val nfcData = NfcA.get(tag)
                if (nfcData == null) {
                    componentActivity.runOnUiThread {
                        Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_tag_not_vb), Toast.LENGTH_SHORT).show()
                    }
                } else {
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
        }
    }

    private fun checkSecrets() {
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            if(secretsFlow.stateIn(componentActivity.lifecycleScope).value.isMissingSecrets()) {
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_missing_secrets), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClickWrite(
        secrets: Secrets,
        nfcCharacter: NfcCharacter,
        onComplete: () -> Unit
    ) {
        handleTag(
            secrets,
            handlerFunc = { tagCommunicator ->
                try {
                    if (nfcCharacter is VBNfcCharacter) {
                        Log.d("SendCharacter", "VBNfcCharacter")
                        tagCommunicator.sendCharacter(nfcCharacter)
                    } else if (nfcCharacter is BENfcCharacter) {
                        Log.d("SendCharacter", "BENfcCharacter")
                        tagCommunicator.sendCharacter(nfcCharacter)
                    }
                    // Physical bracelet write succeeded: remove the local copy here so the
                    // WritingScreen no longer owns deletion (single source of truth).
                    pendingExportCharacterId?.let { database.userCharacterDao().deleteCharacterById(it) }
                    pendingExportCharacterId = null
                    onComplete.invoke()
                    componentActivity.getString(R.string.scan_sent_character_success)
                } catch (e: Throwable) {
                    Log.e("TAG", e.stackTraceToString())
                    componentActivity.getString(R.string.scan_error_generic)
                }
            },
            hceHandler = { _ ->
                // HCE: the character was already sent AND deleted (only if confirmed)
                // in onClickCheckCard's HCE handler. Nothing to do here.
                onComplete()
            }
        )
    }

    override fun onClickCheckCard(
        secrets: Secrets,
        nfcCharacter: NfcCharacter,
        onComplete: () -> Unit
    ) {
        handleTag(
            secrets,
            handlerFunc = { tagCommunicator ->
                tagCommunicator.prepareDIMForCharacter(nfcCharacter.dimId)
                onComplete.invoke()
                componentActivity.getString(R.string.scan_sent_dim_success)
            },
            hceHandler = { isoDep ->
                // VitalWear watch: send the character proto directly via HCE.
                val characterId = pendingExportCharacterId
                if (characterId == null) {
                    Log.e("NFC_WRITE", "No pending character to send to VitalWear")
                    componentActivity.runOnUiThread {
                        Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_error_generic), Toast.LENGTH_SHORT).show()
                    }
                    onComplete()
                    return@handleTag
                }
                try {
                    val proto = VitalWearCharacterExporter(componentActivity, database).buildCharacterProto(characterId)
                    val client = VitalWearHceReaderClient(isoDep)
                    // The watch firmware only implements NEGOTIATE/WRITE_CHUNK/COMMIT (no
                    // INS_STATUS), so we send and rely on COMMIT returning SW_OK as the signal
                    // that the watch received the full payload. sendCharacterToWatch throws if
                    // any APDU (including COMMIT) fails, so reaching the next line means the
                    // transfer was accepted by the watch — only then do we delete the local copy.
                    client.sendCharacterToWatch(proto)
                    database.userCharacterDao().deleteCharacterById(characterId)
                    pendingExportCharacterId = null
                    Log.i("NFC_WRITE", "Character sent to VitalWear and deleted from app (id=$characterId)")
                    componentActivity.runOnUiThread {
                        Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_sent_character_success), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("NFC_WRITE", "Error sending character to VitalWear HCE", e)
                    componentActivity.runOnUiThread {
                        Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_error_generic) + ": " + (e.message ?: e.javaClass.simpleName), Toast.LENGTH_LONG).show()
                    }
                } finally {
                    onComplete()
                }
            }
        )
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun showWirelessSettings() {
        Toast.makeText(componentActivity,  componentActivity.getString(R.string.scan_nfc_must_be_enabled), Toast.LENGTH_SHORT).show()
        componentActivity.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    override fun characterFromNfc(
        nfcCharacter: NfcCharacter,
        onMultipleCards: (List<Card>, NfcCharacter) -> Unit
    ): String {
        val nfcConverter = FromNfcConverter(
            componentActivity = componentActivity
        )
        return nfcConverter.addCharacter(nfcCharacter, onMultipleCards)
    }

    override suspend fun characterToNfc(characterId: Long): NfcCharacter {
        pendingExportCharacterId = characterId
        val nfcGenerator = ToNfcConverter(componentActivity = componentActivity)
        val character = nfcGenerator.characterToNfc(characterId)
        Log.d("CharacterType", character.toString())
        return character
    }

    override suspend fun characterExists(characterId: Long): Boolean {
        return database.userCharacterDao().characterExists(characterId)
    }

    override fun flushCharacter(cardId: Long) {
        val nfcConverter = FromNfcConverter(
            componentActivity = componentActivity
        )

        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            if (lastScannedCharacter != null) {
                nfcConverter.addCharacterUsingCard(lastScannedCharacter!!, cardId)
                lastScannedCharacter = null
            }
        }
    }
}