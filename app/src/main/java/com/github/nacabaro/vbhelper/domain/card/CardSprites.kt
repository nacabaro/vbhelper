package com.github.nacabaro.vbhelper.domain.card

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CardSprites(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sprite: ByteArray,
    val width: Int,
    val height: Int
)
