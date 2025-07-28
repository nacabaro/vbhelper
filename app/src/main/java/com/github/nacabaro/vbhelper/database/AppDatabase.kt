package com.github.nacabaro.vbhelper.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.nacabaro.vbhelper.daos.AdventureDao
import com.github.nacabaro.vbhelper.daos.CharacterDao
import com.github.nacabaro.vbhelper.daos.DexDao
import com.github.nacabaro.vbhelper.daos.DiMDao
import com.github.nacabaro.vbhelper.daos.ItemDao
import com.github.nacabaro.vbhelper.daos.SpriteDao
import com.github.nacabaro.vbhelper.daos.UserCharacterDao
import com.github.nacabaro.vbhelper.domain.characters.Character
import com.github.nacabaro.vbhelper.domain.characters.Card
import com.github.nacabaro.vbhelper.domain.characters.Sprite
import com.github.nacabaro.vbhelper.domain.characters.Adventure
import com.github.nacabaro.vbhelper.domain.characters.Dex
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.SpecialMissions
import com.github.nacabaro.vbhelper.domain.device_data.TransformationHistory
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.domain.items.Items

@Database(
    version = 1,
    entities = [
        Card::class,
        Character::class,
        Sprite::class,
        UserCharacter::class,
        BECharacterData::class,
        VBCharacterData::class,
        SpecialMissions::class,
        TransformationHistory::class,
        Dex::class,
        Items::class,
        Adventure::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dimDao(): DiMDao
    abstract fun characterDao(): CharacterDao
    abstract fun userCharacterDao(): UserCharacterDao
    abstract fun dexDao(): DexDao
    abstract fun itemDao(): ItemDao
    abstract fun adventureDao(): AdventureDao
    abstract fun spriteDao(): SpriteDao
}