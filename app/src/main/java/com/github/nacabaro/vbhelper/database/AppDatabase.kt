package com.github.nacabaro.vbhelper.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.github.nacabaro.vbhelper.daos.AdventureDao
import com.github.nacabaro.vbhelper.daos.CardAdventureDao
import com.github.nacabaro.vbhelper.daos.CharacterDao
import com.github.nacabaro.vbhelper.daos.DexDao
import com.github.nacabaro.vbhelper.daos.CardDao
import com.github.nacabaro.vbhelper.daos.CardFusionsDao
import com.github.nacabaro.vbhelper.daos.CardProgressDao
import com.github.nacabaro.vbhelper.daos.ItemDao
import com.github.nacabaro.vbhelper.daos.SpecialMissionDao
import com.github.nacabaro.vbhelper.daos.SpriteDao
import com.github.nacabaro.vbhelper.daos.UserCharacterDao
import com.github.nacabaro.vbhelper.daos.VitalWearSettingsDao
import com.github.nacabaro.vbhelper.domain.card.Background
import com.github.nacabaro.vbhelper.domain.card.CardCharacter
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.domain.card.CardAdventure
import com.github.nacabaro.vbhelper.domain.card.CardFusions
import com.github.nacabaro.vbhelper.domain.card.CardProgress
import com.github.nacabaro.vbhelper.domain.card.PossibleTransformations
import com.github.nacabaro.vbhelper.domain.characters.Sprite
import com.github.nacabaro.vbhelper.domain.characters.Adventure
import com.github.nacabaro.vbhelper.domain.characters.Dex
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.SpecialMissions
import com.github.nacabaro.vbhelper.domain.device_data.TransformationHistory
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.domain.device_data.VitalsHistory
import com.github.nacabaro.vbhelper.domain.device_data.VitalWearCharacterSettings
import com.github.nacabaro.vbhelper.domain.items.Items

@Database(
    version = 5,
    entities = [
        Card::class,
        CardProgress::class,
        CardCharacter::class,
        CardAdventure::class,
        CardFusions::class,
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
        Background::class,
        PossibleTransformations::class,
        VitalWearCharacterSettings::class,
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
    abstract fun vitalWearSettingsDao(): VitalWearSettingsDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `VitalWearCharacterSettings` (
                        `characterId` INTEGER NOT NULL,
                        `trainingInBackground` INTEGER NOT NULL,
                        `allowedBattles` INTEGER NOT NULL,
                        `accumulatedDailyInjuries` INTEGER NOT NULL,
                        PRIMARY KEY(`characterId`)
                    )
                    """.trimIndent()
                )

                // Clean existing duplicates before adding the unique index.
                db.execSQL(
                    """
                    DELETE FROM `CardCharacter`
                    WHERE `id` NOT IN (
                        SELECT MIN(`id`) FROM `CardCharacter` GROUP BY `cardId`, `charaIndex`
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE UNIQUE INDEX IF NOT EXISTS `index_CardCharacter_cardId_charaIndex`
                    ON `CardCharacter` (`cardId`, `charaIndex`)
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                val backfillTimestamp = System.currentTimeMillis()
                db.execSQL(
                    """
                    INSERT INTO `TransformationHistory`(`monId`, `stageId`, `transformationDate`)
                    SELECT uc.`id`, uc.`charId`, $backfillTimestamp
                    FROM `UserCharacter` uc
                    WHERE NOT EXISTS (
                        SELECT 1
                        FROM `TransformationHistory` th
                        WHERE th.`monId` = uc.`id`
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_3_5 = object : Migration(3, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // TransferSeen moved to shared_transfer.db
                db.execSQL("DROP TABLE IF EXISTS `TransferSeen`")
                db.execSQL("DROP INDEX IF EXISTS `index_TransferSeen_cardLookupKey_slotId`")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // TransferSeen moved to shared_transfer.db
                db.execSQL("DROP TABLE IF EXISTS `TransferSeen`")
                db.execSQL("DROP INDEX IF EXISTS `index_TransferSeen_cardLookupKey_slotId`")
            }
        }

    }
}