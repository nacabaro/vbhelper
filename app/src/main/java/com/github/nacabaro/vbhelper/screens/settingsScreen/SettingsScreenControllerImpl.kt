package com.github.nacabaro.vbhelper.screens.settingsScreen

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.DimCard
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.characters.Sprite
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.domain.card.CardProgress
import com.github.nacabaro.vbhelper.domain.card.CardCharacter
import com.github.nacabaro.vbhelper.source.ApkSecretsImporter
import com.github.nacabaro.vbhelper.source.SecretsImporter
import com.github.nacabaro.vbhelper.source.SecretsRepository
import com.github.nacabaro.vbhelper.source.proto.Secrets
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.InputStream
import java.io.OutputStream


class SettingsScreenControllerImpl(
    private val context: ComponentActivity,
): SettingsScreenController {
    private val roomDbName = "internalDb"
    private val filePickerLauncher: ActivityResultLauncher<String>
    private val filePickerOpenerLauncher: ActivityResultLauncher<Array<String>>
    private val filePickerApk: ActivityResultLauncher<Array<String>>
    private val filePickerCard: ActivityResultLauncher<Array<String>>
    private val secretsImporter: SecretsImporter = ApkSecretsImporter()
    private val application = context.applicationContext as VBHelper
    private val secretsRepository: SecretsRepository = application.container.dataStoreSecretsRepository
    private val database: AppDatabase = application.container.db

    init {
        filePickerLauncher = context.registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri ->
                if (uri != null) {
                    exportDatabase(uri)
                } else {
                    context.runOnUiThread {
                        Toast.makeText(context, "No destination selected", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        filePickerOpenerLauncher = context.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                importDatabase(uri)
            } else {
                context.runOnUiThread {
                    Toast.makeText(context, "No source selected", Toast.LENGTH_SHORT).show()
                }
            }
        }

        filePickerApk = context.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                importApk(uri)
            } else {
                context.runOnUiThread {
                    Toast.makeText(context, "APK import cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }

        filePickerCard = context.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                importCard(uri)
            } else {
                context.runOnUiThread {
                    Toast.makeText(context, "Card import cancelled", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClickOpenDirectory() {
        filePickerLauncher.launch("My application data.vbhelper")
    }

    override fun onClickImportDatabase() {
        filePickerOpenerLauncher.launch(arrayOf("application/octet-stream"))
    }

    override fun onClickImportApk() {
        filePickerApk.launch(arrayOf("*/*"))
    }

    override fun onClickImportCard() {
        filePickerCard.launch(arrayOf("*/*"))
    }

    private suspend fun importEvoData(
        cardId: Long,
        card: com.github.cfogrady.vb.dim.card.Card<*, *, *, *, *, *>
    ) {
        for (index in 0 until card.transformationRequirements.transformationEntries.size) {
            val evo = card.transformationRequirements.transformationEntries[index]

            var transformationTimerHours: Int
            var unlockAdventureLevel: Int

            if (card is BemCard) {
                transformationTimerHours = card
                    .transformationRequirements
                    .transformationEntries[index]
                    .minutesUntilTransformation / 60
                unlockAdventureLevel = if (
                    card
                        .transformationRequirements
                        .transformationEntries[index]
                        .requiredCompletedAdventureLevel == 65535
                ) {
                    0
                } else {
                    card
                        .transformationRequirements
                        .transformationEntries[index]
                        .requiredCompletedAdventureLevel
                }
            } else {
                transformationTimerHours = (card as DimCard)
                    .transformationRequirements
                    .transformationEntries[index]
                    .hoursUntilEvolution
                unlockAdventureLevel = if (
                    card
                        .adventureLevels
                        .levels
                        .last()
                        .bossCharacterIndex == card.transformationRequirements.transformationEntries[index].toCharacterIndex
                ) {
                    14
                    /*
                    Magic number incoming!!

                    In the case of DiMCards, stage 15 is the one that unlocks the locked character.
                    We know it is a locked character if the last adventure level's boss character index
                    is the current index. If it is, we add stage 15 complete as a requirement for transformation.
                     */
                } else {
                    0
                    /*
                    Another magic number...

                    The rest of the characters are not locked.
                     */
                }
            }

            database
                .characterDao()
                .insertPossibleTransformation(
                    cardId = cardId,
                    fromChraraIndex = evo.fromCharacterIndex,
                    toChraraIndex = evo.toCharacterIndex,
                    requiredVitals = evo.requiredVitalValues,
                    requiredTrophies = evo.requiredTrophies,
                    requiredBattles = evo.requiredBattles,
                    requiredWinRate = evo.requiredWinRatio,
                    requiredAdventureLevelCompleted = unlockAdventureLevel,
                    changeTimerHours = transformationTimerHours
                )
        }
    }

    private suspend fun importCharacterData(
        cardId: Long,
        card: com.github.cfogrady.vb.dim.card.Card<*, *, *, *, *, *>
    ) {
        var spriteCounter = when (card is BemCard) {
            true -> 54
            false -> 10
        }

        val domainCharacters = mutableListOf<CardCharacter>()

        val characters = card
            .characterStats
            .characterEntries

        for (index in 0 until characters.size) {
            var domainSprite: Sprite?
            if (index < 2 && card is DimCard) {
                domainSprite = Sprite(
                    width = card.spriteData.sprites[spriteCounter + 1].spriteDimensions.width,
                    height = card.spriteData.sprites[spriteCounter + 1].spriteDimensions.height,
                    spriteIdle1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteIdle2 = card.spriteData.sprites[spriteCounter + 2].pixelData,
                    spriteWalk1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteWalk2 = card.spriteData.sprites[spriteCounter + 3].pixelData,
                    spriteRun1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteRun2 = card.spriteData.sprites[spriteCounter + 3].pixelData,
                    spriteTrain1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteTrain2 = card.spriteData.sprites[spriteCounter + 3].pixelData,
                    spriteHappy = card.spriteData.sprites[spriteCounter + 4].pixelData,
                    spriteSleep = card.spriteData.sprites[spriteCounter + 5].pixelData,
                    spriteAttack = card.spriteData.sprites[spriteCounter + 2].pixelData,
                    spriteDodge = card.spriteData.sprites[spriteCounter + 3].pixelData
                )
            } else {
                domainSprite = Sprite(
                    width = card.spriteData.sprites[spriteCounter + 1].spriteDimensions.width,
                    height = card.spriteData.sprites[spriteCounter + 1].spriteDimensions.height,
                    spriteIdle1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                    spriteIdle2 = card.spriteData.sprites[spriteCounter + 2].pixelData,
                    spriteWalk1 = card.spriteData.sprites[spriteCounter + 3].pixelData,
                    spriteWalk2 = card.spriteData.sprites[spriteCounter + 4].pixelData,
                    spriteRun1 = card.spriteData.sprites[spriteCounter + 5].pixelData,
                    spriteRun2 = card.spriteData.sprites[spriteCounter + 6].pixelData,
                    spriteTrain1 = card.spriteData.sprites[spriteCounter + 7].pixelData,
                    spriteTrain2 = card.spriteData.sprites[spriteCounter + 8].pixelData,
                    spriteHappy = card.spriteData.sprites[spriteCounter + 9].pixelData,
                    spriteSleep = card.spriteData.sprites[spriteCounter + 10].pixelData,
                    spriteAttack = card.spriteData.sprites[spriteCounter + 11].pixelData,
                    spriteDodge = card.spriteData.sprites[spriteCounter + 12].pixelData
                )
            }

            val spriteId = database
                .spriteDao()
                .insertSprite(domainSprite)


            domainCharacters.add(
                CardCharacter(
                    cardId = cardId,
                    spriteId = spriteId,
                    charaIndex = index,
                    nameSprite = card.spriteData.sprites[spriteCounter].pixelData,
                    stage = characters[index].stage,
                    attribute = NfcCharacter.Attribute.entries[characters[index].attribute],
                    baseHp = characters[index].hp,
                    baseBp = characters[index].dp,
                    baseAp = characters[index].ap,
                    nameWidth = card.spriteData.sprites[spriteCounter].spriteDimensions.width,
                    nameHeight = card.spriteData.sprites[spriteCounter].spriteDimensions.height
                )
            )

            spriteCounter += if (card is BemCard) {
                14
            } else {
                when (index) {
                    0 -> 6
                    1 -> 7
                    else -> 14
                }
            }
        }

        database
            .characterDao()
            .insertCharacter(*domainCharacters.toTypedArray())
    }

    private suspend fun importAdventureMissions(
        cardId: Long,
        card: com.github.cfogrady.vb.dim.card.Card<*, *, *, *, *, *>
    ) {
        Log.d("importAdventureMissions", "Importing adventure missions")
        if (card is BemCard) {
            card.adventureLevels.levels.forEach {
                database
                    .cardAdventureDao()
                    .insertNewAdventure(
                        cardId = cardId,
                        characterId = it.bossCharacterIndex,
                        steps = it.steps,
                        bossAp = it.bossAp,
                        bossHp = it.bossHp,
                        bossDp = it.bossDp,
                        bossBp = it.bossBp
                    )
            }
        } else if (card is DimCard) {
            card.adventureLevels.levels.map {
                database
                    .cardAdventureDao()
                    .insertNewAdventure(
                        cardId = cardId,
                        characterId = it.bossCharacterIndex,
                        steps = it.steps,
                        bossAp = it.bossAp,
                        bossHp = it.bossHp,
                        bossDp = it.bossDp,
                        bossBp = null
                    )
            }
        }
    }

    private suspend fun importCardFusions(
        cardId: Long,
        card: com.github.cfogrady.vb.dim.card.Card<*, *, *, *, *, *>
    ) {
        Log.d("importCardFusions", "Importing card fusions")
        if (card is DimCard) {
            card
                .attributeFusions
                .entries
                .forEach {
                    database
                        .cardFusionsDao()
                        .insertNewFusion(
                            cardId = cardId,
                            fromCharaId = it.characterIndex,
                            toCharaIdAttr1 = it.attribute1Fusion,
                            toCharaIdAttr2 = it.attribute2Fusion,
                            toCharaIdAttr3 = it.attribute3Fusion,
                            toCharaIdAttr4 = it.attribute4Fusion
                        )
                }
        }
    }

    private fun updateCardProgress(
        cardId: Long,
    ) {
        database
            .cardProgressDao()
            .insertCardProgress(
                CardProgress(
                    cardId = cardId,
                    currentStage = 1,
                    unlocked = false
                )
            )
    }

    private fun importCard(uri: Uri) {
        context.lifecycleScope.launch(Dispatchers.IO) {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri)

            inputStream.use { fileReader ->
                val dimReader = DimReader()
                val card = dimReader.readCard(fileReader, false)

                val cardModel = Card(
                    cardId = card.header.dimId,
                    logo = card.spriteData.sprites[0].pixelData,
                    name = card.spriteData.text,
                    stageCount = card.adventureLevels.levels.size,
                    logoHeight = card.spriteData.sprites[0].height,
                    logoWidth = card.spriteData.sprites[0].width,
                    isBEm = card is BemCard
                )

                val cardId = database
                    .cardDao()
                    .insertNewCard(cardModel)

                updateCardProgress(cardId = cardId)

                importCharacterData(cardId, card)

                importEvoData(cardId, card)

                importAdventureMissions(cardId, card)

                importCardFusions(cardId, card)
            }

            inputStream?.close()
            context.runOnUiThread {
                Toast.makeText(context, "Import successful!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportDatabase(destinationUri: Uri) {
        context.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dbFile = File(context.getDatabasePath(roomDbName).absolutePath)
                if (!dbFile.exists()) {
                    throw IllegalStateException("Database file does not exist!")
                }

                application.container.db.close()

                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    dbFile.inputStream().use { inputStream ->
                        copyFile(inputStream, outputStream)
                    }
                } ?: throw IllegalArgumentException("Unable to open destination Uri for writing")

                context.runOnUiThread {
                    Toast.makeText(context, "Database exported successfully!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, "Closing application to avoid changes.", Toast.LENGTH_LONG).show()
                    context.finishAffinity()
                }
            } catch (e: Exception) {
                Log.e("ScanScreenController", "Error exporting database $e")
                context.runOnUiThread {
                    Toast.makeText(context, "Error exporting database: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun importDatabase(sourceUri: Uri) {
        context.lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!getFileNameFromUri(sourceUri)!!.endsWith(".vbhelper")) {
                    context.runOnUiThread {
                        Toast.makeText(context, "Invalid file format", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                application.container.db.close()

                val dbPath = context.getDatabasePath(roomDbName)
                val shmFile = File(dbPath.parent, "$roomDbName-shm")
                val walFile = File(dbPath.parent, "$roomDbName-wal")

                // Delete existing database files
                if (dbPath.exists()) dbPath.delete()
                if (shmFile.exists()) shmFile.delete()
                if (walFile.exists()) walFile.delete()

                val dbFile = File(dbPath.absolutePath)

                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    dbFile.outputStream().use { outputStream ->
                        copyFile(inputStream, outputStream)
                    }
                } ?: throw IllegalArgumentException("Unable to open source Uri for reading")

                context.runOnUiThread {
                    Toast.makeText(context, "Database imported successfully!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, "Reopen the app to finish import process!", Toast.LENGTH_LONG).show()
                    context.finishAffinity()
                }
            } catch (e: Exception) {
                Log.e("ScanScreenController", "Error importing database $e")
                context.runOnUiThread {
                    Toast.makeText(context, "Error importing database: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                fileName = it.getString(nameIndex)
            }
        }
        return fileName
    }

    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
    }

    private fun importApk(uri: Uri) {
        context.lifecycleScope.launch(Dispatchers.IO) {
            context.contentResolver.openInputStream(uri).use {
                if(it == null) {
                    context.runOnUiThread {
                        Toast.makeText(
                            context,
                            "Selected file is empty!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    return@launch
                }
                val secrets: Secrets?
                try {
                    secrets = secretsImporter.importSecrets(it)
                } catch (e: Exception) {
                    context.runOnUiThread {
                        Toast.makeText(context, "Secrets import failed. Please only select the official Vital Arena App 2.1.0 APK.", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }
                context.lifecycleScope.launch(Dispatchers.IO) {
                    secretsRepository.updateSecrets(secrets)
                }.invokeOnCompletion {
                    context.runOnUiThread {
                        Toast.makeText(context, "Secrets successfully imported. Connections with devices are now possible.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}