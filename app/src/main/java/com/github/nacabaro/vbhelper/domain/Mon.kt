package com.github.nacabaro.vbhelper.domain

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Dim::class,
            parentColumns = ["id"],
            childColumns = ["dimId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Mon (
    @PrimaryKey val id: Int,
    val dimId: Int,
    val monIndex: Int,
    val name: String,
    val stage: Int, // These should be replaced with enums
    val attribute: Int, // This one too
    val baseHp: Int,
    val baseBp: Int,
    val baseAp: Int,
    val evoTime: Int, // In minutes
)