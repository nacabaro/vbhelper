package com.github.nacabaro.vbhelper

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.github.nacabaro.vbhelper.navigation.AppNavigation
import com.github.cfogrady.vbnfc.CryptographicTransformer
import com.github.cfogrady.vbnfc.R
import com.github.cfogrady.vbnfc.TagCommunicator
import com.github.cfogrady.vbnfc.be.BENfcCharacter
import com.github.cfogrady.vbnfc.data.DeviceType
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryBECharacterData
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryCharacterData
import com.github.nacabaro.vbhelper.temporary_domain.TemporaryTransformationHistory
import com.github.nacabaro.vbhelper.ui.theme.VBHelperTheme
import kotlinx.coroutines.flow.MutableStateFlow

class MainActivity : ComponentActivity() {
    private lateinit var nfcAdapter: NfcAdapter
    private lateinit var deviceToCryptographicTransformers: Map<UShort, CryptographicTransformer>

    private var nfcCharacter = MutableStateFlow<NfcCharacter?>(null)

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    override fun onCreate(savedInstanceState: Bundle?) {
        deviceToCryptographicTransformers = getMapOfCryptographicTransformers()

        val maybeNfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (maybeNfcAdapter == null) {
            Toast.makeText(this, "No NFC on device!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        nfcAdapter = maybeNfcAdapter

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VBHelperTheme {
                MainApplication()
            }
        }
    }

    @Composable
    private fun MainApplication() {
        var isDoneReadingCharacter by remember { mutableStateOf(false) }
        val application = LocalContext.current.applicationContext as VBHelper
        val storageRepository = application.container.db
        AppNavigation(
            isDoneReadingCharacter = isDoneReadingCharacter,
            onClickRead = {
                handleTag {
                    val character = it.receiveCharacter()
                    nfcCharacter.value = character
                    addCharacterScannedIntoDatabase()
                    isDoneReadingCharacter = true
                    "Done reading character"
                }
            },
            onClickScan = {
                isDoneReadingCharacter = false
            }
        )
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun getMapOfCryptographicTransformers(): Map<UShort, CryptographicTransformer> {
        return mapOf(
            Pair(DeviceType.VitalBraceletBEDeviceType,
                CryptographicTransformer(readableHmacKey1 = resources.getString(R.string.password1),
                    readableHmacKey2 = resources.getString(R.string.password2),
                    aesKey = resources.getString(R.string.decryptionKey),
                    substitutionCipher = resources.getIntArray(R.array.substitutionArray))),
//            Pair(DeviceType.VitalSeriesDeviceType,
//                CryptographicTransformer(hmacKey1 = resources.getString(R.string.password1),
//                    hmacKey2 = resources.getString(R.string.password2),
//                    decryptionKey = resources.getString(R.string.decryptionKey),
//                    substitutionCipher = resources.getIntArray(R.array.substitutionArray))),
//            Pair(DeviceType.VitalCharactersDeviceType,
//                CryptographicTransformer(hmacKey1 = resources.getString(R.string.password1),
//                    hmacKey2 = resources.getString(R.string.password2),
//                    decryptionKey = resources.getString(R.string.decryptionKey),
//                    substitutionCipher = resources.getIntArray(R.array.substitutionArray)))
        )
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun showWirelessSettings() {
        Toast.makeText(this, "NFC must be enabled", Toast.LENGTH_SHORT).show()
        startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun buildOnReadTag(handlerFunc: (TagCommunicator)->String): (Tag)->Unit {
        return { tag->
            val nfcData = NfcA.get(tag)
            if (nfcData == null) {
                runOnUiThread {
                    Toast.makeText(this, "Tag detected is not VB", Toast.LENGTH_SHORT).show()
                }
            }
            nfcData.connect()
            nfcData.use {
                val tagCommunicator = TagCommunicator.getInstance(nfcData, deviceToCryptographicTransformers)
                val successText = handlerFunc(tagCommunicator)
                runOnUiThread {
                    Toast.makeText(this, successText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun handleTag(handlerFunc: (TagCommunicator)->String) {
        if (!nfcAdapter.isEnabled) {
            showWirelessSettings()
        } else {
            val options = Bundle()
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            nfcAdapter.enableReaderMode(this, buildOnReadTag(handlerFunc), NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options
            )
        }
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    override fun onPause() {
        super.onPause()
        if (nfcAdapter.isEnabled) {
            nfcAdapter.disableReaderMode(this)
        }
    }

    //
    /*
    TODO:
    - Make it able to detect the different model of watches
    - Support for regular VB

    The good news is that the theory behind inserting to the database should be working
    now, it's a matter of implementing the functionality to parse dim/bem cards and use my
    domain model.
     */
    private fun addCharacterScannedIntoDatabase() {
        val beCharacter = nfcCharacter as MutableStateFlow<BENfcCharacter?>
        val temporaryCharacterData = TemporaryCharacterData(
            dimId = nfcCharacter.value!!.dimId.toInt(),
            charIndex = nfcCharacter.value!!.charIndex.toInt(),
            stage = nfcCharacter.value!!.stage.toInt(),
            attribute = nfcCharacter.value!!.attribute,
            ageInDays = nfcCharacter.value!!.ageInDays.toInt(),
            nextAdventureMissionStage = nfcCharacter.value!!.nextAdventureMissionStage.toInt(),
            mood = nfcCharacter.value!!.mood.toInt(),
            vitalPoints = nfcCharacter.value!!.vitalPoints.toInt(),
            transformationCountdown = nfcCharacter.value!!.transformationCountdown.toInt(),
            injuryStatus = nfcCharacter.value!!.injuryStatus,
            trophies = nfcCharacter.value!!.trophies.toInt(),
            currentPhaseBattlesWon = nfcCharacter.value!!.currentPhaseBattlesWon.toInt(),
            currentPhaseBattlesLost = nfcCharacter.value!!.currentPhaseBattlesLost.toInt(),
            totalBattlesWon = nfcCharacter.value!!.totalBattlesWon.toInt(),
            totalBattlesLost = nfcCharacter.value!!.totalBattlesLost.toInt(),
            activityLevel = nfcCharacter.value!!.activityLevel.toInt(),
            heartRateCurrent = nfcCharacter.value!!.heartRateCurrent.toInt()
        )

        val application = applicationContext as VBHelper
        val storageRepository = application.container.db
        val characterId: Long = storageRepository
            .temporaryMonsterDao()
            .insertCharacterData(temporaryCharacterData)

        val temporaryBECharacterData = TemporaryBECharacterData(
            id = characterId,
            trainingHp = beCharacter.value!!.trainingHp.toInt(),
            trainingAp = beCharacter.value!!.trainingAp.toInt(),
            trainingBp = beCharacter.value!!.trainingBp.toInt(),
            remainingTrainingTimeInMinutes = beCharacter.value!!.remainingTrainingTimeInMinutes.toInt(),
            itemEffectActivityLevelValue = beCharacter.value!!.itemEffectActivityLevelValue.toInt(),
            itemEffectMentalStateValue = beCharacter.value!!.itemEffectMentalStateValue.toInt(),
            itemEffectMentalStateMinutesRemaining = beCharacter.value!!.itemEffectMentalStateMinutesRemaining.toInt(),
            itemEffectActivityLevelMinutesRemaining = beCharacter.value!!.itemEffectActivityLevelMinutesRemaining.toInt(),
            itemEffectVitalPointsChangeValue = beCharacter.value!!.itemEffectVitalPointsChangeValue.toInt(),
            itemEffectVitalPointsChangeMinutesRemaining = beCharacter.value!!.itemEffectVitalPointsChangeMinutesRemaining.toInt(),
            abilityRarity = beCharacter.value!!.abilityRarity,
            abilityType = beCharacter.value!!.abilityType.toInt(),
            abilityBranch = beCharacter.value!!.abilityBranch.toInt(),
            abilityReset = beCharacter.value!!.abilityReset.toInt(),
            rank = beCharacter.value!!.abilityReset.toInt(),
            itemType = beCharacter.value!!.itemType.toInt(),
            itemMultiplier = beCharacter.value!!.itemMultiplier.toInt(),
            itemRemainingTime = beCharacter.value!!.itemRemainingTime.toInt(),
            otp0 = "", //beCharacter.value!!.otp0.toString(),
            otp1 = "", //beCharacter.value!!.otp1.toString(),
            minorVersion = beCharacter.value!!.characterCreationFirmwareVersion.minorVersion.toInt(),
            majorVersion = beCharacter.value!!.characterCreationFirmwareVersion.majorVersion.toInt(),
        )

        storageRepository
            .temporaryMonsterDao()
            .insertBECharacterData(temporaryBECharacterData)

        val transformationHistoryWatch = beCharacter.value!!.transformationHistory
        val domainTransformationHistory = transformationHistoryWatch.map { item ->
            TemporaryTransformationHistory(
                monId = characterId,
                toCharIndex = item.toCharIndex.toInt(),
                yearsSince1988 = item.yearsSince1988.toInt(),
                month = item.month.toInt(),
                day = item.day.toInt()
            )
        }

        storageRepository
            .temporaryMonsterDao()
            .insertTransformationHistory(*domainTransformationHistory.toTypedArray())
    }
}
