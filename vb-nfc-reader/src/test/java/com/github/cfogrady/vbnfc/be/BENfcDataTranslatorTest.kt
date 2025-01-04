package com.github.cfogrady.vbnfc.be

import com.github.cfogrady.vbnfc.ChecksumCalculator
import com.github.cfogrady.vbnfc.CryptographicTransformer
import com.github.cfogrady.vbnfc.data.NfcCharacter
import io.mockk.mockkClass
import org.junit.Assert
import org.junit.Test

class BENfcDataTranslatorTest {
    @OptIn(ExperimentalStdlibApi::class)
    @Test
    fun testNfcCharacterParsing() {
        val nfcBytes = "000000000000000000000000000000000000000000000000000000000000000010400010040010001000000000001094104000100400100010000000000010940000000000000000000500820302008c0000000000000000000500820302008c000b00030002000b000b000001010028000b00030002000b000b0000010100280464028b04b4000000000000000308b80464028b04b4000000000000000308b80474000000001500000000000000008d0002000000000000000000002401062d240105240104240103240102240101c80024010601240106042401060000008605240116ffffffffffffffff00000038ffffffffffffffff00000000000000f80005000f000a00000000046b0000008d0005000f000a00000000046b0000008d00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000010101010101010100000000000000210202020202020202000000000101003a000000000000000000000000000000000000000000000000000000000000000014c5400000000000000000000000001914c540000000000000000000000000190000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000".hexToByteArray()
        val mockCryptographicTransformer = mockkClass(CryptographicTransformer::class)
        val checksumCalculator = ChecksumCalculator()
        val beNfcDataTranslator = BENfcDataTranslator(mockCryptographicTransformer, checksumCalculator)

        val character = beNfcDataTranslator.parseNfcCharacter(nfcBytes)
        val expectedCharacter = BENfcCharacter(
            dimId = 130u,
            charIndex = 5u,
            stage = 3,
            attribute = NfcCharacter.Attribute.Data,
            ageInDays = 0,
            mood = 100,
            characterCreationFirmwareVersion = FirmwareVersion(1, 1),
            nextAdventureMissionStage = 4,
            vitalPoints = 1204u,
            transformationCountdownInMinutes = 776u,
            injuryStatus = NfcCharacter.InjuryStatus.None,
            trainingPp = 11u,
            currentPhaseBattlesWon = 3u,
            currentPhaseBattlesLost = 2u,
            totalBattlesWon = 11u,
            totalBattlesLost = 11u,
            activityLevel = 2,
            heartRateCurrent = 139u,
            transformationHistory = arrayOf(NfcCharacter.Transformation(0, 36, 1, 6),
                NfcCharacter.Transformation(1, 36, 1, 6),
                NfcCharacter.Transformation(4, 36, 1, 6),
                NfcCharacter.Transformation(5, 36, 1, 22),
                NfcCharacter.Transformation(-1, -1, -1, -1),
                NfcCharacter.Transformation(-1, -1, -1, -1),
                NfcCharacter.Transformation(-1, -1, -1, -1),
                NfcCharacter.Transformation(-1, -1, -1, -1),
                ),
            trainingHp = 5u,
            trainingAp = 15u,
            trainingBp = 10u,
            remainingTrainingTimeInMinutes = 1131u,
            itemEffectMentalStateValue = 0,
            itemEffectMentalStateMinutesRemaining = 0,
            itemEffectActivityLevelValue = 0,
            itemEffectActivityLevelMinutesRemaining = 0,
            itemEffectVitalPointsChangeValue = 0,
            itemEffectVitalPointsChangeMinutesRemaining = 0,
            abilityRarity = NfcCharacter.AbilityRarity.None,
            abilityType = 0u,
            abilityBranch = 0u,
            abilityReset = 0,
            rank = 0,
            itemType = 0,
            itemMultiplier = 0,
            itemRemainingTime = 0,
            appReserved1 = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0),
            appReserved2 = arrayOf(0u, 0u, 0u),
            otp0 = "0101010101010101".hexToByteArray(),
            otp1 = "0202020202020202".hexToByteArray()
        )
        Assert.assertEquals(expectedCharacter, character)
    }
}