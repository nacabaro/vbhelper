package com.github.nacabaro.vbhelper.battle

data class PVPDataModel (
    val status: String,
    val state: Int,
    val currentRound: Int,
    val playerHP: Int,
    val opponentHP: Int,
    val playerAttackHit: Boolean,
    val playerAttackDamage: Int,
    val opponentAttackDamage: Int,
    val winner: String,
    val opponentCharaId: String? = null,  // Server provides opponent's charaId from the match
    val playerMaxHP: Int? = null,  // Server should provide max HP for resumed matches
    val opponentMaxHP: Int? = null  // Server should provide max HP for resumed matches
):java.io.Serializable