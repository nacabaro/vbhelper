package com.github.nacabaro.vbhelper.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.nacabaro.vbhelper.domain.characters.Card

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["dimId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class DimProgress(
    @PrimaryKey val dimId: Int,
    @PrimaryKey val userId: Int,
    val currentStage: Int,
    val unlocked: Boolean
)
