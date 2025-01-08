package com.github.nacabaro.vbhelper.domain.device_data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserCharacter::class,
            parentColumns = ["id"],
            childColumns = ["monId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransformationHistory (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monId: Long,
    val toCharIndex: Int,
    val year: Int,
    val month: Int,
    val day: Int
)
