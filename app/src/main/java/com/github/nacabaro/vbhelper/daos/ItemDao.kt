package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.items.Items
import com.github.nacabaro.vbhelper.dtos.ItemDtos

@Dao
interface ItemDao {
    @Query("""
        SELECT Items.*, UserItems.quantity
        FROM Items
        LEFT JOIN UserItems ON Items.id = UserItems.itemId
    """)
    suspend fun getAllItems(): List<ItemDtos.ItemsWithQuantities>

    @Query("""
        SELECT Items.*, UserItems.quantity
        FROM Items
        JOIN UserItems ON Items.id = UserItems.itemId
    """)
    suspend fun getAllUserItems(): List<ItemDtos.ItemsWithQuantities>
}