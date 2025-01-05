package com.github.nacabaro.vbhelper.domain

import android.icu.text.ListFormatter.Width
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Dim(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val logo: ByteArray,
    val logoWidth: Int,
    val logoHeight: Int,
    val name: String,
    val stageCount: Int
)
