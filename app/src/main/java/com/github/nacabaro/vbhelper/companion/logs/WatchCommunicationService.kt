package com.github.nacabaro.vbhelper.companion.logs

import com.github.nacabaro.vbhelper.companion.common.ChannelTypes
import com.github.nacabaro.vbhelper.di.VBHelper
import com.google.android.gms.wearable.ChannelClient
import com.google.android.gms.wearable.WearableListenerService
import timber.log.Timber

class WatchCommunicationService : WearableListenerService() {
    override fun onChannelOpened(channel: ChannelClient.Channel) {
        super.onChannelOpened(channel)
        when (channel.path) {
            ChannelTypes.LOGS_DATA -> {
                val logService = (application as VBHelper).companionLogService
                logService.receiveFile(this, channel)
            }
            else -> Timber.i("Unknown channel: ${channel.path}")
        }
    }
}

