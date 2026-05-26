package com.github.nacabaro.vbhelper.domain.card

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CardAdventure(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val characterId: Long,
    val steps: Int,
    val bossHp: Int,
    val bossAp: Int,
    val bossDp: Int,
    val bossBp: Int?
)