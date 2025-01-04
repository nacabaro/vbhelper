package com.github.cfogrady.vbnfc.be

import com.github.cfogrady.vbnfc.data.NfcDevice
import java.util.BitSet

class BENfcDevice(
    val gender: Gender,
    val registedDims: ByteArray,
    val currDays: Byte,


    ):
    NfcDevice(
        BitSet(),
    ) {
    enum class Gender {
        Male,
        Female
    }
}