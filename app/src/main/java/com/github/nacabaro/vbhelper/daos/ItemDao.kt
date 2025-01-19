package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import com.github.nacabaro.vbhelper.dtos.ItemDtos

@Dao
interface ItemDao {
    @Query("""
        SELECT Items.*, UserItems.quantity
        FROM Items
        LEFT JOIN UserItems ON Items.id = UserItems.itemId
        ORDER BY Items.itemIcon ASC
    """)
    suspend fun getAllItems(): List<ItemDtos.ItemsWithQuantities>

    @Query("""
        SELECT Items.*, UserItems.quantity
        FROM Items
        JOIN UserItems ON Items.id = UserItems.itemId
    """)
    suspend fun getAllUserItems(): List<ItemDtos.ItemsWithQuantities>

    @Query("""
        UPDATE UserItems
        SET quantity = quantity - 1
        WHERE itemId = :itemId
    """)
    fun useItem(itemId: Long)

    @Query("""
        UPDATE UserItems
        SET quantity = quantity - :itemAmount
        WHERE itemId = :itemId
    """)
    suspend fun purchaseItem(itemId: Long, itemAmount: Int)
}