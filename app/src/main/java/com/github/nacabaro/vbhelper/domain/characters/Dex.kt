package com.github.nacabaro.vbhelper.domain.characters

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.nacabaro.vbhelper.domain.card.CharacterData

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CharacterData::class,
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