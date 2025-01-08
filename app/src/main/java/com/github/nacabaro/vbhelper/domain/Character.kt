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
data class Character (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val dimId: Long,
    val monIndex: Int,
    val name: ByteArray,
    val stage: Int, // These should be replaced with enums
    val attribute: Int, // This one too
    val baseHp: Int,
    val baseBp: Int,
    val baseAp: Int,
    val sprite1: ByteArray,
    val sprite2: ByteArray,
    val nameWidth: Int,
    val nameHeight: Int,
    val spritesWidth: Int,
    val spritesHeight: Int
)
