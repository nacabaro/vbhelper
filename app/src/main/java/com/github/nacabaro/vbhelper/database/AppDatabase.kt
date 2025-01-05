package com.github.nacabaro.vbhelper.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.nacabaro.vbhelper.daos.CharacterDao
import com.github.nacabaro.vbhelper.daos.DiMDao
import com.github.nacabaro.vbhelper.domain.Character
import com.github.nacabaro.vbhelper.domain.Dim
import com.github.nacabaro.vbhelper.domain.Sprites
import com.github.nacabaro.vbhelper.temporary_daos.TemporaryMonsterDao
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryBECharacterData
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryCharacterData
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryTransformationHistory

@Database(
    version = 1,
    entities = [
        TemporaryCharacterData::class,
        TemporaryBECharacterData::class,
        TemporaryTransformationHistory::class,
        Dim::class,
        Character::class,
        Sprites::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun temporaryMonsterDao(): TemporaryMonsterDao
    abstract fun dimDao(): DiMDao
    abstract fun characterDao(): CharacterDao
}