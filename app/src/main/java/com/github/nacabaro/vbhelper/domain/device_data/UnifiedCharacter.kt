package com.github.nacabaro.vbhelper.domain.device_data

import com.github.nacabaro.vbhelper.utils.DeviceType

/**
 * UnifiedCharacter provides a single access point for common character data
 * and bridges the specific data for VB and BE characters.
 */
data class UnifiedCharacter(
    val userCharacter: UserCharacter,
    val vbData: VBCharacterData? = null,
    val beData: BECharacterData? = null
) {
    val characterType: DeviceType get() = userCharacter.characterType

    fun isVB() = characterType == DeviceType.VBDevice
    fun isBE() = characterType == DeviceType.BEDevice

    // Unified accessors for core stats
    val trophies: Int get() = userCharacter.trophies
    val ageInDays: Int get() = userCharacter.ageInDays
    val mood: Int get() = userCharacter.mood
    val vitalPoints: Int get() = userCharacter.vitalPoints
    val injuryStatus = userCharacter.injuryStatus
    
    // BE specific accessors with defaults
    val trainingHp: Int get() = beData?.trainingHp ?: 0
    val trainingAp: Int get() = beData?.trainingAp ?: 0
    val trainingBp: Int get() = beData?.trainingBp ?: 0
    val abilityRarity = beData?.abilityRarity
}
