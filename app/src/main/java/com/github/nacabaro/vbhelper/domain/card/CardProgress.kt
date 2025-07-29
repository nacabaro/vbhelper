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
data class CardProgress(
    @PrimaryKey val cardId: Long,
    val currentStage: Int,
    val unlocked: Boolean
)
