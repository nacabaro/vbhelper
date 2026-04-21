package com.github.nacabaro.vbhelper.transfer

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.github.cfogrady.vitalwear.common.data.SharedDatabaseFactory

class SharedTransferSeenProvider : ContentProvider() {
    companion object {
        const val AUTHORITY = "com.github.nacabaro.vbhelper.transferseen"
        val URI: Uri = Uri.parse("content://$AUTHORITY")

        const val METHOD_MARK_SEEN = "markSeen"
        const val EXTRA_CARD_NAME = "cardName"
        const val EXTRA_SLOT_ID = "slotId"
        const val EXTRA_SEEN_AT_EPOCH_MILLIS = "seenAtEpochMillis"
        const val RESULT_OK = "ok"
    }

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        if (method != METHOD_MARK_SEEN) {
            return Bundle().apply { putBoolean(RESULT_OK, false) }
        }

        val context = context ?: return Bundle().apply { putBoolean(RESULT_OK, false) }
        val cardName = extras?.getString(EXTRA_CARD_NAME).orEmpty()
        val slotId = extras?.getInt(EXTRA_SLOT_ID, -1) ?: -1
        val seenAt = extras?.getLong(EXTRA_SEEN_AT_EPOCH_MILLIS, 0L) ?: 0L

        if (cardName.isBlank() || slotId < 0 || seenAt <= 0L) {
            return Bundle().apply { putBoolean(RESULT_OK, false) }
        }

        return runCatching {
            SharedDatabaseFactory.getDatabase(context).transferSeenDao().markSeen(cardName, slotId, seenAt)
            Bundle().apply { putBoolean(RESULT_OK, true) }
        }.getOrElse {
            Bundle().apply { putBoolean(RESULT_OK, false) }
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun insert(uri: Uri, values: ContentValues?): Uri? = null

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0
}

