package com.github.nacabaro.vbhelper.transfer

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import com.github.cfogrady.vb.dim.card.DimReader
import com.github.nacabaro.vbhelper.di.VBHelper
import com.github.nacabaro.vbhelper.screens.settingsScreen.controllers.CardImportController
import kotlinx.coroutines.runBlocking

/**
 * ContentProvider that lets the VitalWear Companion app read and write card data into
 * VBHelper's single database (internalDb). Both apps are signed with the same key, so
 * the signature-level permission is automatically granted without user interaction.
 *
 * Usage from the companion:
 *   val names = call(METHOD_GET_CARD_NAMES)?.getStringArray(EXTRA_CARD_NAMES_LIST)
 *   call(METHOD_IMPORT_CARD, null, Bundle { putByteArray(EXTRA_CARD_BYTES, ...) })
 */
class CardImportProvider : ContentProvider() {

    companion object {
        const val AUTHORITY = "com.github.nacabaro.vbhelper.cardimport"
        val URI: Uri = Uri.parse("content://$AUTHORITY")

        /** Returns a Bundle with EXTRA_CARD_NAMES_LIST containing all imported card names. */
        const val METHOD_GET_CARD_NAMES = "getCardNames"

        /**
         * Parses and stores a DIM card.
         * Required extras: EXTRA_CARD_BYTES (ByteArray)
         * Optional extras: EXTRA_CARD_NAME  (String) — user-visible label; if omitted the DIM
         *                  file's built-in name is used.
         * Returns a Bundle with RESULT_OK = true on success.
         */
        const val METHOD_IMPORT_CARD = "importCard"

        const val EXTRA_CARD_BYTES      = "cardBytes"
        const val EXTRA_CARD_NAME       = "cardName"
        const val EXTRA_CARD_NAMES_LIST = "cardNames"
        const val RESULT_OK             = "ok"
    }

    override fun onCreate(): Boolean = true

    override fun call(method: String, arg: String?, extras: Bundle?): Bundle {
        val ctx = context ?: return Bundle().apply { putBoolean(RESULT_OK, false) }
        val db  = (ctx.applicationContext as VBHelper).container.db

        return when (method) {

            METHOD_GET_CARD_NAMES -> runCatching {
                val names = db.cardDao().getAllCards().map { it.name }.toTypedArray()
                Bundle().apply { putStringArray(EXTRA_CARD_NAMES_LIST, names) }
            }.getOrElse {
                Bundle().apply { putStringArray(EXTRA_CARD_NAMES_LIST, emptyArray()) }
            }

            METHOD_IMPORT_CARD -> {
                val cardBytes  = extras?.getByteArray(EXTRA_CARD_BYTES)
                    ?: return Bundle().apply { putBoolean(RESULT_OK, false) }
                val customName = extras.getString(EXTRA_CARD_NAME)

                runCatching {
                    runBlocking {
                        val dimReader  = DimReader()
                        val peeked     = cardBytes.inputStream().use { dimReader.readCard(it, false) }
                        val builtInName = peeked.spriteData.text
                        val dimId       = peeked.header.dimId

                        val alreadyExists =
                            db.cardDao().getCardByName(builtInName) != null ||
                            db.cardDao().getCardByCardId(dimId).isNotEmpty()

                        if (!alreadyExists) {
                            CardImportController(db).importCard(cardBytes.inputStream())
                            // Rename to the user's custom label if it differs from the DIM's text
                            if (!customName.isNullOrBlank() && customName != builtInName) {
                                db.cardDao().getCardByName(builtInName)?.let { card ->
                                    db.cardDao().renameCard(card.id.toInt(), customName)
                                }
                            }
                        }
                    }
                    Bundle().apply { putBoolean(RESULT_OK, true) }
                }.getOrElse {
                    Bundle().apply { putBoolean(RESULT_OK, false) }
                }
            }

            else -> Bundle().apply { putBoolean(RESULT_OK, false) }
        }
    }

    // ---- Unused mandatory overrides ----
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?,
                       selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null
    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?,
                        selectionArgs: Array<out String>?): Int = 0
}

