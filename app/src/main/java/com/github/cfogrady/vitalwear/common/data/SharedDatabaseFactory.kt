package com.github.cfogrady.vitalwear.common.data

import android.content.Context
import androidx.room.Room

object SharedDatabaseFactory {
    private const val DATABASE_NAME = "shared_transfer.db"

    @Volatile
    private var instance: SharedTransferDatabase? = null

    fun getDatabase(context: Context): SharedTransferDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                SharedTransferDatabase::class.java,
                DATABASE_NAME
            ).build().also { instance = it }
        }
    }
}

