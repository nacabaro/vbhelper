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
            childColumns = ["attribute1Fusion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["attribute2Fusion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["attribute3Fusion"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["attribute4Fusion"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CardFusions(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val fromCharaId: Long,
    val attribute1Fusion: Long?,
    val attribute2Fusion: Long?,
    val attribute3Fusion: Long?,
    val attribute4Fusion: Long?
)