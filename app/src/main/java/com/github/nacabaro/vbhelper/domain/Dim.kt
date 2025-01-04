package com.github.nacabaro.vbhelper.domain

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Dim(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val name: String,
    val stageCount: Int
)
