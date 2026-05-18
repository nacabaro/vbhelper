package com.github.nacabaro.vbhelper.source

import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.domain.device_data.VBCharacterData
import com.github.nacabaro.vbhelper.utils.DeviceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Utility for converting character device types.
 * Used to fix mismatched device types when sending to physical NFC devices.
 */
class DeviceTypeConverter(private val database: AppDatabase) {

    /**
     * Prepares a BE character for VB-target exports without deleting BE stats.
     * This keeps the BE profile intact while ensuring a VB sidecar exists.
     */
    suspend fun convertBEToVB(characterId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            val userCharacter = database.userCharacterDao().getCharacter(characterId)

            if (userCharacter.characterType != DeviceType.BEDevice) {
                // Already VB or other type, no conversion needed
                return@withContext true
            }

            // Keep BE typing/stats as-is; only ensure a VB sidecar profile exists for exports.
            ensureVBCharacterDataExists(characterId)

            true
        } catch (e: Exception) {
            // Log but don't crash - character can still be sent as-is
            android.util.Log.e("DeviceTypeConverter", "Failed to convert character $characterId to VB type", e)
            false
        }
    }

    private suspend fun ensureVBCharacterDataExists(characterId: Long) {
        val vbData = database.userCharacterDao().getVbDataOrNull(characterId)
        if (vbData == null) {
            val vbCharacterData = VBCharacterData(
                id = characterId,
                generation = 0,
                totalTrophies = 0
            )
            database.userCharacterDao().insertVBCharacterData(vbCharacterData)
        }
    }
}




