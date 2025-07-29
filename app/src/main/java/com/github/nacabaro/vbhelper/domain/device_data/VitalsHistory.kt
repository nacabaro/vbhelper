package com.github.nacabaro.vbhelper.domain.device_data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserCharacter::class,
            parentColumns = ["id"],
            childColumns = ["charId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VitalsHistory (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val charId: Long,
    val year: Int,
    val month: Int,
    val day: Int,
    val vitalPoints: Int
)