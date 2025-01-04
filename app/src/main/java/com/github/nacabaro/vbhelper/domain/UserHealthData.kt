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
        )
    ]
)
data class UserHealthData(
    @PrimaryKey val userId: Int,
    val tamerRank: Int, // Old VB thingy, will probably go unused at first
    val totalSteps: Int
)
