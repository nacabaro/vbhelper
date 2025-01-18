package com.github.nacabaro.vbhelper.dtos

import androidx.room.PrimaryKey

object ItemDtos {
    data class ItemsWithQuantities (
        val id: Long,
        val name: String,
        val description: String,
        val itemIcon: Int,
        val lengthIcon: Int,
        val price: Int,
        val quantity: Int,
    )
}