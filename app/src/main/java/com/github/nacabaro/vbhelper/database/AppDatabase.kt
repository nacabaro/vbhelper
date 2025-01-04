package com.github.nacabaro.vbhelper.database

import androidx.room.Database
import com.github.nacabaro.vbhelper.domain.Dim
import com.github.nacabaro.vbhelper.domain.DimProgress
import com.github.nacabaro.vbhelper.domain.Evolutions
import com.github.nacabaro.vbhelper.domain.Mon
import com.github.nacabaro.vbhelper.domain.User
import com.github.nacabaro.vbhelper.domain.UserHealthData
import com.github.nacabaro.vbhelper.domain.UserMonsters
import com.github.nacabaro.vbhelper.domain.UserMonstersSpecialMissions
import com.github.nacabaro.vbhelper.domain.UserStepsData

@Database(
    version = 1,
    entities = [
        Dim::class,
        DimProgress::class,
        Evolutions::class,
        Mon::class,
        User::class,
        UserHealthData::class,
        UserMonsters::class,
        UserMonstersSpecialMissions::class,
        UserStepsData::class
    ]
)
abstract class AppDatabase {

}