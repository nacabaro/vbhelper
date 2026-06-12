package com.github.nacabaro.vbhelper.transfer

import com.github.cfogrady.vitalwear.protos.Character

/**
 * Real bracelets talk over NFC-A while VitalWear uses HCE / ISO-DEP.
 * Keep transport, target, and payload format separate so one canonical character can be
 * projected differently depending on where it is being transferred.
 */
enum class TransferTransport {
    NFCA,
    HCE,
}

enum class TransferTarget {
    REAL_BRACELET,
    VITAL_WEAR,
}

enum class ExportFormat {
    VB,
    BE,
}

fun ExportFormat.toTransferDeviceType(): Character.CharacterStats.TransferDeviceType {
    return when (this) {
        ExportFormat.VB -> Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_VB
        ExportFormat.BE -> Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_BE
    }
}

