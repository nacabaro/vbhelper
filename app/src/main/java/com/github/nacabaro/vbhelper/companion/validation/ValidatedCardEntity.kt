package com.github.nacabaro.vbhelper.companion.validation

import androidx.room.Entity

@Entity(tableName = ValidatedCardEntity.TABLE, primaryKeys = ["cardId"])
data class ValidatedCardEntity(
    val cardId: Int,
) {
    companion object {
        const val TABLE = "validated"
    }
}

