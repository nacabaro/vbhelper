package com.github.nacabaro.vbhelper.transfer.hce
import android.nfc.tech.IsoDep
import android.os.SystemClock
import android.util.Log
import com.github.cfogrady.vitalwear.protos.Character
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
/**
 * Phone-side ISO-DEP client that drives the VitalWear HCE session.
 * Mirrors the APDU protocol defined in VitalWearHceProtocol on the watch.
 */
class VitalWearHceReaderClient(
    private val isoDep: IsoDep,
    private val fastMode: Boolean = true,
) {
    companion object {
        private const val MIN_CHUNK_SIZE = 256
        private val bestChunkByDevice = linkedMapOf<String, Int>()
    }

    init {
        // Import + DB writes can make COMMIT slower than default transceive time on some devices.
        isoDep.timeout = 10_000
    }

    private val AID = byteArrayOf(
        0xF0.toByte(), 0x56, 0x49, 0x54, 0x41, 0x4C, 0x57, 0x45, 0x41, 0x52
    )
    private val CLA: Byte = 0x80.toByte()
    private val INS_NEGOTIATE: Byte = 0x10
    private val INS_READ_CHUNK: Byte = 0x20
    private val INS_WRITE_CHUNK: Byte = 0x30
    private val INS_COMMIT: Byte = 0x40
    private val INS_STATUS: Byte = 0x50
    private val INS_SYNC_UI: Byte = 0x60
    private val INS_VIBRATE: Byte = 0x70
    private val MODE_WATCH_TO_PHONE: Byte = 0x01
    private val MODE_PHONE_TO_WATCH: Byte = 0x02
    private val VERSION: Byte = 0x01
    private val SW_OK = 0x9000
    private val STATUS_SUCCESS: Byte = 0x04
    private val STATUS_FAILURE: Byte = 0x05
    private val STATUS_SYNCING: Byte = 0x03
    private val STATUS_ARMED_SEND: Byte = 0x01
    private val STATUS_ARMED_RECEIVE: Byte = 0x02
    private val STATUS_IDLE: Byte = 0x00
    private val CONFIRMATION_MAX_POLLS = 84 // ~21s with adaptive delay schedule
    private val PREFERRED_MAX_CHUNK_SIZE = 2048
    // Fast mode is the default; ceremony APDUs are only enabled when fastMode is false.
    private val ENABLE_TRANSFER_CEREMONY = !fastMode

    private data class ReadPayloadResult(
        val character: Character,
        val session: VitalWearHceSessionInfo,
    )

    private data class HceTransferMetrics(
        val direction: String,
        val payloadBytes: Int,
        val negotiatedChunkBytes: Int,
        val apduCountTotal: Int,
        val apduCountData: Int,
        val statusPollCount: Int,
        val tSelectNegotiateMs: Long,
        val tChunkLoopMs: Long,
        val tCommitAckMs: Long,
        val tConfirmMs: Long,
        val tTotalMs: Long,
        val result: String,
    ) {
        fun log() {
            Log.i(
                "HCE_CLIENT_METRICS",
                "dir=$direction payloadBytes=$payloadBytes chunk=$negotiatedChunkBytes apduTotal=$apduCountTotal " +
                    "apduData=$apduCountData polls=$statusPollCount selectNegotiateMs=$tSelectNegotiateMs " +
                    "chunkLoopMs=$tChunkLoopMs commitAckMs=$tCommitAckMs confirmMs=$tConfirmMs totalMs=$tTotalMs result=$result"
            )
        }
    }

    /** MOVE READ: read character bytes first, let caller import/validate, then COMMIT only on success.
     * If [onCharacterRead] returns false, COMMIT is skipped so source can remain on the watch.
     */
    fun moveCharacterFromWatch(onCharacterRead: (Character) -> Boolean): Boolean {
        val readResult = readCharacterFromWatchWithoutCommit()
        if (!onCharacterRead(readResult.character)) {
            return false
        }
        if (!readResult.session.canCommit()) {
            error("Session does not allow COMMIT for watch->phone transfer")
        }
        requireOk(sendApdu(INS_COMMIT, byteArrayOf()), "COMMIT")
        return true
    }

    /** READ: watch has called armSend() — phone reads the character off the watch. */
    fun receiveCharacterFromWatch(): Character {
        val readResult = readCharacterFromWatchWithoutCommit()
        if (!readResult.session.canCommit()) {
            error("Session does not allow COMMIT for watch->phone transfer")
        }
        requireOk(sendApdu(INS_COMMIT, byteArrayOf()), "COMMIT")
        return readResult.character
    }

    private fun readCharacterFromWatchWithoutCommit(): ReadPayloadResult {
        val transferStartMs = SystemClock.elapsedRealtime()
        var apduCount = 0
        var dataApduCount = 0
        selectAid()
        apduCount++
        val session = negotiateSession(MODE_WATCH_TO_PHONE, desiredMaxChunkSize())
        apduCount++
        val negotiatedMs = SystemClock.elapsedRealtime()
        check(session.isWatchToPhone()) { "NEGOTIATE direction mismatch for WATCH_TO_PHONE" }
        Log.d("HCE_CLIENT", "Receiving ${session.payloadLength} bytes in chunks of ${session.maxChunkSize}")
        val payload = ByteArray(session.payloadLength)
        var offset = 0
        while (offset < session.payloadLength) {
            val chunkResponse = sendApdu(INS_READ_CHUNK, intToBytes(offset))
            apduCount++
            dataApduCount++
            requireOk(chunkResponse, "READ_CHUNK @ $offset")
            val chunkData = chunkResponse.dropLast(2).toByteArray()
            chunkData.copyInto(payload, offset)
            offset += chunkData.size
        }
        val chunksDoneMs = SystemClock.elapsedRealtime()
        HceTransferMetrics(
            direction = "WATCH_TO_PHONE_READ",
            payloadBytes = payload.size,
            negotiatedChunkBytes = session.maxChunkSize,
            apduCountTotal = apduCount,
            apduCountData = dataApduCount,
            statusPollCount = 0,
            tSelectNegotiateMs = negotiatedMs - transferStartMs,
            tChunkLoopMs = chunksDoneMs - negotiatedMs,
            tCommitAckMs = 0L,
            tConfirmMs = 0L,
            tTotalMs = chunksDoneMs - transferStartMs,
            result = "read_complete",
        ).log()
        return ReadPayloadResult(
            character = Character.parseFrom(payload),
            session = session,
        )
    }

    /** WRITE: watch has called armReceive() — phone pushes a character to the watch. */
    fun sendCharacterToWatch(character: Character, closeSyncUiAfterCommit: Boolean = true) {
        val session = sendCharacterToWatchInternal(
            character = character,
            closeSyncUiAfterCommit = closeSyncUiAfterCommit,
            requestedChunkSize = desiredMaxChunkSize(),
        )
        rememberBestChunk(session.maxChunkSize)
    }

    private fun sendCharacterToWatchInternal(
        character: Character,
        closeSyncUiAfterCommit: Boolean,
        requestedChunkSize: Int,
    ): VitalWearHceSessionInfo {
        val transferStartMs = SystemClock.elapsedRealtime()
        var apduCount = 0
        var dataApduCount = 0
        selectAid()
        apduCount++
        if (ENABLE_TRANSFER_CEREMONY) {
            // Optional toy-like ceremony; kept behind a toggle to avoid transfer overhead.
            sendApdu(INS_VIBRATE, byteArrayOf())
            apduCount++
            sendApdu(INS_SYNC_UI, byteArrayOf(0x01))
            apduCount++
        }

        val payload = character.toByteArray()
        val session = negotiateSession(MODE_PHONE_TO_WATCH, requestedChunkSize)
        apduCount++
        val negotiatedMs = SystemClock.elapsedRealtime()
        check(session.isPhoneToWatch()) { "NEGOTIATE direction mismatch for PHONE_TO_WATCH" }
        Log.d("HCE_CLIENT", "Sending ${payload.size} bytes in chunks of ${session.maxChunkSize}")
        var offset = 0
        while (offset < payload.size) {
            val chunkEnd = (offset + session.maxChunkSize).coerceAtMost(payload.size)
            val chunk = payload.copyOfRange(offset, chunkEnd)
            requireOk(sendApdu(INS_WRITE_CHUNK, intToBytes(offset) + chunk), "WRITE_CHUNK @ $offset")
            apduCount++
            dataApduCount++
            offset = chunkEnd
        }
        val chunksDoneMs = SystemClock.elapsedRealtime()
        if (!session.canCommit()) {
            error("Session does not allow COMMIT for phone->watch transfer")
        }
        val commitStartMs = SystemClock.elapsedRealtime()
        requireOk(sendApdu(INS_COMMIT, byteArrayOf()), "COMMIT")
        apduCount++
        val commitDoneMs = SystemClock.elapsedRealtime()

        if (ENABLE_TRANSFER_CEREMONY && closeSyncUiAfterCommit) {
            sendApdu(INS_SYNC_UI, byteArrayOf(0x00))
            apduCount++
        }
        HceTransferMetrics(
            direction = "PHONE_TO_WATCH_WRITE",
            payloadBytes = payload.size,
            negotiatedChunkBytes = session.maxChunkSize,
            apduCountTotal = apduCount,
            apduCountData = dataApduCount,
            statusPollCount = 0,
            tSelectNegotiateMs = negotiatedMs - transferStartMs,
            tChunkLoopMs = chunksDoneMs - negotiatedMs,
            tCommitAckMs = commitDoneMs - commitStartMs,
            tConfirmMs = 0L,
            tTotalMs = commitDoneMs - transferStartMs,
            result = "commit_acked",
        ).log()
        return session
    }

    /**
     * WRITE + confirmation polling.
     * Returns true only when the watch reports import success.
     */
    fun sendCharacterToWatchAndConfirm(character: Character): Boolean {
        var lastError: Exception? = null
        for (candidateChunk in chunkCandidates()) {
            val confirmStartMs = SystemClock.elapsedRealtime()
            val session = try {
                sendCharacterToWatchInternal(
                    character = character,
                    closeSyncUiAfterCommit = false,
                    requestedChunkSize = candidateChunk,
                )
            } catch (e: Exception) {
                lastError = e
                Log.w("HCE_CLIENT", "Chunk attempt failed before confirmation at chunk=$candidateChunk", e)
                continue
            }
            if (!session.canPollStatus()) {
                return false
            }
            var polls = 0
            var apduCount = 0
            var result = "timeout"
            val confirmed = runBlocking {
                repeat(CONFIRMATION_MAX_POLLS) {
                    val statusResponse = sendApdu(INS_STATUS, byteArrayOf())
                    apduCount++
                    polls++
                    requireOk(statusResponse, "STATUS")
                    val statusByte = statusResponse.dropLast(2).firstOrNull()
                    Log.d("HCE_CLIENT", "STATUS poll[$it]: statusByte=${statusByte?.toInt()?.and(0xFF)}")
                    if (statusByte == STATUS_SUCCESS) {
                        result = "success"
                        return@runBlocking true
                    }
                    if (statusByte == STATUS_FAILURE) {
                        result = "failure"
                        return@runBlocking false
                    }
                    if (
                        statusByte == STATUS_SYNCING ||
                        statusByte == STATUS_ARMED_RECEIVE ||
                        statusByte == STATUS_ARMED_SEND ||
                        statusByte == STATUS_IDLE
                    ) {
                        delay(delayForPoll(it))
                        return@repeat
                    }
                    // Unknown status byte: keep waiting briefly instead of failing early.
                    delay(delayForPoll(it))
                }
                false
            }
            if (ENABLE_TRANSFER_CEREMONY) {
                // End the sync UI after we have consumed terminal status.
                runCatching {
                    sendApdu(INS_SYNC_UI, byteArrayOf(0x00))
                }
            }
            HceTransferMetrics(
                direction = "PHONE_TO_WATCH_CONFIRM",
                payloadBytes = character.serializedSize,
                negotiatedChunkBytes = session.maxChunkSize,
                apduCountTotal = apduCount,
                apduCountData = 0,
                statusPollCount = polls,
                tSelectNegotiateMs = 0L,
                tChunkLoopMs = 0L,
                tCommitAckMs = 0L,
                tConfirmMs = SystemClock.elapsedRealtime() - confirmStartMs,
                tTotalMs = SystemClock.elapsedRealtime() - confirmStartMs,
                result = "$result(requested=$candidateChunk)",
            ).log()
            if (confirmed) {
                rememberBestChunk(session.maxChunkSize)
                return true
            }
            // Explicit failure means import rejected payload; retrying chunk size won't help.
            if (result == "failure") {
                return false
            }
        }
        lastError?.let { throw it }
        return false
    }

    /**
     * Verification step for two-tap transfer UX:
     * confirms the watch is armed to receive before the actual write step.
     */
    fun verifyWatchReadyToReceive(): Boolean {
        selectAid()
        val session = negotiateSession(MODE_PHONE_TO_WATCH, desiredMaxChunkSize())
        return session.canCommit()
    }

    private fun selectAid() {
        val selectApdu = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, AID.size.toByte()) + AID
        val response = isoDep.transceive(selectApdu)
        if (statusWord(response) != SW_OK) error("SELECT AID failed: ${response.toHex()}")
    }
    private fun sendApdu(ins: Byte, data: ByteArray): ByteArray {
        val apdu = if (data.size <= 0xFF) {
            byteArrayOf(CLA, ins, 0x00, 0x00, data.size.toByte()) + data
        } else {
            byteArrayOf(
                CLA,
                ins,
                0x00,
                0x00,
                0x00,
                ((data.size ushr 8) and 0xFF).toByte(),
                (data.size and 0xFF).toByte(),
            ) + data
        }
        return isoDep.transceive(apdu)
    }

    private fun negotiateSession(mode: Byte, requestedChunkSize: Int): VitalWearHceSessionInfo {
        val negResponse = sendApdu(
            INS_NEGOTIATE,
            byteArrayOf(mode, VERSION) + intToShortBytes(requestedChunkSize)
        )
        requireOk(negResponse, "NEGOTIATE")
        val negotiatedMode = when (negResponse[1]) {
            MODE_WATCH_TO_PHONE -> VitalWearHceTransferDirection.WATCH_TO_PHONE
            MODE_PHONE_TO_WATCH -> VitalWearHceTransferDirection.PHONE_TO_WATCH
            else -> error("NEGOTIATE returned unknown direction: ${negResponse[1]}")
        }
        val maxChunk = ((negResponse[2].toInt() and 0xFF) shl 8) or (negResponse[3].toInt() and 0xFF)
        val payloadLen =
            ((negResponse[4].toInt() and 0xFF) shl 24) or
            ((negResponse[5].toInt() and 0xFF) shl 16) or
            ((negResponse[6].toInt() and 0xFF) shl 8) or
            (negResponse[7].toInt() and 0xFF)
        return VitalWearHceSession(
            direction = negotiatedMode,
            maxChunkSize = maxChunk,
            payloadLength = payloadLen,
        )
    }

    private fun desiredMaxChunkSize(): Int {
        val transceiveLimit = runCatching { isoDep.maxTransceiveLength }.getOrNull()
        val maxByDevice = if (transceiveLimit != null && transceiveLimit > 0) {
            // Extended APDU write overhead: 7-byte header + 4-byte offset.
            (transceiveLimit - 11).coerceAtLeast(1)
        } else {
            PREFERRED_MAX_CHUNK_SIZE
        }
        val cappedByDevice = maxByDevice.coerceAtMost(PREFERRED_MAX_CHUNK_SIZE)
        val cachedBest = cachedBestChunk()
        return if (cachedBest == null) {
            cappedByDevice
        } else {
            cachedBest.coerceIn(MIN_CHUNK_SIZE, cappedByDevice)
        }
    }

    private fun chunkCandidates(): List<Int> {
        val maxChunk = desiredMaxChunkSize()
        val baselineCandidates = listOf(maxChunk, 2048, 1536, 1024, 768, 512, MIN_CHUNK_SIZE)
        return baselineCandidates
            .map { it.coerceAtMost(maxChunk) }
            .filter { it in MIN_CHUNK_SIZE..maxChunk }
            .distinct()
    }

    private fun deviceKey(): String {
        val tagId = isoDep.tag?.id ?: return "unknown-device"
        return tagId.toHex()
    }

    private fun cachedBestChunk(): Int? {
        synchronized(bestChunkByDevice) {
            return bestChunkByDevice[deviceKey()]
        }
    }

    private fun rememberBestChunk(chunkSize: Int) {
        synchronized(bestChunkByDevice) {
            bestChunkByDevice[deviceKey()] = chunkSize
        }
    }

    private fun delayForPoll(pollIndex: Int): Long {
        return when {
            pollIndex < 20 -> 80L
            pollIndex < 56 -> 175L
            else -> 300L
        }
    }

    private fun intToShortBytes(value: Int): ByteArray = byteArrayOf(
        ((value ushr 8) and 0xFF).toByte(),
        (value and 0xFF).toByte()
    )

    private fun requireOk(response: ByteArray, context: String) {
        if (statusWord(response) != SW_OK) error("$context failed: ${response.toHex()}")
    }
    private fun statusWord(response: ByteArray): Int {
        if (response.size < 2) return -1
        return ((response[response.size - 2].toInt() and 0xFF) shl 8) or
                (response[response.size - 1].toInt() and 0xFF)
    }
    private fun intToBytes(value: Int): ByteArray = byteArrayOf(
        ((value ushr 24) and 0xFF).toByte(),
        ((value ushr 16) and 0xFF).toByte(),
        ((value ushr 8)  and 0xFF).toByte(),
        (value and 0xFF).toByte()
    )
    private fun ByteArray.toHex() = joinToString("") { "%02X".format(it) }
}
