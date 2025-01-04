package com.github.cfogrady.vbnfc

import com.github.cfogrady.vbnfc.be.BENfcDataTranslator
import com.github.cfogrady.vbnfc.data.DeviceType

class NfcDataTranslatorFactory(
    private val translators: MutableMap<UShort,NfcDataTranslator> = HashMap()
) {


    fun getNfcDataTranslator(deviceTypeId: UShort): NfcDataTranslator {
        val dataTranslator = translators[deviceTypeId]
        if(dataTranslator != null) {
            return dataTranslator
        }
        throw UnsupportedOperationException("Device type ${deviceTypeId} is not yet supported")
    }

    fun addNfcDataTranslator(nfcDataTranslator: NfcDataTranslator, deviceTypeId: UShort) {
        translators[deviceTypeId] = nfcDataTranslator
    }
}