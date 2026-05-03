package com.github.nacabaro.vbhelper.source

import com.github.cfogrady.vitalwear.protos.Character
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.utils.DeviceType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class VitalWearCharacterImporterTest {
    @Test
    fun normalizeTransformationCountdownMinutes_keepsOneMinuteMinimumWhenCharacterCanStillEvolve() {
        assertEquals(1, normalizeTransformationCountdownMinutes(0, hasPossibleTransformations = true))
    }

    @Test
    fun normalizeTransformationCountdownMinutes_preservesPositiveCountdownWhenCharacterCanStillEvolve() {
        assertEquals(12, normalizeTransformationCountdownMinutes(12, hasPossibleTransformations = true))
    }

    @Test
    fun normalizeTransformationCountdownMinutes_allowsZeroWhenNoTransformationsRemain() {
        assertEquals(0, normalizeTransformationCountdownMinutes(0, hasPossibleTransformations = false))
    }

    @Test
    fun resolveDeviceType_prefersExplicitTransferTypeOverFallbackHeuristic() {
        assertEquals(
            DeviceType.VBDevice,
            resolveDeviceType(
                transferDeviceType = Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_VB,
                fallbackIsBeCharacter = true,
            )
        )
    }

    @Test
    fun resolveDeviceType_fallsBackToBeWhenTransferTypeIsUnspecified() {
        assertEquals(
            DeviceType.BEDevice,
            resolveDeviceType(
                transferDeviceType = Character.CharacterStats.TransferDeviceType.TRANSFER_DEVICE_TYPE_UNSPECIFIED,
                fallbackIsBeCharacter = true,
            )
        )
    }

    @Test
    fun resolveDeviceType_usesFallbackWhenTransferTypeIsUnrecognized() {
        assertEquals(
            DeviceType.VBDevice,
            resolveDeviceType(
                transferDeviceType = Character.CharacterStats.TransferDeviceType.UNRECOGNIZED,
                fallbackIsBeCharacter = false,
            )
        )
    }

    @Test
    fun selectImportedCard_prefersUniqueCardIdWhenNameWasRenamed() {
        val selected = selectImportedCard(
            candidates = listOf(
                testCard(id = 1, cardId = 77, name = "My Custom DIM"),
                testCard(id = 2, cardId = 88, name = "Other"),
            ),
            incomingCardName = "Impulse City",
            incomingCardId = 77,
        )

        assertNotNull(selected)
        assertEquals("My Custom DIM", selected?.name)
    }

    @Test
    fun selectImportedCard_matchesNormalizedRenamedNameWhenCardIdMissing() {
        val selected = selectImportedCard(
            candidates = listOf(
                testCard(id = 1, cardId = 0, name = "Impulse City!"),
            ),
            incomingCardName = "impulse-city",
            incomingCardId = null,
        )

        assertNotNull(selected)
        assertEquals("Impulse City!", selected?.name)
    }

    @Test
    fun cardNamesMatch_ignoresCaseAndPunctuation() {
        assertEquals(true, cardNamesMatch("My DIM: Zero", "my-dim zero"))
    }

    private fun testCard(id: Long, cardId: Int, name: String): Card {
        return Card(
            id = id,
            cardId = cardId,
            logo = byteArrayOf(),
            logoWidth = 0,
            logoHeight = 0,
            name = name,
            stageCount = 0,
            isBEm = false,
        )
    }
}

