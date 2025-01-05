package com.github.nacabaro.vbhelper.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["monId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Character::class,
            parentColumns = ["id"],
            childColumns = ["nextMon"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Evolutions(
    @PrimaryKey val monId: Int,
    @PrimaryKey val nextMon: Int,
    val trophies: Int,
    val vitals: Int,
    val totalBattles: Int,
    val winRate: Int // Does not need to be a floating point
)
