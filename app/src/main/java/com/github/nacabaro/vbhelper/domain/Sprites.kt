package com.github.nacabaro.vbhelper.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sprites(
    @PrimaryKey(autoGenerate = true) val id : Int,
    val sprite: ByteArray,
    val width: Int,
    val height: Int
)
