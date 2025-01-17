package com.github.nacabaro.vbhelper.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.nacabaro.vbhelper.daos.CharacterDao
import com.github.nacabaro.vbhelper.daos.DexDao
import com.github.nacabaro.vbhelper.daos.DiMDao
import com.github.nacabaro.vbhelper.daos.UserCharacterDao
import com.github.nacabaro.vbhelper.domain.characters.Character
import com.github.nacabaro.vbhelper.domain.characters.Card
import com.github.nacabaro.vbhelper.domain.Sprites
import com.github.nacabaro.vbhelper.domain.characters.Dex
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.TransformationHistory
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter

@Database(
    version = 1,
    entities = [
        Card::class,
        Character::class,
        Sprites::class,
        UserCharacter::class,
        BECharacterData::class,
        TransformationHistory::class,
        Dex::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dimDao(): DiMDao
    abstract fun characterDao(): CharacterDao
    abstract fun userCharacterDao(): UserCharacterDao
    abstract fun dexDao(): DexDao
}