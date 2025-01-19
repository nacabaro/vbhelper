package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.items.Items
import com.github.nacabaro.vbhelper.dtos.ItemDtos

class ItemsRepository(
    private val db: AppDatabase
) {
    suspend fun getAllItems(): List<ItemDtos.ItemsWithQuantities> {
        return db.itemDao().getAllItems()
    }

    suspend fun getUserItems(): List<ItemDtos.ItemsWithQuantities> {
        return db.itemDao().getAllUserItems()
    }
}