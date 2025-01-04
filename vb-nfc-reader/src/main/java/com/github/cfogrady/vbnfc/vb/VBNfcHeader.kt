package com.github.cfogrady.vbnfc.vb

import com.github.cfogrady.vbnfc.data.DeviceType
import com.github.cfogrady.vbnfc.data.NfcHeader

class VBNfcHeader(
    deviceType: UShort,
    deviceSubType: UShort,
    vbCompatibleTagIdentifier: ByteArray,
    status: Byte,
    operation: Byte,
    dimId: Byte,
    val reserved: Byte,
    appFlag: Byte,
    nonce: ByteArray
    ) : NfcHeader(
    deviceId = deviceType,
    deviceSubType = deviceSubType,
    vbCompatibleTagIdentifier = vbCompatibleTagIdentifier,
    status = status,
    operation = operation,
    dimIdBytes = byteArrayOf(0, dimId),
    appFlag = appFlag,
    nonce = nonce,
)