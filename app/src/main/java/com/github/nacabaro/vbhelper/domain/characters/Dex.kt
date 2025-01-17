package com.github.nacabaro.vbhelper.domain.characters

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Dex(
    @PrimaryKey val id: Long = 0,
    val discoveredOn: Long
)