package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Upsert
import com.github.nacabaro.vbhelper.domain.DimProgress

@Dao
interface DiMProgressDao {
    @Upsert
    suspend fun updateDimProgress(vararg dimProgress: DimProgress)
}