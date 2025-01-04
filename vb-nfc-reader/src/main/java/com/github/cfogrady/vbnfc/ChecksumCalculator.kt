package com.github.cfogrady.vbnfc

import java.lang.IllegalStateException

class ChecksumCalculator {
    companion object {
        private val PagesWithChecksum = hashSetOf(8, 16, 24, 32, 40, 48, 52, 56, 60, 64, 68, 72, 76, 80, 84, 104, 192, 200, 208, 216)
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun checkChecksums(data: ByteArray) {
        operateOnChecksums(data) { checksumByte, checksumIdx ->
            if (checksumByte != data[checksumIdx]) {
                throw IllegalStateException("Checksum ${checksumByte.toHexString()} doesn't match expected ${data[checksumIdx].toHexString()}")
            }
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun recalculateChecksums(data: ByteArray) {
        operateOnChecksums(data) { checksumByte, checksumIdx ->
            data[checksumIdx] = checksumByte
        }
    }

    private fun operateOnChecksums(data: ByteArray, operator: (Byte, Int)->Unit) {
        // loop through all data
        for(i in data.indices step 16) {
            val page = i/4 + 8 // first 8 pages are header data and not part of the character data
            if (PagesWithChecksum.contains(page)) {
                var sum = 0
                val checksumIndex = i + 15
                for(j in i..<checksumIndex) {
                    sum += data[j]
                }
                val checksumByte = (sum and 0xff).toByte()
                operator(checksumByte, checksumIndex)
            }
        }
    }
}