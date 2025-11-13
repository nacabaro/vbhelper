package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import kotlinx.coroutines.flow.Flow

class ItemsRepository(
    private val db: AppDatabase
) {
    fun getAllItems(): Flow<List<ItemDtos.ItemsWithQuantities>> {
        return db.itemDao().getAllItems()
    }

    fun getUserItems(): Flow<List<ItemDtos.ItemsWithQuantities>> {
        return db.itemDao().getAllUserItems()
    }
}