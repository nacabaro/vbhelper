package com.github.nacabaro.vbhelper.transfer.hce
import android.nfc.tech.IsoDep
import android.util.Log
import com.github.cfogrady.vitalwear.protos.Character
/**
 * Phone-side ISO-DEP client that drives the VitalWear HCE session.
 * Mirrors the APDU protocol defined in VitalWearHceProtocol on the watch.
 */
class VitalWearHceReaderClient(private val isoDep: IsoDep) {
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
    private val MODE_WATCH_TO_PHONE: Byte = 0x01
    private val MODE_PHONE_TO_WATCH: Byte = 0x02
    private val VERSION: Byte = 0x01
    private val SW_OK = 0x9000
    /** MOVE READ: read character bytes first, let caller import/validate, then COMMIT only on success.
     * If [onCharacterRead] returns false, COMMIT is skipped so source can remain on the watch.
     */
    fun moveCharacterFromWatch(onCharacterRead: (Character) -> Boolean): Boolean {
        val character = readCharacterFromWatchWithoutCommit()
        if (!onCharacterRead(character)) {
            return false
        }
        requireOk(sendApdu(INS_COMMIT, byteArrayOf()), "COMMIT")
        return true
    }

    /** READ: watch has called armSend() — phone reads the character off the watch. */
    fun receiveCharacterFromWatch(): Character {
        val character = readCharacterFromWatchWithoutCommit()
        requireOk(sendApdu(INS_COMMIT, byteArrayOf()), "COMMIT")
        return character
    }

    private fun readCharacterFromWatchWithoutCommit(): Character {
        selectAid()
        val negResponse = sendApdu(INS_NEGOTIATE, byteArrayOf(MODE_WATCH_TO_PHONE, VERSION))
        requireOk(negResponse, "NEGOTIATE")
        val maxChunk = ((negResponse[2].toInt() and 0xFF) shl 8) or (negResponse[3].toInt() and 0xFF)
        val payloadLen =
            ((negResponse[4].toInt() and 0xFF) shl 24) or
            ((negResponse[5].toInt() and 0xFF) shl 16) or
            ((negResponse[6].toInt() and 0xFF) shl 8) or
            (negResponse[7].toInt() and 0xFF)
        Log.d("HCE_CLIENT", "Receiving $payloadLen bytes in chunks of $maxChunk")
        val payload = ByteArray(payloadLen)
        var offset = 0
        while (offset < payloadLen) {
            val chunkResponse = sendApdu(INS_READ_CHUNK, intToBytes(offset))
            requireOk(chunkResponse, "READ_CHUNK @ $offset")
            val chunkData = chunkResponse.dropLast(2).toByteArray()
            chunkData.copyInto(payload, offset)
            offset += chunkData.size
        }
        return Character.parseFrom(payload)
    }

    /** WRITE: watch has called armReceive() — phone pushes a character to the watch. */
    fun sendCharacterToWatch(character: Character) {
        selectAid()
        val payload = character.toByteArray()
        val negResponse = sendApdu(INS_NEGOTIATE, byteArrayOf(MODE_PHONE_TO_WATCH, VERSION))
        requireOk(negResponse, "NEGOTIATE")
        val maxChunk = ((negResponse[2].toInt() and 0xFF) shl 8) or (negResponse[3].toInt() and 0xFF)
        Log.d("HCE_CLIENT", "Sending ${payload.size} bytes in chunks of $maxChunk")
        var offset = 0
        while (offset < payload.size) {
            val chunkEnd = (offset + maxChunk).coerceAtMost(payload.size)
            val chunk = payload.copyOfRange(offset, chunkEnd)
            requireOk(sendApdu(INS_WRITE_CHUNK, intToBytes(offset) + chunk), "WRITE_CHUNK @ $offset")
            offset = chunkEnd
        }
        requireOk(sendApdu(INS_COMMIT, byteArrayOf()), "COMMIT")
    }

    /**
     * Verification step for two-tap transfer UX:
     * confirms the watch is armed to receive before the actual write step.
     */
    fun verifyWatchReadyToReceive(): Boolean {
        selectAid()
        val negResponse = sendApdu(INS_NEGOTIATE, byteArrayOf(MODE_PHONE_TO_WATCH, VERSION))
        requireOk(negResponse, "NEGOTIATE")
        return true
    }

    private fun selectAid() {
        val selectApdu = byteArrayOf(0x00, 0xA4.toByte(), 0x04, 0x00, AID.size.toByte()) + AID
        val response = isoDep.transceive(selectApdu)
        if (statusWord(response) != SW_OK) error("SELECT AID failed: ${response.toHex()}")
    }
    private fun sendApdu(ins: Byte, data: ByteArray): ByteArray {
        val apdu = byteArrayOf(CLA, ins, 0x00, 0x00, data.size.toByte()) + data
        return isoDep.transceive(apdu)
    }
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
