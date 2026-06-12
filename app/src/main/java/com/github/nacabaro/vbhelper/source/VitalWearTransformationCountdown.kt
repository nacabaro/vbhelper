package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vitalwear.protos.Character
import com.github.nacabaro.vbhelper.utils.DeviceType

internal const val MINIMUM_TRANSFORMATION_COUNTDOWN_MINUTES = 1

internal fun normalizeTransformationCountdownMinutes(
    transformationCountdownMinutes: Int,
    hasPossibleTransformations: Boolean,
): Int {
    val sanitizedCountdown = transformationCountdownMinutes.coerceAtLeast(0)
    if (!hasPossibleTransformations) {
        return sanitizedCountdown
    }
    return sanitizedCountdown.coerceAtLeast(MINIMUM_TRANSFORMATION_COUNTDOWN_MINUTES)
}

internal fun DeviceType.toTransferDeviceType(): Character.CharacterStats.TransferDeviceType {
    return when (this) {
        DeviceType.BEDevice -> Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_BE
        DeviceType.VBDevice,
        DeviceType.VitalWear -> Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_VB
    }
}

internal fun resolveDeviceType(
    transferDeviceType: Character.CharacterStats.TransferDeviceType,
    fallbackIsBeCharacter: Boolean,
): DeviceType {
    return when (transferDeviceType) {
        Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_BE -> DeviceType.BEDevice
        Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_VB -> DeviceType.VBDevice
        Character.CharacterStats.TransferDeviceType.UNRECOGNIZED,
        Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_UNSPECIFIED -> if (fallbackIsBeCharacter) DeviceType.BEDevice else DeviceType.VBDevice
    }
}

