package com.github.nacabaro.vbhelper.companion.card

import android.nfc.tech.MifareUltralight
import kotlin.experimental.and
import kotlin.experimental.or

class VBNfcData(nfcData: MifareUltralight) {
    companion object {
        private const val ITEM_ID_BE: UShort = 4u
        private const val STATUS_READY_FLAG: Byte = 0b00000001
        private const val STATUS_DIM_READY_FLAG: Byte = 0b00000010
        private val STATUS_DIM_IS_READY = STATUS_READY_FLAG or STATUS_DIM_READY_FLAG
        private const val OPERATION_READY: Byte = 1
        private const val OPERATION_CHECK_DIM: Byte = 3
    }

    val itemId: UShort
    val status: Byte
    val operation: Byte
    val dimId: UShort

    init {
        val readData = nfcData.transceive(byteArrayOf(0x30, 0x04))
        itemId = readData.getUInt16(4, Endian.Big)
        status = readData[8]
        if (itemId == ITEM_ID_BE) {
            operation = readData[9]
            dimId = readData.getUInt16(10, Endian.Big)
        } else {
            dimId = readData[9].toUShort()
            operation = readData[10]
        }
    }

    fun writeCardCheck(nfcData: MifareUltralight, dimId: UShort) {
        if (itemId == ITEM_ID_BE) {
            val dimData = dimId.toByteArray(endian = Endian.Big)
            nfcData.writePage(6, byteArrayOf(STATUS_READY_FLAG, OPERATION_CHECK_DIM, dimData[0], dimData[1]))
        } else {
            nfcData.writePage(6, byteArrayOf(STATUS_READY_FLAG, dimId.toByte(), OPERATION_CHECK_DIM, 0))
        }
    }

    fun wasCardIdValidated(dimId: UShort): Boolean {
        return dimId == this.dimId && operation == OPERATION_READY && (status and STATUS_DIM_IS_READY == STATUS_DIM_IS_READY)
    }
}

