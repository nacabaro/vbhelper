package com.github.nacabaro.vbhelper.domain.device_data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.cfogrady.vbnfc.vb.SpecialMission

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
data class SpecialMissions (
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var characterId: Long,
    var goal: Int,
    val watchId: Int,
    val progress: Int,
    val status: SpecialMission.Status,
    val timeElapsedInMinutes: Int,
    val timeLimitInMinutes: Int,
    val missionType: SpecialMission.Type
)