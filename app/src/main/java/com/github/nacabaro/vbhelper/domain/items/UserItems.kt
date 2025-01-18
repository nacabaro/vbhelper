package com.github.nacabaro.vbhelper.domain.items

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Items::class,
            parentColumns = ["id"],
            childColumns = ["itemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserItems(
    @PrimaryKey val itemId: Long,
    val quantity: Int,
)
