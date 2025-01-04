package com.github.cfogrady.vbnfc

import android.nfc.tech.NfcA
import android.util.Log
import com.github.cfogrady.vbnfc.be.BENfcDataTranslator
import com.github.cfogrady.vbnfc.data.DeviceType
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vbnfc.data.NfcHeader
import java.nio.ByteOrder

class TagCommunicator(
    private val nfcData: NfcA,
    private val checksumCalculator: ChecksumCalculator,
    private val nfcDataTranslatorFactory: NfcDataTranslatorFactory,
    ) {

    companion object {
        const val TAG = "VBNfcHandler"
        const val HEADER_PAGE: Byte = 0x04
        const val NFC_PASSWORD_COMMAND: Byte = 0x1b
        const val NFC_READ_COMMAND: Byte = 0x30
        const val NFC_WRITE_COMMAND: Byte = 0xA2.toByte()

        const val STATUS_IDLE: Byte = 0
        const val STATUS_READY: Byte = 1

        const val OPERATION_IDLE: Byte = 0
        const val OPERATION_READY: Byte = 1
        const val OPERATION_TRANSFERRED_TO_APP: Byte = 2
        const val OPERATION_CHECK_DIM: Byte = 3
        const val OPERATION_TRANSFERED_TO_DEVICE: Byte = 4
        const val START_DATA_PAGE = 8
        const val LAST_DATA_PAGE = 220 // technically 223, but we read 4 pages at a time.

        fun getInstance(nfcData: NfcA, deviceTypeIdSecrets: Map<UShort, CryptographicTransformer>): TagCommunicator {
            val checksumCalculator = ChecksumCalculator()
            val deviceToTranslator = HashMap<UShort, NfcDataTranslator>()
            for (keyValue in deviceTypeIdSecrets) {
                when(keyValue.key) {
                    DeviceType.VitalBraceletBEDeviceType -> {
                        deviceToTranslator[keyValue.key] = BENfcDataTranslator(keyValue.value, checksumCalculator)
                    }
                    else -> {
                        throw IllegalArgumentException("DeviceId ${keyValue.key} Provided Without Known Parser")
                    }
                }
            }
            return TagCommunicator(nfcData, checksumCalculator, NfcDataTranslatorFactory(deviceToTranslator))
        }

    }

    data class DeviceTranslatorAndHeader(val nfcHeader: NfcHeader, val translator: NfcDataTranslator)

    @OptIn(ExperimentalStdlibApi::class)
    fun receiveCharacter(): NfcCharacter {
        val translatorAndHeader = fetchDeviceTranslatorAndHeader()
        val header = translatorAndHeader.nfcHeader
        val translator = translatorAndHeader.translator
        Log.i(TAG, "Writing to make ready for operation")
        nfcData.transceive(translator.getOperationCommandBytes(header, OPERATION_READY))
        Log.i(TAG, "Authenticating")

        passwordAuth(translator.cryptographicTransformer)
        Log.i(TAG, "Reading Character")
        val encryptedCharacterData = readNfcData()
        Log.i(TAG, "Raw NFC Data Received: ${encryptedCharacterData.toHexString()}")
        val decryptedCharacterData = translator.cryptographicTransformer.decryptData(encryptedCharacterData, nfcData.tag.id)
        checksumCalculator.checkChecksums(decryptedCharacterData)
        val nfcCharacter = translator.parseNfcCharacter(decryptedCharacterData)
        Log.i(TAG, "Known Character Stats: $nfcCharacter")
        // Not ready to lose any of my mons in this...
        //Log.i(TAG, "Signaling operation complete")
        //nfcData.transceive(translator.getOperationCommandBytes(header, OPERATION_TRANSFERRED_TO_APP))
        return nfcCharacter
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun fetchDeviceTranslatorAndHeader(): DeviceTranslatorAndHeader {
        val readData = nfcData.transceive(byteArrayOf(NFC_READ_COMMAND, HEADER_PAGE))
        Log.i("TagCommunicator", "First 4 Pages: ${readData.toHexString()}")
        val deviceTypeId = readData.getUInt16(4, ByteOrder.BIG_ENDIAN)
        val translator = nfcDataTranslatorFactory.getNfcDataTranslator(deviceTypeId)
        val header = translator.parseHeader(readData)
        return DeviceTranslatorAndHeader(header, translator)
    }

    private fun readNfcData(): ByteArray {
        val result = ByteArray(((LAST_DATA_PAGE +4)- START_DATA_PAGE) * 4)
        for (page in START_DATA_PAGE..LAST_DATA_PAGE step 4) {
            val pages = nfcData.transceive(byteArrayOf(NFC_READ_COMMAND, page.toByte()))
            if (pages.size < 16) {
                throw Exception("Failed to read page: $page")
            }
            System.arraycopy(pages, 0, result, (page - START_DATA_PAGE)*4, pages.size)
        }
        return result
    }

    fun prepareDIMForCharacter(dimId: UShort) {
        val translatorAndHeader = fetchDeviceTranslatorAndHeader()
        val header = translatorAndHeader.nfcHeader
        val translator = translatorAndHeader.translator
        // set app nonce to device ensure when we send back the character that we are preparing
        // the same device we send to
        nfcData.transceive(translator.getOperationCommandBytes(header, OPERATION_READY))
        // app authenticates, and reads everything, and checks the version
        // The version check is only for the BE when transfering from DIM=0 (pulsemon).
        // This was from the bug when the BE first came out.
        // Check (page 103 [0:1] != 1, 0)
        header.setDimId(dimId)
        nfcData.transceive(translator.getOperationCommandBytes(header, OPERATION_CHECK_DIM))
    }

    private fun defaultNfcDataGenerator(translator: NfcDataTranslator, character: NfcCharacter): ByteArray {
        val currentNfcData = readNfcData()
        val newNfcData = translator.cryptographicTransformer.decryptData(currentNfcData, nfcData.tag.id)
        translator.setCharacterInByteArray(character, newNfcData)
        checksumCalculator.recalculateChecksums(newNfcData)
        translator.finalizeByteArrayFormat(newNfcData)
        return newNfcData
    }

    // sendCharacter sends a character to the device using the nfcDataGenerator function. The
    // default nfcDataGenerator reads the current data of the device and applies the new character
    // data to the read data and prepares that to be sent back to the device. The nfcDataGenerator
    // is a functor which takes in the NfcDataTranslator for the device and the NfcCharacter
    // provided to the sendCharacter method and returns the decrypted byte array data to be sent
    // back to the device.
    @OptIn(ExperimentalStdlibApi::class)
    fun sendCharacter(character: NfcCharacter, nfcDataGenerator: (NfcDataTranslator, NfcCharacter) -> ByteArray = this::defaultNfcDataGenerator) {
        Log.i(TAG, "Sending Character: $character")
        val deviceTranslatorAndHeader = fetchDeviceTranslatorAndHeader()
        val translator = deviceTranslatorAndHeader.translator
        val header = deviceTranslatorAndHeader.nfcHeader

        // check the nonce
        // if it's not expected, then the bracelet isn't ready

        // Check the product and device ids that they match the target.
        // This check relies on the app categories of BE, Vital Hero, and Vital Series.

        // ensure the dim id matches the expected
        if (character.dimId != header.getDimId()) {
            throw IllegalArgumentException("Device is ready for DIM ${header.getDimId()}, but attempted to send ${character.dimId}")
        }

        // update the memory data
        nfcData.transceive(translator.getOperationCommandBytes(header, OPERATION_READY))
        passwordAuth(translator.cryptographicTransformer)

        var newNfcData = nfcDataGenerator(translator, character)
        newNfcData = translator.cryptographicTransformer.encryptData(newNfcData, nfcData.tag.id)
        Log.i(TAG, "Sending Character: ${newNfcData.toHexString()}")


        // write nfc data
        val pagedData = ConvertToPages(newNfcData)
        for(pageToWriteIdx in 8..<pagedData.size) {
            val pageToWrite = pagedData[pageToWriteIdx]
            nfcData.transceive(byteArrayOf(NFC_WRITE_COMMAND, pageToWriteIdx.toByte(), pageToWrite[0], pageToWrite[1], pageToWrite[2], pageToWrite[3]))
        }


        nfcData.transceive(translator.getOperationCommandBytes(header, OPERATION_TRANSFERED_TO_DEVICE))

    }

    @OptIn(ExperimentalStdlibApi::class)
    fun passwordAuth(cryptographicTransformer: CryptographicTransformer) {
        val tagId = nfcData.tag.id
        Log.i(TAG, "TagId: ${tagId.toHexString()}")
        val password = cryptographicTransformer.createNfcPassword(tagId)
        try {
            val result = nfcData.transceive(byteArrayOf(NFC_PASSWORD_COMMAND, password[0], password[1], password[2], password[3]))
            Log.i(TAG, "PasswordAuth Result: ${result.toHexString()}")
            if (result.size == 1) {
                throw AuthenticationException("Authentication failed. Result: ${result.toHexString()}")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception: ${e.message}")
        }
    }

    // addDataTranslator adds a new data translator to be used with the specified deviceTypeId.
    // This can be used to keep the same general communication protocol, but allows for a different
    // parsing of the data.
    fun addDataTranslator(nfcDataTranslator: NfcDataTranslator, deviceTypeId: UShort) {
        nfcDataTranslatorFactory.addNfcDataTranslator(nfcDataTranslator, deviceTypeId)
    }

    class AuthenticationException(message: String): Exception(message)
}