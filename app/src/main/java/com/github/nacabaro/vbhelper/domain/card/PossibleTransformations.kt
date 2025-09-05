package com.github.nacabaro.vbhelper.domain.card

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CharacterData::class,
            parentColumns = ["id"],
            childColumns = ["charaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CharacterData::class,
            parentColumns = ["id"],
            childColumns = ["toCharaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PossibleTransformations (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var charaId: Long,
    val requiredVitals: Int,
    val requiredTrophies: Int,
    val requiredBattles: Int,
    val requiredWinRate: Int,
    val changeTimerHours: Int,
    val requiredAdventureLevelCompleted: Int,
    val toCharaId: Long?
)