package com.github.nacabaro.vbhelper.domain.device_data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class VitalWearCharacterSettings(
    @PrimaryKey val characterId: Long,
    val trainingInBackground: Boolean = false,
    val allowedBattles: Int = 1,
    val accumulatedDailyInjuries: Int = 0,
)

