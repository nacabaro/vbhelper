package com.github.nacabaro.vbhelper.domain.device_data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.nacabaro.vbhelper.domain.card.CharacterData

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = UserCharacter::class,
            parentColumns = ["id"],
            childColumns = ["monId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CharacterData::class,
            parentColumns = ["id"],
            childColumns = ["stageId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransformationHistory (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val monId: Long,
    val stageId: Long,
    val transformationDate: Long
)
