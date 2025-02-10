package com.github.nacabaro.vbhelper.screens.scanScreen

import com.github.cfogrady.vbnfc.data.NfcCharacter
import com.github.nacabaro.vbhelper.ActivityLifecycleListener
import com.github.nacabaro.vbhelper.source.proto.Secrets
import kotlinx.coroutines.flow.Flow

interface ScanScreenController {
    val secretsFlow: Flow<Secrets>
    fun onClickRead(secrets: Secrets, onComplete: ()->Unit)
    fun onClickCheckCard(secrets: Secrets, nfcCharacter: NfcCharacter, onComplete: () -> Unit)
    fun onClickWrite(secrets: Secrets, nfcCharacter: NfcCharacter, onComplete: () -> Unit)

    fun cancelRead()

    fun registerActivityLifecycleListener(key: String, activityLifecycleListener: ActivityLifecycleListener)
    fun unregisterActivityLifecycleListener(key: String)

    suspend fun characterToNfc(characterId: Long): NfcCharacter?
}