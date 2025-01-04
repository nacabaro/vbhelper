package com.github.nacabaro.vbhelper.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User (
    @PrimaryKey(autoGenerate = true) val id: Int,
    val username: String,
    val gender: Boolean
)