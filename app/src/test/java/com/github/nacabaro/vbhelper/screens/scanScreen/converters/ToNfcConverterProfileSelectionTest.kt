package com.github.nacabaro.vbhelper.screens.scanScreen.converters

import com.github.nacabaro.vbhelper.utils.DeviceType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ToNfcConverterProfileSelectionTest {
    @Test
    fun shouldEncodeAsBem_usesStoredTypeWhenForcedProfileMissing() {
        assertTrue(ToNfcConverter.shouldEncodeAsBem(null, DeviceType.BEDevice))
        assertFalse(ToNfcConverter.shouldEncodeAsBem(null, DeviceType.VBDevice))
    }

    @Test
    fun shouldEncodeAsBem_forcedVBWinsOverStoredBE() {
        assertFalse(ToNfcConverter.shouldEncodeAsBem(DeviceType.VBDevice, DeviceType.BEDevice))
    }

    @Test
    fun shouldEncodeAsBem_forcedBEWinsOverStoredVB() {
        assertTrue(ToNfcConverter.shouldEncodeAsBem(DeviceType.BEDevice, DeviceType.VBDevice))
    }
}

