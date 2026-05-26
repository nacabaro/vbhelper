package com.github.nacabaro.vbhelper.domain.card

import androidx.room.Entity
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
    ]
)
data class Background (
    @PrimaryKey(autoGenerate = true) val id: Long,
    val cardId: Long,
    val background: ByteArray,
    val backgroundWidth: Int,
    val backgroundHeight: Int
)