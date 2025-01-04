package com.github.nacabaro.vbhelper.domain

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Mon::class,
            parentColumns = ["id"],
            childColumns = ["monId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserMonsters::class,
            parentColumns = ["id"],
            childColumns = ["previousStage"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class UserMonsters (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val userId: Int,
    val monId: Int,
    val previousStage: Int?,
    val vitals: Int,
    val trophies: Int,
    val trainingAp: Int,
    val trainingBp: Int,
    val trainingHp: Int,
    val rank: Int, // Maybe use another enum (?)
    val ability: Int,  // Another enum (???)
    val evoTimerLeft: Int,  // Minutes!!
    val limitTimerLeft: Int,  // Minutes!!!!!
    val totalBattles: Int,
    val totalWins: Int
)