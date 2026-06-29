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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sprite

        if (id != other.id) return false
        if (!spriteIdle1.contentEquals(other.spriteIdle1)) return false
        if (!spriteIdle2.contentEquals(other.spriteIdle2)) return false
        if (!spriteWalk1.contentEquals(other.spriteWalk1)) return false
        if (!spriteWalk2.contentEquals(other.spriteWalk2)) return false
        if (!spriteRun1.contentEquals(other.spriteRun1)) return false
        if (!spriteRun2.contentEquals(other.spriteRun2)) return false
        if (!spriteTrain1.contentEquals(other.spriteTrain1)) return false
        if (!spriteTrain2.contentEquals(other.spriteTrain2)) return false
        if (!spriteHappy.contentEquals(other.spriteHappy)) return false
        if (!spriteSleep.contentEquals(other.spriteSleep)) return false
        if (!spriteAttack.contentEquals(other.spriteAttack)) return false
        if (!spriteDodge.contentEquals(other.spriteDodge)) return false
        if (width != other.width) return false
        if (height != other.height) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + spriteIdle1.contentHashCode()
        result = 31 * result + spriteIdle2.contentHashCode()
        result = 31 * result + spriteWalk1.contentHashCode()
        result = 31 * result + spriteWalk2.contentHashCode()
        result = 31 * result + spriteRun1.contentHashCode()
        result = 31 * result + spriteRun2.contentHashCode()
        result = 31 * result + spriteTrain1.contentHashCode()
        result = 31 * result + spriteTrain2.contentHashCode()
        result = 31 * result + spriteHappy.contentHashCode()
        result = 31 * result + spriteSleep.contentHashCode()
        result = 31 * result + spriteAttack.contentHashCode()
        result = 31 * result + spriteDodge.contentHashCode()
        result = 31 * result + width
        result = 31 * result + height
        return result
    }
}
