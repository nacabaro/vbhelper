package com.github.nacabaro.vbhelper.domain.characters

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Sprite(
    @PrimaryKey(autoGenerate = true) val id : Int = 0,
    val spriteIdle1: ByteArray,
    val spriteIdle2: ByteArray,
    val spriteWalk1: ByteArray,
    val spriteWalk2: ByteArray,
    val spriteRun1: ByteArray,
    val spriteRun2: ByteArray,
    val spriteTrain1: ByteArray,
    val spriteTrain2: ByteArray,
    val spriteHappy: ByteArray,
    val spriteSleep: ByteArray,
    val spriteAttack: ByteArray,
    val spriteDodge: ByteArray,
    val width: Int,
    val height: Int
)
