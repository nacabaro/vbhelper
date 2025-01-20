package com.github.nacabaro.vbhelper

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vb.dim.card.BemCard
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.nacabaro.vbhelper.navigation.AppNavigation
import com.github.cfogrady.vbnfc.be.BENfcCharacter
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vbnfc.vb.VBNfcCharacter
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.characters.Card
import com.github.nacabaro.vbhelper.domain.Sprites
import com.github.nacabaro.vbhelper.domain.characters.Character
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.navigation.AppNavigationHandlers
import com.github.nacabaro.vbhelper.screens.itemsScreen.ItemsScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.scanScreen.ScanScreenControllerImpl
import com.github.nacabaro.vbhelper.screens.settingsScreen.SettingsScreenControllerImpl
import com.github.nacabaro.vbhelper.ui.theme.VBHelperTheme
import com.github.nacabaro.vbhelper.utils.DeviceType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import java.util.GregorianCalendar

class MainActivity : ComponentActivity() {

    private var nfcCharacter = MutableStateFlow<NfcCharacter?>(null)

    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>

    private val onActivityLifecycleListeners = HashMap<String, ActivityLifecycleListener>()

    private fun registerActivityLifecycleListener(key: String, activityLifecycleListener: ActivityLifecycleListener) {
        if( onActivityLifecycleListeners[key] != null) {
            throw IllegalStateException("Key is already in use")
        }
        onActivityLifecycleListeners[key] = activityLifecycleListener
    }

    private fun unregisterActivityLifecycleListener(key: String) {
        onActivityLifecycleListeners.remove(key)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        registerFileActivityResult()

        val application = applicationContext as VBHelper
        val scanScreenController = ScanScreenControllerImpl(
            application.container.dataStoreSecretsRepository.secretsFlow,
            this::handleReceivedNfcCharacter,
            this,
            this::registerActivityLifecycleListener,
            this::unregisterActivityLifecycleListener
        )
        val settingsScreenController = SettingsScreenControllerImpl(this)
        val itemsScreenController = ItemsScreenControllerImpl(this)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VBHelperTheme {
                MainApplication(
                    scanScreenController = scanScreenController,
                    settingsScreenController = settingsScreenController,
                    itemsScreenController = itemsScreenController
                )
            }
        }
        Log.i("MainActivity", "Activity onCreated")
    }

    override fun onPause() {
        super.onPause()
        Log.i("MainActivity", "onPause")
        for(activityListener in onActivityLifecycleListeners) {
            activityListener.value.onPause()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.i("MainActivity", "Resume")
        for(activityListener in onActivityLifecycleListeners) {
            activityListener.value.onResume()
        }
    }

    private fun registerFileActivityResult() {
        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            lifecycleScope.launch {
                val application = applicationContext as VBHelper
                val storageRepository = application.container.db

                if (it.resultCode != RESULT_OK) {
                    Toast.makeText(applicationContext, "Import operation cancelled.", Toast.LENGTH_SHORT).show()
                }
                val contentResolver = applicationContext.contentResolver
                val inputStream = contentResolver.openInputStream(it.data!!.data!!)
                inputStream.use { fileReader ->
                    val dimReader = DimReader()
                    val card = dimReader.readCard(fileReader, false)

                    Log.i("MainActivity", "Card name: ${card is BemCard}")

                    val cardModel = Card(
                        dimId = card.header.dimId,
                        logo = card.spriteData.sprites[0].pixelData,
                        name = card.spriteData.text, // TODO Make user write card name
                        stageCount = card.adventureLevels.levels.size,
                        logoHeight = card.spriteData.sprites[0].height,
                        logoWidth = card.spriteData.sprites[0].width,
                        isBEm = card is BemCard
                    )

                    val dimId = storageRepository
                        .dimDao()
                        .insertNewDim(cardModel)

                    val characters = card.characterStats.characterEntries

                    var spriteCounter = when (card is BemCard) {
                        true -> 55
                        false -> 10
                    }

                    val domainCharacters = mutableListOf<Character>()

                    for (index in 0 until characters.size) {
                        domainCharacters.add(
                            Character(
                                dimId = dimId,
                                monIndex = index,
                                name = card.spriteData.sprites[spriteCounter].pixelData,
                                stage = characters[index].stage,
                                attribute = characters[index].attribute,
                                baseHp = characters[index].hp,
                                baseBp = characters[index].dp,
                                baseAp = characters[index].ap,
                                sprite1 = card.spriteData.sprites[spriteCounter + 1].pixelData,
                                sprite2 = card.spriteData.sprites[spriteCounter + 2].pixelData,
                                nameWidth = card.spriteData.sprites[spriteCounter].width,
                                nameHeight = card.spriteData.sprites[spriteCounter].height,
                                spritesWidth = card.spriteData.sprites[spriteCounter + 1].width,
                                spritesHeight = card.spriteData.sprites[spriteCounter + 1].height
                            )
                        )

                        // TODO: Improve this
                        if (card is BemCard) {
                            spriteCounter += 14
                        } else {
                            when (index) {
                                0 -> spriteCounter += 6
                                1 -> spriteCounter += 7
                                else -> spriteCounter += 14
                            }
                        }
                    }

                    storageRepository
                        .characterDao()
                        .insertCharacter(*domainCharacters.toTypedArray())

                    val sprites = card.spriteData.sprites.map { sprite ->
                        Sprites(
                            id = 0,
                            sprite = sprite.pixelData,
                            width = sprite.width,
                            height = sprite.height
                        )
                    }
                    storageRepository
                        .characterDao()
                        .insertSprite(*sprites.toTypedArray())
                }
                inputStream?.close()
                Toast.makeText(applicationContext, "Import successful!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @Composable
    private fun MainApplication(
        scanScreenController: ScanScreenControllerImpl,
        settingsScreenController: SettingsScreenControllerImpl,
        itemsScreenController: ItemsScreenControllerImpl
    ) {

        AppNavigation(
            applicationNavigationHandlers = AppNavigationHandlers(
                settingsScreenController,
                scanScreenController,
                itemsScreenController
            ),
            onClickImportCard = {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                }
                activityResultLauncher.launch(intent)
            }
        )
    }

    private fun handleReceivedNfcCharacter(character: NfcCharacter): String {
        nfcCharacter.value = character

        val importStatus = addCharacterScannedIntoDatabase()

        return importStatus
    }

    //
    /*
    TODO:
    - Support for regular VB

    The good news is that the theory behind inserting to the database should be working
    now, it's a matter of implementing the functionality to parse dim/bem cards and use my
    domain model.
     */
    private fun addCharacterScannedIntoDatabase(): String {
        val application = applicationContext as VBHelper
        val storageRepository = application.container.db

        val dimData = storageRepository
            .dimDao()
            .getDimById(nfcCharacter.value!!.dimId.toInt())

        if (dimData == null) return "Card not found"

        val cardCharData = storageRepository
            .characterDao()
            .getCharacterByMonIndex(nfcCharacter.value!!.charIndex.toInt(), dimData.id)

        val characterData = UserCharacter(
            charId = cardCharData.id,
            stage = nfcCharacter.value!!.stage.toInt(),
            attribute = nfcCharacter.value!!.attribute,
            ageInDays = nfcCharacter.value!!.ageInDays.toInt(),
            nextAdventureMissionStage = nfcCharacter.value!!.nextAdventureMissionStage.toInt(),
            mood = nfcCharacter.value!!.mood.toInt(),
            vitalPoints = nfcCharacter.value!!.vitalPoints.toInt(),
            transformationCountdown = nfcCharacter.value!!.transformationCountdownInMinutes.toInt(),
            injuryStatus = nfcCharacter.value!!.injuryStatus,
            trophies = nfcCharacter.value!!.trophies.toInt(),
            currentPhaseBattlesWon = nfcCharacter.value!!.currentPhaseBattlesWon.toInt(),
            currentPhaseBattlesLost = nfcCharacter.value!!.currentPhaseBattlesLost.toInt(),
            totalBattlesWon = nfcCharacter.value!!.totalBattlesWon.toInt(),
            totalBattlesLost = nfcCharacter.value!!.totalBattlesLost.toInt(),
            activityLevel = nfcCharacter.value!!.activityLevel.toInt(),
            heartRateCurrent = nfcCharacter.value!!.heartRateCurrent.toInt(),
            characterType = when (nfcCharacter.value) {
                is BENfcCharacter -> DeviceType.BEDevice
                else -> DeviceType.VBDevice
            },
            isActive = true
        )

        storageRepository
            .userCharacterDao()
            .clearActiveCharacter()

        val characterId: Long = storageRepository
            .userCharacterDao()
            .insertCharacterData(characterData)

        if (nfcCharacter.value is BENfcCharacter) {
            val beCharacter = nfcCharacter.value as BENfcCharacter
            val extraCharacterData = BECharacterData(
                id = characterId,
                trainingHp = beCharacter.trainingHp.toInt(),
                trainingAp = beCharacter.trainingAp.toInt(),
                trainingBp = beCharacter.trainingBp.toInt(),
                remainingTrainingTimeInMinutes = beCharacter.remainingTrainingTimeInMinutes.toInt(),
                itemEffectActivityLevelValue = beCharacter.itemEffectActivityLevelValue.toInt(),
                itemEffectMentalStateValue = beCharacter.itemEffectMentalStateValue.toInt(),
                itemEffectMentalStateMinutesRemaining = beCharacter.itemEffectMentalStateMinutesRemaining.toInt(),
                itemEffectActivityLevelMinutesRemaining = beCharacter.itemEffectActivityLevelMinutesRemaining.toInt(),
                itemEffectVitalPointsChangeValue = beCharacter.itemEffectVitalPointsChangeValue.toInt(),
                itemEffectVitalPointsChangeMinutesRemaining = beCharacter.itemEffectVitalPointsChangeMinutesRemaining.toInt(),
                abilityRarity = beCharacter.abilityRarity,
                abilityType = beCharacter.abilityType.toInt(),
                abilityBranch = beCharacter.abilityBranch.toInt(),
                abilityReset = beCharacter.abilityReset.toInt(),
                rank = beCharacter.abilityReset.toInt(),
                itemType = beCharacter.itemType.toInt(),
                itemMultiplier = beCharacter.itemMultiplier.toInt(),
                itemRemainingTime = beCharacter.itemRemainingTime.toInt(),
                otp0 = "", //beCharacter.value!!.otp0.toString(),
                otp1 = "", //beCharacter.value!!.otp1.toString(),
                minorVersion = beCharacter.characterCreationFirmwareVersion.minorVersion.toInt(),
                majorVersion = beCharacter.characterCreationFirmwareVersion.majorVersion.toInt(),
            )

            storageRepository
                .userCharacterDao()
                .insertBECharacterData(extraCharacterData)

            val transformationHistoryWatch = beCharacter.transformationHistory
            transformationHistoryWatch.map { item ->
                if (item.toCharIndex.toInt() != 255) {
                    val date = GregorianCalendar(item.year.toInt(), item.month.toInt(), item.day.toInt())
                        .time
                        .time

                    storageRepository
                        .characterDao()
                        .insertTransformation(characterId, item.toCharIndex.toInt(), dimData.id, date)

                    storageRepository
                        .dexDao()
                        .insertCharacter(item.toCharIndex.toInt(), dimData.id, date)
                }
            }
        } else if (nfcCharacter.value is VBNfcCharacter) {
            return "Not implemented yet"
        }

        return "Done reading character!"
    }
}
