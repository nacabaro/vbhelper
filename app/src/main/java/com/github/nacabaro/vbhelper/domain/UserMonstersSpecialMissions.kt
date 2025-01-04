package com.github.nacabaro.vbhelper.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserMonsters::class,
            parentColumns = ["id"],
            childColumns = ["monId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserMonstersSpecialMissions(
    @PrimaryKey val monId: Int,
    val slot1: Int,
    val timeLeft1: Int,
    val progression1: Int,
    val slot2: Int,
    val timeLeft2: Int,
    val progression2: Int,
    val slot3: Int,
    val timeLeft3: Int,
    val progression3: Int,
    val slot4: Int,
    val timeLeft4: Int,
    val progression4: Int
)

// Not really proud of this one boss
