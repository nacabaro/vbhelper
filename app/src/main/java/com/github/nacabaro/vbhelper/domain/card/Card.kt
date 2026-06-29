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
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Card

        if (id != other.id) return false
        if (cardId != other.cardId) return false
        if (!logo.contentEquals(other.logo)) return false
        if (logoWidth != other.logoWidth) return false
        if (logoHeight != other.logoHeight) return false
        if (name != other.name) return false
        if (stageCount != other.stageCount) return false
        if (isBEm != other.isBEm) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + cardId
        result = 31 * result + logo.contentHashCode()
        result = 31 * result + logoWidth
        result = 31 * result + logoHeight
        result = 31 * result + name.hashCode()
        result = 31 * result + stageCount
        result = 31 * result + isBEm.hashCode()
        return result
    }
}
