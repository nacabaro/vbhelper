package com.github.nacabaro.vbhelper.temporary_domain

import androidx.room.Entity

@Entity
// Bit lazy, will correct later...
data class TemporaryTransformationHistory (
    val monId: Int,
    val toCharIndex: Byte,
    val yearsSince1988: Byte,
    val month: Byte,
    val day: Byte
)
