package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query

@Dao
interface SpecialMissionDao {
    @Query("""
        UPDATE SpecialMissions SET 
            missionType = "NONE",
            status = "UNAVAILABLE"
        WHERE id = :id
    """)
    suspend fun clearSpecialMission(id: Long)
}