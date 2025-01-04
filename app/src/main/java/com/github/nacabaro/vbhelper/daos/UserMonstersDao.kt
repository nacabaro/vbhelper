package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.nacabaro.vbhelper.domain.UserMonsters

@Dao
interface UserMonstersDao {
    @Insert
    fun insertUserMonsters(userMonsters: UserMonsters)

    @Query("SELECT * FROM UserMonsters WHERE userId = :userId")
    fun getUserMonsters(userId: Int): List<UserMonsters>
}