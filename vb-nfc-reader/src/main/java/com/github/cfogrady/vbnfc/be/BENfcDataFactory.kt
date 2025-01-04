package com.github.cfogrady.vbnfc.be

import com.github.cfogrady.vbnfc.ChecksumCalculator

// Obsolete... being held onto for reference to device side data.
class BENfcDataFactory(checksumCalculator: ChecksumCalculator = ChecksumCalculator()) {



    fun buildBENfcDevice(bytes: ByteArray): BENfcDevice {
        return BENfcDevice(
            gender = BENfcDevice.Gender.entries[bytes[12].toInt()],
            registedDims = bytes.sliceArray(32..<32+15),
            currDays = bytes[78]
        )



//        reserved1 = readByte(107)
//        saveFirmwareVersion = readUShort(108)
//        advMissionStage = readByte(128)
//
//
//
//
//        reserved2 = readByte(140)
//
//        reserved3 = readUShort(162)
//        year = readByte(164)
//        month = readByte(165)
//        day = readByte(166)
//        vitalPointsHistory0 = readUShort(176)
//        vitalPointsHistory1 = readUShort(178)
//        vitalPointsHistory2 = readUShort(180)
//        vitalPointsHistory3 = readUShort(182)
//        vitalPointsHistory4 = readUShort(184)
//        vitalPointsHistory5 = readUShort(186)
//        year0PreviousVitalPoints = readByte(188)
//        month0PreviousVitalPoints = readByte(189)
//        day0PreviousVitalPoints = readByte(190)
//        year1PreviousVitalPoints = readByte(192)
//        month1PreviousVitalPoints = readByte(193)
//        day1PreviousVitalPoints = readByte(194)
//        year2PreviousVitalPoints = readByte(195)
//        month2PreviousVitalPoints = readByte(196)
//        day2PreviousVitalPoints = readByte(197)
//        year3PreviousVitalPoints = readByte(198)
//        month3PreviousVitalPoints = readByte(199)
//        day3PreviousVitalPoints = readByte(200)
//        year4PreviousVitalPoints = readByte(201)
//        month4PreviousVitalPoints = readByte(202)
//        day4PreviousVitalPoints = readByte(203)
//        year5PreviousVitalPoints = readByte(204)
//        month5PreviousVitalPoints = readByte(205)
//        day5PreviousVitalPoints = readByte(206)
//
//        reserved4 = readUShort(262)
//        reserved5 = readByte(264)
//        questionMark = readByte(265)
//        reserved6 = readByte(289)
//        reserved7 = readByte(291)
//
//        reserved8 = readUShort(297)
//        reserved9 = readUShortArray(376, 2)
//
    }

}