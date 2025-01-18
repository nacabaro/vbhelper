package com.github.nacabaro.vbhelper.domain.items

import android.content.Intent
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Items(
    @PrimaryKey val id: Long,
    val name: String,
    val description: String,
    val itemIcon: Int,
    val lengthIcon: Int,
    val price: Int
)
