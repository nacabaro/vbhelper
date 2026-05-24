package com.github.nacabaro.vbhelper.battle

data class APIBattleCharacter(
    val name: String,
    val namekey: String,
    val charaId: String,
    val stage: Int,
    val attribute: Int,
    val baseHp: Int,
    val currentHp: Int,
    val baseBp: Float,
    val baseAp: Float,
    val displayName: String? = null
)