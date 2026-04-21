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
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.screens.scanScreen.converters.FromNfcConverter
import com.github.nacabaro.vbhelper.screens.scanScreen.converters.ToNfcConverter
import com.github.nacabaro.vbhelper.source.VitalWearCharacterExporter
import com.github.nacabaro.vbhelper.source.VitalWearCharacterImporter
import com.github.nacabaro.vbhelper.source.getCryptographicTransformerMap
import com.github.nacabaro.vbhelper.source.isMissingSecrets
import com.github.nacabaro.vbhelper.source.proto.Secrets
import com.github.nacabaro.vbhelper.transfer.hce.VitalWearHceReaderClient
import com.github.nacabaro.vbhelper.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.Arrays
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class ScanScreenControllerImpl(
    override val secretsFlow: Flow<Secrets>,
    private val componentActivity: ComponentActivity,
    private val registerActivityLifecycleListener: (String, ActivityLifecycleListener)->Unit,
    private val unregisterActivityLifecycleListener: (String)->Unit,
): ScanScreenController {

    override val detectedTransportFlow: StateFlow<DetectedTransport>
        get() = _detectedTransportFlow

    private val _detectedTransportFlow = MutableStateFlow(DetectedTransport.UNKNOWN)
    private var lastScannedCharacter: NfcCharacter? = null
    private var lastRequestedCharacterId: Long? = null
    private val nfcAdapter: NfcAdapter
    private val isHandlingTag = AtomicBoolean(false)
    private var lastTagId: ByteArray? = null
    private var lastTagHandledAtMs: Long = 0L
    private val tagStateLock = Any()
    private val readerSessionCounter = AtomicLong(0L)
    @Volatile private var activeReaderSessionId: Long = 0L

    companion object {
        private const val TAG_DEBOUNCE_MS = 1500L
        private const val TAG_IGNORE_AFTER_HANDLED_MS = 2000
        private val VITALWEAR_AID = byteArrayOf(
            0xF0.toByte(), 0x56, 0x49, 0x54, 0x41, 0x4C, 0x57, 0x45, 0x41, 0x52
        )
        private const val SW_OK = 0x9000
        // Lifecycle key for the always-on NFC suppressor that prevents Android from launching
        // com.android.apps.tag/.TagViewer whenever the watch's HCE comes into range.
        private const val LIFECYCLE_KEY_NFC_SUPPRESSOR = "nfc_suppressor"
    }

    init {
        val maybeNfcAdapter = NfcAdapter.getDefaultAdapter(componentActivity)
        if (maybeNfcAdapter == null) {
            Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_no_nfc_on_device), Toast.LENGTH_SHORT).show()
        }
        nfcAdapter = maybeNfcAdapter
        checkSecrets()
        registerNfcSuppressor()
    }

    /**
     * Registers a lifecycle listener that keeps reader mode active (with a no-op callback)
     * whenever this Activity is in the foreground. This prevents Android's default NFC dispatch
     * system from launching com.android.apps.tag/.TagViewer when the watch's HCE service is
     * detected while the user hasn't explicitly pressed a transfer button yet.
     *
     * When the user presses Read/Write/CheckCard, [handleTag] replaces this suppressor with
     * the real transfer callback via [NfcAdapter.enableReaderMode]. After the transfer
     * completes, [enableNfcSuppressor] is called again to restore the passive suppressor.
     */
    private fun registerNfcSuppressor() {
        registerActivityLifecycleListener(
            LIFECYCLE_KEY_NFC_SUPPRESSOR,
            object : ActivityLifecycleListener {
                override fun onResume() {
                    enableNfcSuppressor()
                }
                override fun onPause() {
                    disableReaderModeSafely()
                }
            }
        )
    }

    /**
     * Enables reader mode with a silent no-op callback. Calling [NfcAdapter.enableReaderMode]
     * suppresses Android's default tag-dispatch system (and therefore the "new tag scanned"
     * TagViewer screen) for as long as this Activity is in the foreground. The actual transfer
     * logic is wired up separately via [handleTag] when the user presses a button.
     */
    private fun enableNfcSuppressor() {
        if (!nfcAdapter.isEnabled) return
        val options = Bundle()
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
        runCatching {
            nfcAdapter.enableReaderMode(
                componentActivity,
                { /* suppress default dispatch — user must tap a transfer button */ },
                flags,
                options
            )
        }.onFailure {
            Log.w("NFC", "Failed to enable NFC suppressor reader mode", it)
        }
    }

    // ---- Read (phone receives character FROM watch or bracelet) --------------------

    override fun onClickRead(secrets: Secrets, onComplete: () -> Unit, onMultipleCards: (List<Card>) -> Unit) {
        _detectedTransportFlow.value = DetectedTransport.UNKNOWN
        handleTag(
            secrets,
            nfcAHandler = { tagCommunicator ->
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
            isoDepHandler = { isoDep ->
                val application = componentActivity.applicationContext as VBHelper
                val importer = VitalWearCharacterImporter(application.container.db, application.container.transferSeenDao)
                try {
                    var importResult: VitalWearCharacterImporter.ImportResult? = null
                    val moved = VitalWearHceReaderClient(isoDep).moveCharacterFromWatch { character ->
                        val result = importer.importCharacter(character)
                        importResult = result
                        result.success
                    }
                    onComplete.invoke()
                    if (moved) {
                        importResult?.message ?: componentActivity.getString(R.string.scan_sent_character_success)
                    } else {
                        importResult?.message
                            ?: "VitalWear import was rejected. Source character remains on the watch."
                    }
                } catch (readError: Exception) {
                    Log.e("NFC_READ", "HCE read failed; watch may be armed as destination", readError)
                    onComplete.invoke()
                    "No source character detected on watch. If the watch is waiting to receive, use VBH to Watch."
                }
            }
        )
    }

    // ---- Write (phone sends character TO watch or bracelet) ------------------------

    override fun onClickWrite(secrets: Secrets, nfcCharacter: NfcCharacter, onComplete: (ScanScreenController.WriteResult) -> Unit) {
        _detectedTransportFlow.value = DetectedTransport.UNKNOWN
        handleTag(
            secrets,
            nfcAHandler = { tagCommunicator ->
                try {
                    val initialSlotState = readNfcASlotState(tagCommunicator)
                    if (initialSlotState.isFull()) {
                        onComplete.invoke(ScanScreenController.WriteResult.BLOCKED_DEVICE_FULL)
                        return@handleTag componentActivity.getString(R.string.scan_target_device_full)
                    }

                    val migrationCheck = verifyActiveToBackupMigration(tagCommunicator, initialSlotState)
                    if (!migrationCheck.canProceed) {
                        onComplete.invoke(ScanScreenController.WriteResult.COPIED)
                        return@handleTag migrationCheck.message
                    }

                    when (nfcCharacter) {
                        is VBNfcCharacter -> tagCommunicator.sendCharacter(nfcCharacter)
                        is BENfcCharacter -> tagCommunicator.sendCharacter(nfcCharacter)
                    }
                    onComplete.invoke(ScanScreenController.WriteResult.MOVE_CONFIRMED)
                    componentActivity.getString(R.string.scan_sent_character_success)
                } catch (e: Throwable) {
                    Log.e("TAG", e.stackTraceToString())
                    componentActivity.getString(R.string.scan_error_generic)
                }
            },
            isoDepHandler = { isoDep ->
                val characterId = lastRequestedCharacterId
                    ?: throw IllegalStateException("No character id available for VitalWear HCE write")
                val application = componentActivity.applicationContext as VBHelper
                val hceClient = VitalWearHceReaderClient(isoDep)
                try {
                    val proto = runBlocking {
                        VitalWearCharacterExporter(application.container.db)
                            .buildCharacterProto(characterId)
                    }
                    hceClient.sendCharacterToWatch(proto)
                    onComplete.invoke(ScanScreenController.WriteResult.MOVE_CONFIRMED)
                    componentActivity.getString(R.string.scan_sent_character_success)
                } catch (writeError: Throwable) {
                    Log.e("NFC_WRITE_HCE", "HCE write failed; attempting opposite direction", writeError)
                    runCatching {
                        var importResult: VitalWearCharacterImporter.ImportResult? = null
                        val moved = hceClient.moveCharacterFromWatch { character ->
                            val result = VitalWearCharacterImporter(application.container.db, application.container.transferSeenDao).importCharacter(character)
                            importResult = result
                            result.success
                        }
                        onComplete.invoke(ScanScreenController.WriteResult.COPIED)
                        if (moved) {
                            importResult?.message
                                ?: "Detected source on watch and imported it instead of sending."
                        } else {
                            importResult?.message
                                ?: "Watch is in source mode, but import was rejected."
                        }
                    }.getOrElse { readFallbackError ->
                        Log.e("NFC_WRITE_HCE", "HCE opposite-direction fallback failed", readFallbackError)
                        componentActivity.getString(R.string.scan_error_generic)
                    }
                }
            }
        )
    }

    // ---- Check card (NFC-A only; ISO-DEP watches don't need a DIM prep) -----------

    override fun onClickCheckCard(secrets: Secrets, nfcCharacter: NfcCharacter, onComplete: () -> Unit) {
        _detectedTransportFlow.value = DetectedTransport.UNKNOWN
        handleTag(
            secrets,
            nfcAHandler = { tagCommunicator ->
                tagCommunicator.prepareDIMForCharacter(nfcCharacter.dimId)
                onComplete.invoke()
                componentActivity.getString(R.string.scan_sent_dim_success)
            },
            // HCE equivalent of the prep step: verify watch is armed for phone->watch transfer.
            isoDepHandler = { isoDep ->
                runCatching {
                    VitalWearHceReaderClient(isoDep).verifyWatchReadyToReceive()
                }.fold(
                    onSuccess = {
                        onComplete.invoke()
                        "Watch ready. Tap again to send character."
                    },
                    onFailure = { error ->
                        Log.e("NFC_HCE", "Watch is not ready to receive character", error)
                        "Watch is not ready. On watch: Transfer > Receive from VBH, then tap again."
                    }
                )
            }
        )
    }

    // ---- Cancel / lifecycle -------------------------------------------------------

    override fun cancelRead() {
        _detectedTransportFlow.value = DetectedTransport.UNKNOWN
        activeReaderSessionId = readerSessionCounter.incrementAndGet()
        isHandlingTag.set(false)
        synchronized(tagStateLock) {
            lastTagId = null
            lastTagHandledAtMs = 0L
        }
        // Re-arm the suppressor so the TagViewer doesn't appear while the user is still
        // on the scan screen but hasn't pressed a transfer button yet.
        enableNfcSuppressor()
    }

    override fun registerActivityLifecycleListener(key: String, activityLifecycleListener: ActivityLifecycleListener) {
        registerActivityLifecycleListener.invoke(key, activityLifecycleListener)
    }

    override fun unregisterActivityLifecycleListener(key: String) {
        unregisterActivityLifecycleListener.invoke(key)
    }

    // ---- NFC adapter wiring -------------------------------------------------------

    /**
     * Arms the NFC reader for both NFC-A (bracelet) and ISO-DEP (VitalWear HCE).
     * [isoDepHandler] is optional; when null the reader only accepts NFC-A.
     */
    private fun handleTag(
        secrets: Secrets,
        nfcAHandler: (TagCommunicator) -> String,
        isoDepHandler: ((IsoDep) -> String)? = null
    ) {
        if (!nfcAdapter.isEnabled) {
            showWirelessSettings()
            return
        }
        val options = Bundle()
        // Work around for some broken Nfc firmware implementations that poll the card too fast
        options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
        val flags = NfcAdapter.FLAG_READER_NFC_A or
                NfcAdapter.FLAG_READER_NFC_B or
                NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS or
                NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
        val sessionId = readerSessionCounter.incrementAndGet()
        activeReaderSessionId = sessionId
        isHandlingTag.set(false)
        synchronized(tagStateLock) {
            lastTagId = null
            lastTagHandledAtMs = 0L
        }
        disableReaderModeSafely()
        nfcAdapter.enableReaderMode(
            componentActivity,
            buildOnReadTag(secrets, nfcAHandler, isoDepHandler, sessionId),
            flags,
            options
        )
    }

    private fun buildOnReadTag(
        secrets: Secrets,
        nfcAHandler: (TagCommunicator) -> String,
        isoDepHandler: ((IsoDep) -> String)?,
        sessionId: Long,
    ): (Tag) -> Unit {
        return { tag ->
            if (activeReaderSessionId == sessionId) {
                val now = System.currentTimeMillis()
                val shouldHandleTag = synchronized(tagStateLock) {
                    val sameRecentTag = lastTagId != null && tag.id != null && Arrays.equals(lastTagId, tag.id) &&
                        (now - lastTagHandledAtMs) < TAG_DEBOUNCE_MS
                    if (sameRecentTag || !isHandlingTag.compareAndSet(false, true)) {
                        false
                    } else {
                        lastTagId = tag.id?.clone()
                        lastTagHandledAtMs = now
                        true
                    }
                }
                if (shouldHandleTag) {

                // Detect transport once per tap and lock to that route for this transfer.
                val isoDep = IsoDep.get(tag)
                val hasIsoDepRoute = isoDep != null && isoDepHandler != null
                val hasNfcARoute = NfcA.get(tag) != null
                try {
                    // Prefer ISO-DEP whenever present so HCE sessions do not accidentally fall through to NFC-A.
                    if (hasIsoDepRoute) {
                        val isoDepTarget = isoDep
                        val isoDepAction = isoDepHandler
                        val confirmedVitalWear = runCatching { isVitalWearHceTarget(isoDepTarget) }.getOrDefault(false)
                        if (!confirmedVitalWear) {
                            Log.w("NFC_HCE", "IsoDep tag did not confirm VitalWear AID; attempting ISO-DEP handler without NFC-A fallback")
                        }
                        try {
                            _detectedTransportFlow.value = DetectedTransport.ISO_DEP
                            isoDepTarget.connect()
                            isoDepTarget.use {
                                val successText = isoDepAction.invoke(isoDepTarget)
                                componentActivity.runOnUiThread {
                                    Toast.makeText(componentActivity, successText, Toast.LENGTH_SHORT).show()
                            }
                        }
                        } catch (e: Throwable) {
                            Log.e("NFC_HCE", "IsoDep transfer failed", e)
                            componentActivity.runOnUiThread {
                                Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_error_generic), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else if (hasNfcARoute) {
                        if (!handleNfcATag(tag, secrets, nfcAHandler)) {
                            componentActivity.runOnUiThread {
                                Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_tag_not_vb), Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        _detectedTransportFlow.value = DetectedTransport.UNKNOWN
                        componentActivity.runOnUiThread {
                            Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_tag_not_vb), Toast.LENGTH_SHORT).show()
                        }
                    }
                    } finally {
                        finishHandledTagSession(tag, sessionId)
                    }
                }
            }
        }
    }

    private fun finishHandledTagSession(tag: Tag, sessionId: Long) {
        if (activeReaderSessionId != sessionId) return

        // Keep Android from redispatching this same tag to other NFC apps while still in range.
        runCatching {
            nfcAdapter.ignore(
                tag,
                TAG_IGNORE_AFTER_HANDLED_MS,
                NfcAdapter.OnTagRemovedListener { },
                null
            )
        }.onFailure {
            Log.w("NFC", "Failed to ignore handled tag", it)
        }

        // Restore the passive suppressor so devices can remain close without Android
        // launching the TagViewer between transfers or while waiting for the next button press.
        enableNfcSuppressor()
        isHandlingTag.set(false)
    }

    private fun handleNfcATag(
        tag: Tag,
        secrets: Secrets,
        nfcAHandler: (TagCommunicator) -> String,
    ): Boolean {
        val nfcData = NfcA.get(tag) ?: return false
        _detectedTransportFlow.value = DetectedTransport.NFC_A
        return try {
            nfcData.connect()
            nfcData.use {
                val tagCommunicator = TagCommunicator.getInstance(nfcData, secrets.getCryptographicTransformerMap())
                val successText = nfcAHandler(tagCommunicator)
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, successText, Toast.LENGTH_SHORT).show()
                }
            }
            true
        } catch (e: Throwable) {
            val staleTagSession = e is SecurityException && (e.message?.contains("out of date", ignoreCase = true) == true)
            if (staleTagSession) {
                Log.w("NFC_A", "Ignoring stale NFC-A tag session; waiting for a fresh tap", e)
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "Tag session expired. Please tap again.", Toast.LENGTH_SHORT).show()
                }
                return false
            }
            Log.e("NFC_A", "NfcA transfer failed", e)
            componentActivity.runOnUiThread {
                Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_error_generic), Toast.LENGTH_SHORT).show()
            }
            true
        }
    }

    private fun disableReaderModeSafely() {
        if (!nfcAdapter.isEnabled) return
        runCatching { nfcAdapter.disableReaderMode(componentActivity) }
    }

    private fun isVitalWearHceTarget(isoDep: IsoDep): Boolean {
        return runCatching {
            val originalTimeout = isoDep.timeout
            try {
                isoDep.timeout = 500
                if (!isoDep.isConnected) {
                    isoDep.connect()
                }
                val selectApdu = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, VITALWEAR_AID.size.toByte()) + VITALWEAR_AID
                val response = isoDep.transceive(selectApdu)
                statusWord(response) == SW_OK
            } finally {
                isoDep.timeout = originalTimeout
                runCatching { if (isoDep.isConnected) isoDep.close() }
            }
        }.getOrDefault(false)
    }

    private fun statusWord(response: ByteArray): Int {
        if (response.size < 2) return -1
        return ((response[response.size - 2].toInt() and 0xFF) shl 8) or
            (response[response.size - 1].toInt() and 0xFF)
    }

    // ---- Misc ---------------------------------------------------------------------

    private fun checkSecrets() {
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            if (secretsFlow.stateIn(componentActivity.lifecycleScope).value.isMissingSecrets()) {
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_missing_secrets), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showWirelessSettings() {
        Toast.makeText(componentActivity, componentActivity.getString(R.string.scan_nfc_must_be_enabled), Toast.LENGTH_SHORT).show()
        componentActivity.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    /**
     * Best-effort, non-destructive slot-capacity check for NFC-A devices.
     */
    private fun readNfcASlotState(tagCommunicator: TagCommunicator): NfcASlotState {
        val communicatorClass = tagCommunicator.javaClass

        val countMethod = communicatorClass.methods.firstOrNull {
            it.parameterCount == 0 &&
                Number::class.java.isAssignableFrom(it.returnType) &&
                it.name.contains("count", ignoreCase = true) &&
                it.name.contains("character", ignoreCase = true)
        }
        val count = if (countMethod != null) {
            runCatching { (countMethod.invoke(tagCommunicator) as Number).toInt() }.getOrNull()
        } else null

        val activeMethod = communicatorClass.methods.firstOrNull {
            it.parameterCount == 0 && it.returnType == java.lang.Boolean.TYPE &&
                (it.name.contains("active", ignoreCase = true) || it.name.contains("current", ignoreCase = true)) &&
                it.name.contains("character", ignoreCase = true)
        }
        val backupMethod = communicatorClass.methods.firstOrNull {
            it.parameterCount == 0 && it.returnType == java.lang.Boolean.TYPE &&
                it.name.contains("backup", ignoreCase = true) &&
                it.name.contains("character", ignoreCase = true)
        }

        val activePresent = if (activeMethod != null) {
            runCatching { activeMethod.invoke(tagCommunicator) as Boolean }.getOrNull()
        } else null
        val backupPresent = if (backupMethod != null) {
            runCatching { backupMethod.invoke(tagCommunicator) as Boolean }.getOrNull()
        } else null

        if (count == null && activePresent == null && backupPresent == null) {
            Log.w("NFC_A", "Unable to introspect NFC-A slot occupancy; defaulting to non-blocking write path")
        }

        return NfcASlotState(count = count, activePresent = activePresent, backupPresent = backupPresent)
    }

    /**
     * Attempts to mirror official toy behavior by moving the active character to backup.
     * If the library already does this internally, this is a harmless no-op.
     */
    private fun verifyActiveToBackupMigration(
        tagCommunicator: TagCommunicator,
        beforeState: NfcASlotState,
    ): SlotMigrationCheck {
        val migrationResult = moveActiveToBackupIfSupported(tagCommunicator)
        if (migrationResult.attempted && !migrationResult.success) {
            return SlotMigrationCheck(
                canProceed = false,
                message = "Transfer blocked. Could not move active character to backup safely."
            )
        }

        // If we could not introspect occupancy at all, keep legacy behavior and proceed.
        if (!beforeState.hasOccupancySignal()) {
            return SlotMigrationCheck(canProceed = true, message = "")
        }

        val afterState = readNfcASlotState(tagCommunicator)
        val migrationVerified = afterState.backupPresent == true ||
            (beforeState.count != null && afterState.count != null && afterState.count >= beforeState.count)
        if (!migrationVerified) {
            return SlotMigrationCheck(
                canProceed = false,
                message = "Transfer blocked. Backup slot could not be verified after migration."
            )
        }

        return SlotMigrationCheck(canProceed = true, message = "")
    }

    private fun moveActiveToBackupIfSupported(tagCommunicator: TagCommunicator): MigrationInvokeResult {
        val communicatorClass = tagCommunicator.javaClass
        val candidate = communicatorClass.methods.firstOrNull {
            it.parameterCount == 0 &&
                (
                    (it.name.contains("move", ignoreCase = true) && it.name.contains("backup", ignoreCase = true)) ||
                    (it.name.contains("shift", ignoreCase = true) && it.name.contains("backup", ignoreCase = true)) ||
                    (it.name.contains("promote", ignoreCase = true) && it.name.contains("backup", ignoreCase = true))
                )
        }
        if (candidate == null) {
            return MigrationInvokeResult(attempted = false, success = true)
        }

        return runCatching { candidate.invoke(tagCommunicator) }
            .fold(
                onSuccess = { MigrationInvokeResult(attempted = true, success = true) },
                onFailure = {
                    Log.w("NFC_A", "Failed to move active character to backup", it)
                    MigrationInvokeResult(attempted = true, success = false)
                }
            )
    }

    private data class NfcASlotState(
        val count: Int?,
        val activePresent: Boolean?,
        val backupPresent: Boolean?,
    ) {
        fun hasOccupancySignal(): Boolean {
            return count != null || activePresent != null || backupPresent != null
        }

        fun isFull(): Boolean {
            if (count != null) {
                return count >= 2
            }
            if (activePresent != null && backupPresent != null) {
                return activePresent && backupPresent
            }
            return false
        }
    }

    private data class SlotMigrationCheck(
        val canProceed: Boolean,
        val message: String,
    )

    private data class MigrationInvokeResult(
        val attempted: Boolean,
        val success: Boolean,
    )

    override fun characterFromNfc(nfcCharacter: NfcCharacter, onMultipleCards: (List<Card>, NfcCharacter) -> Unit): String {
        return FromNfcConverter(componentActivity = componentActivity).addCharacter(nfcCharacter, onMultipleCards)
    }

    override suspend fun characterToNfc(characterId: Long): NfcCharacter {
        lastRequestedCharacterId = characterId
        val character = ToNfcConverter(componentActivity = componentActivity).characterToNfc(characterId)
        Log.d("CharacterType", character.toString())
        return character
    }

    override fun flushCharacter(cardId: Long) {
        val nfcConverter = FromNfcConverter(componentActivity = componentActivity)
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            if (lastScannedCharacter != null) {
                nfcConverter.addCharacterUsingCard(lastScannedCharacter!!, cardId)
                lastScannedCharacter = null
            }
        }
    }
}