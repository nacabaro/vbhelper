package com.github.nacabaro.vbhelper.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Dim::class,
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
