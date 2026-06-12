package com.github.nacabaro.vbhelper.transfer.hce

internal enum class VitalWearHceTransferDirection {
    WATCH_TO_PHONE,
    PHONE_TO_WATCH,
}

internal interface VitalWearHceSessionInfo {
    val direction: VitalWearHceTransferDirection
    val maxChunkSize: Int
    val payloadLength: Int

    fun isWatchToPhone(): Boolean {
        return direction == VitalWearHceTransferDirection.WATCH_TO_PHONE
    }

    fun isPhoneToWatch(): Boolean {
        return direction == VitalWearHceTransferDirection.PHONE_TO_WATCH
    }

    fun canCommit(): Boolean {
        return true
    }

    fun canPollStatus(): Boolean {
        // Import confirmation polling only applies for phone->watch transfers.
        return isPhoneToWatch()
    }
}

internal data class VitalWearHceSession(
    override val direction: VitalWearHceTransferDirection,
    override val maxChunkSize: Int,
    override val payloadLength: Int,
) : VitalWearHceSessionInfo





