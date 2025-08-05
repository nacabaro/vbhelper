package com.github.nacabaro.vbhelper.domain.items

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ItemType {
    VBITEM,
    BEITEM,
    UNIVERSAL,
    SPECIALMISSION
}

@Entity
data class Items(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String,
    val itemIcon: Int,
    val itemLength: Int,
    val price: Int,
    val quantity: Int,
    val itemType: ItemType
)
