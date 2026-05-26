package com.github.nacabaro.vbhelper.domain.card

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.github.cfogrady.vbnfc.data.NfcCharacter

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["fromCharaId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CardCharacter::class,
            parentColumns = ["id"],
            childColumns = ["toCharaId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class CardFusions(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val fromCharaId: Long,
    val attribute: NfcCharacter.Attribute,
    val toCharaId: Long
)