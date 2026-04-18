package com.github.nacabaro.vbhelper.utils

enum class DeviceType {
    VBDevice, // Bandai Vital Bracelet
    BEDevice, // Bandai BE Bracelet
    VitalWear // WearOS VitalWear app
}

fun detectDeviceType(deviceName: String): DeviceType = when {
    deviceName.startsWith("VitalWear", ignoreCase = true) || deviceName.startsWith("VW-", ignoreCase = true) -> DeviceType.VitalWear
    deviceName.startsWith("BE-", ignoreCase = true) -> DeviceType.BEDevice
    deviceName.startsWith("VB-", ignoreCase = true) -> DeviceType.VBDevice
    else -> DeviceType.VBDevice // Default/fallback, can be refined
}