package com.github.nacabaro.vbhelper.domain.characters

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["dimId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
/*
 * Character represents a character on a DIM card. There should only be one of these per dimId
 * and monIndex.
 * TODO: Customs will mean this should be unique per cardName and monIndex
 */
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
