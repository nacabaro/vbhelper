package com.github.cfogrady.vitalwear.common.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface SharedTransferSeenDao {
	@Insert(onConflict = OnConflictStrategy.IGNORE)
	fun insert(entry: SharedTransferSeenEntity): Long

	@Query(
		"""
		UPDATE ${SharedTransferSeenEntity.TABLE}
		SET cardName = :cardName,
			seenAtEpochMillis = :seenAtEpochMillis
		WHERE cardLookupKey = :cardLookupKey
		  AND slotId = :slotId
		"""
	)
	fun update(cardName: String, cardLookupKey: String, slotId: Int, seenAtEpochMillis: Long)

	@Transaction
	fun markSeen(cardName: String, slotId: Int, seenAtEpochMillis: Long) {
		if (slotId < 0) {
			return
		}
		val cardLookupKey = cardName.lowercase().filter { it.isLetterOrDigit() }
		if (cardLookupKey.isBlank()) {
			return
		}
		val inserted = insert(
			SharedTransferSeenEntity(
				cardName = cardName,
				cardLookupKey = cardLookupKey,
				slotId = slotId,
				seenAtEpochMillis = seenAtEpochMillis,
			)
		)
		if (inserted == -1L) {
			update(cardName, cardLookupKey, slotId, seenAtEpochMillis)
		}
	}
}

