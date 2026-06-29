package com.github.nacabaro.vbhelper.domain.card

import androidx.room.Entity
import androidx.room.Index
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId"])]
)
data class Background (
    @PrimaryKey(autoGenerate = true) val id: Long,
    val cardId: Long,
    val background: ByteArray,
    val backgroundWidth: Int,
    val backgroundHeight: Int
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Background

        if (id != other.id) return false
        if (cardId != other.cardId) return false
        if (!background.contentEquals(other.background)) return false
        if (backgroundWidth != other.backgroundWidth) return false
        if (backgroundHeight != other.backgroundHeight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + cardId.hashCode()
        result = 31 * result + background.contentHashCode()
        result = 31 * result + backgroundWidth
        result = 31 * result + backgroundHeight
        return result
    }
}
