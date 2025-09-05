package com.github.nacabaro.vbhelper.domain.card

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.domain.characters.Sprite

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Sprite::class,
            parentColumns = ["id"],
            childColumns = ["spriteId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)

/*
 * Character represents a character on a card. There should only be one of these per dimId
 * and monIndex.
 * TODO: Customs will mean this should be unique per cardName and monIndex
 */
data class CharacterData (
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardId: Long,
    val spriteId: Long,
    val charaIndex: Int,
    val stage: Int, // These should be replaced with enums
    val attribute: NfcCharacter.Attribute, // This one too
    val baseHp: Int,
    val baseBp: Int,
    val baseAp: Int,
    val nameSprite: ByteArray,
    val nameWidth: Int,
    val nameHeight: Int
)