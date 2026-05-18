package com.github.nacabaro.vbhelper.daos

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.github.nacabaro.vbhelper.domain.device_data.CharacterTransferPolicy

@Dao
interface CharacterTransferPolicyDao {
    @Query("SELECT * FROM CharacterTransferPolicy WHERE characterId = :characterId")
    fun getByCharacterId(characterId: Long): CharacterTransferPolicy?

    @Upsert
    fun upsert(policy: CharacterTransferPolicy)
}


