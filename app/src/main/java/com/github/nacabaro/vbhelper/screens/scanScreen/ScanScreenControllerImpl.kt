package com.github.nacabaro.vbhelper.screens.scanScreen

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcA
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.cfogrady.vbnfc.TagCommunicator
import com.github.cfogrady.vbnfc.be.BENfcCharacter
import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.cfogrady.vbnfc.vb.VBNfcCharacter
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.domain.device_data.BECharacterData
import com.github.nacabaro.vbhelper.domain.device_data.UserCharacter
import com.github.nacabaro.vbhelper.source.getCryptographicTransformerMap
import com.github.nacabaro.vbhelper.source.isMissingSecrets
import com.github.nacabaro.vbhelper.source.proto.Secrets
import com.github.nacabaro.vbhelper.utils.DeviceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.GregorianCalendar

class ScanScreenControllerImpl(
    override val secretsFlow: Flow<Secrets>,
    private val componentActivity: ComponentActivity,
    private val registerActivityLifecycleListener: (String, ActivityLifecycleListener)->Unit,
    private val unregisterActivityLifecycleListener: (String)->Unit,
): ScanScreenController {

    private val nfcAdapter: NfcAdapter

    init {
        val maybeNfcAdapter = NfcAdapter.getDefaultAdapter(componentActivity)
        if (maybeNfcAdapter == null) {
            Toast.makeText(componentActivity, "No NFC on device!", Toast.LENGTH_SHORT).show()
        }
        nfcAdapter = maybeNfcAdapter
        checkSecrets()
    }

    override fun onClickRead(secrets: Secrets, onComplete: ()->Unit) {
        handleTag(secrets) { tagCommunicator ->
            val character = tagCommunicator.receiveCharacter()
            val resultMessage = addCharacterScannedIntoDatabase(character)
            onComplete.invoke()
            resultMessage
        }
    }

    override fun cancelRead() {
        if(nfcAdapter.isEnabled) {
            nfcAdapter.disableReaderMode(componentActivity)
        }
    }

    override fun registerActivityLifecycleListener(
        key: String,
        activityLifecycleListener: ActivityLifecycleListener
    ) {
        registerActivityLifecycleListener.invoke(key, activityLifecycleListener)
    }

    override fun unregisterActivityLifecycleListener(key: String) {
        unregisterActivityLifecycleListener.invoke(key)
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun handleTag(secrets: Secrets, handlerFunc: (TagCommunicator)->String) {
        if (!nfcAdapter.isEnabled) {
            showWirelessSettings()
        } else {
            val options = Bundle()
            // Work around for some broken Nfc firmware implementations that poll the card too fast
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)
            nfcAdapter.enableReaderMode(componentActivity, buildOnReadTag(secrets, handlerFunc), NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                options
            )
        }
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun buildOnReadTag(secrets: Secrets, handlerFunc: (TagCommunicator)->String): (Tag)->Unit {
        return { tag->
            val nfcData = NfcA.get(tag)
            if (nfcData == null) {
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "Tag detected is not VB", Toast.LENGTH_SHORT).show()
                }
            }
            nfcData.connect()
            nfcData.use {
                val tagCommunicator = TagCommunicator.getInstance(nfcData, secrets.getCryptographicTransformerMap())
                val successText = handlerFunc(tagCommunicator)
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, successText, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkSecrets() {
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            if(secretsFlow.stateIn(componentActivity.lifecycleScope).value.isMissingSecrets()) {
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "Missing Secrets. Go to settings and import Vital Arena APK", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClickWrite(
        secrets: Secrets,
        nfcCharacter: NfcCharacter,
        onComplete: () -> Unit
    ) {
        handleTag(secrets) { tagCommunicator ->
            try {
                tagCommunicator.sendCharacter(nfcCharacter)
                onComplete.invoke()
                "Sent character successfully!"
            } catch (e: Throwable) {
                Log.e("TAG", e.stackTraceToString())
                "Whoops"
            }
        }
    }

    override fun onClickCheckCard(
        secrets: Secrets,
        nfcCharacter: NfcCharacter,
        onComplete: () -> Unit
    ) {
        handleTag(secrets) { tagCommunicator ->
            tagCommunicator.prepareDIMForCharacter(nfcCharacter.dimId)
            onComplete.invoke()
            "Sent DIM successfully!"
        }
    }

    // EXTRACTED DIRECTLY FROM EXAMPLE APP
    private fun showWirelessSettings() {
        Toast.makeText(componentActivity, "NFC must be enabled", Toast.LENGTH_SHORT).show()
        componentActivity.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS))
    }

    private fun addCharacterScannedIntoDatabase(nfcCharacter: NfcCharacter): String {
        val application = componentActivity.applicationContext as VBHelper
        val storageRepository = application.container.db

        val dimData = storageRepository
            .dimDao()
            .getDimById(nfcCharacter.dimId.toInt())

        if (dimData == null) return "Card not found"

        val cardCharData = storageRepository
            .characterDao()
            .getCharacterByMonIndex(nfcCharacter.charIndex.toInt(), dimData.id)

        val characterData = UserCharacter(
            charId = cardCharData.id,
            ageInDays = nfcCharacter.ageInDays.toInt(),
            nextAdventureMissionStage = nfcCharacter.nextAdventureMissionStage.toInt(),
            mood = nfcCharacter.mood.toInt(),
            vitalPoints = nfcCharacter.vitalPoints.toInt(),
            transformationCountdown = nfcCharacter.transformationCountdownInMinutes.toInt(),
            injuryStatus = nfcCharacter.injuryStatus,
            trophies = nfcCharacter.trophies.toInt(),
            currentPhaseBattlesWon = nfcCharacter.currentPhaseBattlesWon.toInt(),
            currentPhaseBattlesLost = nfcCharacter.currentPhaseBattlesLost.toInt(),
            totalBattlesWon = nfcCharacter.totalBattlesWon.toInt(),
            totalBattlesLost = nfcCharacter.totalBattlesLost.toInt(),
            activityLevel = nfcCharacter.activityLevel.toInt(),
            heartRateCurrent = nfcCharacter.heartRateCurrent.toInt(),
            characterType = when (nfcCharacter) {
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

        if (nfcCharacter is BENfcCharacter) {
            val extraCharacterData = BECharacterData(
                id = characterId,
                trainingHp = nfcCharacter.trainingHp.toInt(),
                trainingAp = nfcCharacter.trainingAp.toInt(),
                trainingBp = nfcCharacter.trainingBp.toInt(),
                remainingTrainingTimeInMinutes = nfcCharacter.remainingTrainingTimeInMinutes.toInt(),
                itemEffectActivityLevelValue = nfcCharacter.itemEffectActivityLevelValue.toInt(),
                itemEffectMentalStateValue = nfcCharacter.itemEffectMentalStateValue.toInt(),
                itemEffectMentalStateMinutesRemaining = nfcCharacter.itemEffectMentalStateMinutesRemaining.toInt(),
                itemEffectActivityLevelMinutesRemaining = nfcCharacter.itemEffectActivityLevelMinutesRemaining.toInt(),
                itemEffectVitalPointsChangeValue = nfcCharacter.itemEffectVitalPointsChangeValue.toInt(),
                itemEffectVitalPointsChangeMinutesRemaining = nfcCharacter.itemEffectVitalPointsChangeMinutesRemaining.toInt(),
                abilityRarity = nfcCharacter.abilityRarity,
                abilityType = nfcCharacter.abilityType.toInt(),
                abilityBranch = nfcCharacter.abilityBranch.toInt(),
                abilityReset = nfcCharacter.abilityReset.toInt(),
                rank = nfcCharacter.abilityReset.toInt(),
                itemType = nfcCharacter.itemType.toInt(),
                itemMultiplier = nfcCharacter.itemMultiplier.toInt(),
                itemRemainingTime = nfcCharacter.itemRemainingTime.toInt(),
                otp0 = "", //nfcCharacter.value!!.otp0.toString(),
                otp1 = "", //nfcCharacter.value!!.otp1.toString(),
                minorVersion = nfcCharacter.characterCreationFirmwareVersion.minorVersion.toInt(),
                majorVersion = nfcCharacter.characterCreationFirmwareVersion.majorVersion.toInt(),
            )

            storageRepository
                .userCharacterDao()
                .insertBECharacterData(extraCharacterData)

            val transformationHistoryWatch = nfcCharacter.transformationHistory
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
        } else if (nfcCharacter is VBNfcCharacter) {
            return "Not implemented yet"
        }

        return "Done reading character!"
    }
}