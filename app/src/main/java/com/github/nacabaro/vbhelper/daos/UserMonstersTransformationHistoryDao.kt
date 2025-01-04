package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryTransformationHistory

@Dao
interface UserMonstersTransformationHistoryDao {
    @Insert
    fun insertTransformations(vararg transformations: TemporaryTransformationHistory)
}