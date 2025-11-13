package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import com.github.nacabaro.vbhelper.dtos.ItemDtos
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query(
        """
        SELECT *
        FROM Items
        ORDER BY Items.itemIcon ASC
    """
    )
    fun getAllItems(): Flow<List<ItemDtos.ItemsWithQuantities>>

    @Query(
        """
        SELECT *
        FROM Items
        WHERE quantity > 0
    """
    )
    fun getAllUserItems(): Flow<List<ItemDtos.ItemsWithQuantities>>

    @Query(
        """
        SELECT *
        FROM Items
        WHERE Items.id = :itemId
    """
    )
    suspend fun getItem(itemId: Long): ItemDtos.ItemsWithQuantities

    @Query(
        """
        UPDATE Items
        SET quantity = quantity - 1
        WHERE id = :itemId
    """
    )
    suspend fun useItem(itemId: Long)

    @Query(
        """
        UPDATE Items
        SET quantity = quantity + :itemAmount
        WHERE id = :itemId
    """
    )
    suspend fun purchaseItem(itemId: Long, itemAmount: Int)
}