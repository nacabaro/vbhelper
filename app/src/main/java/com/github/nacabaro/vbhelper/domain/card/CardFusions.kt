package com.github.nacabaro.vbhelper.domain.card

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["fromCharaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["toVirusFusion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["toDataFusion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["toVaccineFusion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["toFreeFusion"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CardFusions(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val fromCharaId: Long,
    val toVirusFusion: Long?,
    val toDataFusion: Long?,
    val toVaccineFusion: Long?,
    val toFreeFusion: Long?
)