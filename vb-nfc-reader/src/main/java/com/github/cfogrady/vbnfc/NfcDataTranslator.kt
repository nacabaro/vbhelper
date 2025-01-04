package com.github.cfogrady.vbnfc

import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vbnfc.data.NfcHeader

interface NfcDataTranslator {

    // setCharacterInByteArray takes the NfcCharacter and modifies the byte array with character
    // data. At the time of writing this is used to write a parsed character into fresh unparsed
    // device data when sending a character back to the device.
    fun setCharacterInByteArray(character: NfcCharacter, bytes: ByteArray) {
    }

    // finalizeByteArrayFormat finalizes the byte array for NFC format by setting all the
    // checksums, and duplicating the duplicate memory pages.
    fun finalizeByteArrayFormat(bytes: ByteArray)

    // getOperationCommandBytes gets an operation command corresponding to the existing header and
    // the input operation
    fun getOperationCommandBytes(header: NfcHeader, operation: Byte): ByteArray

    // parseNfcCharacter parses the nfc data byte array into an instance of a NfcCharacter object
    fun parseNfcCharacter(bytes: ByteArray): NfcCharacter

    // parseHeader parses the nfc header byte array into an instance of NfcHeader
    fun parseHeader(headerBytes: ByteArray): NfcHeader

    val cryptographicTransformer: CryptographicTransformer

}