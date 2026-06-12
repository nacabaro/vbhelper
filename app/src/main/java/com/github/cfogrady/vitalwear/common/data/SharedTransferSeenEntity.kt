package com.github.cfogrady.vitalwear.common.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = SharedTransferSeenEntity.TABLE,
    indices = [
        Index(value = ["cardLookupKey", "slotId"], unique = true)
    ]
)
data class SharedTransferSeenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val cardName: String,
    val cardLookupKey: String,
    val slotId: Int,
    val seenAtEpochMillis: Long,
) {
    companion object {
        const val TABLE = "TransferSeen"
    }
}

