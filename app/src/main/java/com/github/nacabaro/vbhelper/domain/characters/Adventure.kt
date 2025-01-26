package com.github.nacabaro.vbhelper.domain.characters

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserCharacter::class,
            parentColumns = ["id"],
            childColumns = ["characterId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Adventure(
    @PrimaryKey val characterId: Long,
    val originalDuration: Long,
    val finishesAdventure: Long
)
