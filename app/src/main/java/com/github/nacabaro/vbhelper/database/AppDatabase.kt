package com.github.nacabaro.vbhelper.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.nacabaro.vbhelper.domain.Dim
import com.github.nacabaro.vbhelper.domain.DimProgress
import com.github.nacabaro.vbhelper.domain.Evolutions
import com.github.nacabaro.vbhelper.domain.Mon
import com.github.nacabaro.vbhelper.domain.User
import com.github.nacabaro.vbhelper.domain.UserHealthData
import com.github.nacabaro.vbhelper.domain.UserMonsters
import com.github.nacabaro.vbhelper.domain.UserMonstersSpecialMissions
import com.github.nacabaro.vbhelper.domain.UserStepsData
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryBECharacterData
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryCharacterData
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryTransformationHistory

@Database(
    version = 1,
    entities = [
        TemporaryCharacterData::class,
        TemporaryBECharacterData::class,
        TemporaryTransformationHistory::class
    ]
)
abstract class AppDatabase : RoomDatabase() {

}