package com.github.nacabaro.vbhelper.dtos


object ItemDtos {
    data class ItemsWithQuantities (
        val id: Long,
        val name: String,
        val description: String,
        val itemIcon: Int,
        val itemLength: Int,
        val price: Int,
        val quantity: Int,
    )
}