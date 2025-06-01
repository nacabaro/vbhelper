package com.github.nacabaro.vbhelper.domain.device_data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserCharacter::class,
            parentColumns = ["characterId"],
            childColumns = ["id"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class VitalsHistory (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val characterId: Long,
    val date: Long,
    val vitalPoints: Int
)