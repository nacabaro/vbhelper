package com.github.cfogrady.vbnfc.data

import com.github.cfogrady.vbnfc.NfcDataTranslator
import com.github.cfogrady.vbnfc.getUInt16
import com.github.cfogrady.vbnfc.toByteArray
import java.nio.ByteOrder

open class NfcHeader (
    val deviceId: UShort,
    val deviceSubType: UShort,
    val vbCompatibleTagIdentifier: ByteArray, // this is a magic number used to verify that the tag is a VB.
    val status: Byte,
    val operation: Byte,
    var dimIdBytes: ByteArray,
    val appFlag: Byte,
    val nonce: ByteArray,
) {

    fun getDimId(): UShort {
        return dimIdBytes.getUInt16(0, ByteOrder.BIG_ENDIAN)
    }

    fun setDimId(dimId: UShort) {
        dimIdBytes = dimId.toByteArray(ByteOrder.BIG_ENDIAN)
    }
}