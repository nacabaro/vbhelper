package com.github.nacabaro.vbhelper.domain.card

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Card(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Int,
    val logo: ByteArray,
    val logoWidth: Int,
    val logoHeight: Int,
    val name: String,
    val stageCount: Int,
    val isBEm: Boolean
)
