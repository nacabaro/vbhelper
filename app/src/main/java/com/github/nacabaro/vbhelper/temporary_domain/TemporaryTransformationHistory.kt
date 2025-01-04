package com.github.nacabaro.vbhelper.temporary_domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = TemporaryCharacterData::class,
            parentColumns = ["id"],
            childColumns = ["monId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
// Bit lazy, will correct later...
data class TemporaryTransformationHistory (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monId: Long,
    val toCharIndex: Int,
    val yearsSince1988: Int,
    val month: Int,
    val day: Int
)
