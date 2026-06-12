package com.github.cfogrady.vitalwear.common.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        SharedTransferSeenEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class SharedTransferDatabase : RoomDatabase() {
    abstract fun transferSeenDao(): SharedTransferSeenDao
}

