package com.github.nacabaro.vbhelper.companion.validation

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ValidatedCardEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class CompanionValidatedDatabase : RoomDatabase() {
    abstract fun validatedCardDao(): ValidatedCardDao
}

