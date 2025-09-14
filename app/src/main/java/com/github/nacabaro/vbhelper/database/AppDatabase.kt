package com.github.nacabaro.vbhelper.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.nacabaro.vbhelper.daos.AdventureDao
import com.github.nacabaro.vbhelper.daos.CardAdventureDao
import com.github.nacabaro.vbhelper.daos.CardBackgroundDao
import com.github.nacabaro.vbhelper.daos.CharacterDao
import com.github.nacabaro.vbhelper.daos.DexDao
import com.github.nacabaro.vbhelper.daos.CardDao
import com.github.nacabaro.vbhelper.daos.CardFusionsDao
import com.github.nacabaro.vbhelper.daos.CardProgressDao
import com.github.nacabaro.vbhelper.daos.ItemDao
import com.github.nacabaro.vbhelper.daos.SpecialMissionDao
import com.github.nacabaro.vbhelper.daos.SpriteDao
import com.github.nacabaro.vbhelper.daos.UserCharacterDao
import com.github.nacabaro.vbhelper.domain.card.CardBackground
import com.github.nacabaro.vbhelper.domain.card.CardCharacter
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.domain.card.CardAdventure
import com.github.nacabaro.vbhelper.domain.card.CardFusions
import com.github.nacabaro.vbhelper.domain.card.CardProgress
import com.github.nacabaro.vbhelper.domain.card.CardSprites
import com.github.nacabaro.vbhelper.domain.card.CardTransformations
import com.github.nacabaro.vbhelper.domain.characters.Sprite
import com.github.nacabaro.vbhelper.domain.characters.Adventure
import com.github.nacabaro.vbhelper.domain.characters.Dex
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.SpecialMissions
import com.github.nacabaro.vbhelper.domain.device_data.TransformationHistory
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.domain.device_data.VitalsHistory
import com.github.nacabaro.vbhelper.domain.items.Items

@Database(
    version = 1,
    entities = [
        Card::class,
        CardProgress::class,
        CardCharacter::class,
        CardAdventure::class,
        CardFusions::class,
        CardSprites::class,
        Sprite::class,
        UserCharacter::class,
        BECharacterData::class,
        VBCharacterData::class,
        SpecialMissions::class,
        TransformationHistory::class,
        VitalsHistory::class,
        Dex::class,
        Items::class,
        Adventure::class,
        CardBackground::class,
        CardTransformations::class
    ]
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cardDao(): CardDao
    abstract fun cardProgressDao(): CardProgressDao
    abstract fun characterDao(): CharacterDao
    abstract fun userCharacterDao(): UserCharacterDao
    abstract fun dexDao(): DexDao
    abstract fun itemDao(): ItemDao
    abstract fun adventureDao(): AdventureDao
    abstract fun spriteDao(): SpriteDao
    abstract fun specialMissionDao(): SpecialMissionDao
    abstract fun cardAdventureDao(): CardAdventureDao
    abstract fun cardFusionsDao(): CardFusionsDao
    abstract fun cardBackgroundDao(): CardBackgroundDao
}