package com.github.cfogrady.vbnfc.vb

import com.github.cfogrady.vbnfc.CryptographicTransformer
import com.github.cfogrady.vbnfc.NfcDataTranslator
import com.github.cfogrady.vbnfc.TagCommunicator
import com.github.cfogrady.vbnfc.data.DeviceSubType
import com.github.cfogrady.vbnfc.data.DeviceType
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vbnfc.data.NfcHeader
import com.github.cfogrady.vbnfc.getUInt16

class VBNfcDataTranslator(override val cryptographicTransformer: CryptographicTransformer) : NfcDataTranslator {

    companion object {
        const val OPERATION_PAGE: Byte = 0x6
    }

    override fun finalizeByteArrayFormat(bytes: ByteArray) {
        TODO("Not yet implemented")
    }

    override fun getOperationCommandBytes(header: NfcHeader, operation: Byte): ByteArray {
        val vbHeader = header as VBNfcHeader
        return byteArrayOf(TagCommunicator.NFC_WRITE_COMMAND, OPERATION_PAGE, header.status, header.dimIdBytes[1], operation, vbHeader.reserved)
    }

    override fun parseNfcCharacter(bytes: ByteArray): NfcCharacter {
        TODO("Not yet implemented")
    }

    override fun parseHeader(headerBytes: ByteArray): NfcHeader {
        val header = VBNfcHeader(
            deviceType = DeviceType.VitalSeriesDeviceType,
            deviceSubType = headerBytes.getUInt16(6),
            vbCompatibleTagIdentifier = headerBytes.sliceArray(0..3), // this is a magic number used to verify that the tag is a VB.
            status = headerBytes[8],
            dimId = headerBytes[9],
            operation = headerBytes[10],
            reserved = headerBytes[11],
            appFlag = headerBytes[12],
            nonce = headerBytes.sliceArray(13..15)
        )
        return header
    }
}