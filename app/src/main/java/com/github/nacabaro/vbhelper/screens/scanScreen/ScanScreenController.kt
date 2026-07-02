package com.github.nacabaro.vbhelper.screens.scanScreen

import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.domain.card.Card
import com.github.nacabaro.vbhelper.source.proto.Secrets
import kotlinx.coroutines.flow.Flow

interface ScanScreenController {
    val secretsFlow: Flow<Secrets>
    fun onClickRead(secrets: Secrets, onComplete: ()->Unit, onMultipleCards: (List<Card>) -> Unit)
    /**
     * First scan step. For physical bracelets this only prepares the DIM and the
     * character is written in a second step (onClickWrite). For the VitalWear watch
     * (HCE) the whole transfer happens here, so onComplete receives
     * transferComplete = true and no second step must run.
     */
    fun onClickCheckCard(secrets: Secrets, nfcCharacter: NfcCharacter, onComplete: (transferComplete: Boolean) -> Unit)
    fun onClickWrite(secrets: Secrets, nfcCharacter: NfcCharacter, onComplete: () -> Unit)

    fun cancelRead()

    fun registerActivityLifecycleListener(key: String, activityLifecycleListener: ActivityLifecycleListener)
    fun unregisterActivityLifecycleListener(key: String)

    fun flushCharacter(cardId: Long)

    fun characterFromNfc(
        nfcCharacter: NfcCharacter,
        onMultipleCards: (List<Card>, NfcCharacter) -> Unit
    ): String
    suspend fun characterToNfc(characterId: Long): NfcCharacter?
}