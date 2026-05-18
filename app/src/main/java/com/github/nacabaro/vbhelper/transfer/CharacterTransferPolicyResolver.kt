package com.github.nacabaro.vbhelper.transfer

import com.github.nacabaro.vbhelper.daos.CharacterTransferPolicyDao
import com.github.nacabaro.vbhelper.domain.device_data.CharacterTransferPolicy
import com.github.nacabaro.vbhelper.utils.DeviceType

class CharacterTransferPolicyResolver(
    private val policyDao: CharacterTransferPolicyDao,
) {
    fun resolveExportFormat(
        characterId: Long,
        characterType: DeviceType,
        transport: TransferTransport,
        target: TransferTarget,
        isBemCard: Boolean,
    ): ExportFormat {
        val policy = policyDao.getByCharacterId(characterId)
        return when (transport) {
            TransferTransport.HCE -> policy?.preferredHceExportFormat
                ?: defaultHceExportFormat(characterType, isBemCard, target)
            TransferTransport.NFCA -> policy?.preferredNfcaExportFormat
                ?: defaultNfcaExportFormat(characterType, isBemCard, target)
        }
    }

    fun policyForNfcaImport(
        characterId: Long,
        observedFormat: ExportFormat,
    ): CharacterTransferPolicy {
        val nativeDeviceType = observedFormat.toNativeDeviceType()
        return CharacterTransferPolicy(
            characterId = characterId,
            nativeDeviceType = nativeDeviceType,
            preferredHceExportFormat = defaultHceExportFormat(nativeDeviceType, nativeDeviceType == DeviceType.BEDevice, TransferTarget.VITAL_WEAR),
            preferredNfcaExportFormat = observedFormat,
            lastObservedImportFormat = observedFormat,
            lastTransferTransport = TransferTransport.NFCA,
            lastTransferTarget = TransferTarget.REAL_BRACELET,
            preserveVbRoundTrip = observedFormat == ExportFormat.VB,
            preserveBeRoundTrip = observedFormat == ExportFormat.BE,
        )
    }

    fun policyForHceImport(
        characterId: Long,
        importedCardIsBem: Boolean,
        resolvedDeviceType: DeviceType,
        observedFormat: ExportFormat,
    ): CharacterTransferPolicy {
        val nativeDeviceType = when {
            importedCardIsBem -> DeviceType.BEDevice
            resolvedDeviceType == DeviceType.BEDevice && observedFormat == ExportFormat.BE -> DeviceType.VBDevice
            resolvedDeviceType == DeviceType.VitalWear -> DeviceType.VBDevice
            else -> resolvedDeviceType
        }
        val preferredNfcaExportFormat = if (importedCardIsBem || nativeDeviceType == DeviceType.BEDevice) {
            ExportFormat.BE
        } else {
            ExportFormat.VB
        }

        return CharacterTransferPolicy(
            characterId = characterId,
            nativeDeviceType = nativeDeviceType,
            preferredHceExportFormat = defaultHceExportFormat(nativeDeviceType, importedCardIsBem, TransferTarget.VITAL_WEAR),
            preferredNfcaExportFormat = preferredNfcaExportFormat,
            lastObservedImportFormat = observedFormat,
            lastTransferTransport = TransferTransport.HCE,
            lastTransferTarget = TransferTarget.VITAL_WEAR,
            preserveVbRoundTrip = preferredNfcaExportFormat == ExportFormat.VB,
            preserveBeRoundTrip = importedCardIsBem || observedFormat == ExportFormat.BE,
        )
    }

    private fun defaultHceExportFormat(
        characterType: DeviceType,
        isBemCard: Boolean,
        target: TransferTarget,
    ): ExportFormat {
        if (target != TransferTarget.VITAL_WEAR) {
            return defaultNfcaExportFormat(characterType, isBemCard, target)
        }

        // VitalWear is the HCE endpoint. Default to BE projection so HCE transfers can carry the
        // richer payload, while NFCA policy still controls what is sent back to real bracelets.
        return ExportFormat.BE
    }

    private fun defaultNfcaExportFormat(
        characterType: DeviceType,
        isBemCard: Boolean,
        target: TransferTarget,
    ): ExportFormat {
        require(target == TransferTarget.REAL_BRACELET) {
            "NFCA exports are only supported for real bracelets."
        }
        return if (isBemCard || characterType == DeviceType.BEDevice) {
            ExportFormat.BE
        } else {
            ExportFormat.VB
        }
    }

    private fun ExportFormat.toNativeDeviceType(): DeviceType {
        return when (this) {
            ExportFormat.BE -> DeviceType.BEDevice
            ExportFormat.VB -> DeviceType.VBDevice
        }
    }
}


