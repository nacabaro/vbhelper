package com.github.nacabaro.vbhelper.screens.settingsScreen.controllers

import androidx.room.Room
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.github.nacabaro.vbhelper.database.AppDatabase
import com.github.nacabaro.vbhelper.di.VBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class DatabaseManagementController(
    val componentActivity: ComponentActivity,
    val application: VBHelper
) {
    private val roomDbName = "internalDb"
    private val requiredLegacyVersions = setOf(1, 2, 3, 4, 5)
    private val requiredCoreTables = setOf(
        "UserCharacter",
        "Character",
        "Card",
        "CardCharacter",
        "TransformationHistory",
    )

    private data class ValidationResult(
        val isValid: Boolean,
        val reason: String? = null,
    )

    fun exportDatabase( destinationUri: Uri) {
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val dbFile = File(componentActivity.getDatabasePath(roomDbName).absolutePath)
                if (!dbFile.exists()) {
                    throw IllegalStateException("Database file does not exist!")
                }

                application.container.db.close()

                componentActivity.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    dbFile.inputStream().use { inputStream ->
                        copyFile(inputStream, outputStream)
                    }
                } ?: throw IllegalArgumentException("Unable to open destination Uri for writing")

                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "Database exported successfully!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(componentActivity, "Closing application to avoid changes.", Toast.LENGTH_LONG).show()
                    componentActivity.finishAffinity()
                }
            } catch (e: Exception) {
                Log.e("ScanScreenController", "Error exporting database $e")
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "Error exporting database: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    fun importDatabase(sourceUri: Uri) {
        componentActivity.lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (!getFileNameFromUri(sourceUri)!!.endsWith(".vbhelper")) {
                    componentActivity.runOnUiThread {
                        Toast.makeText(componentActivity, "Invalid file format", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val validationResult = validateImportBackup(sourceUri)
                if (!validationResult.isValid) {
                    componentActivity.runOnUiThread {
                        Toast.makeText(
                            componentActivity,
                            validationResult.reason
                                ?: "Import blocked: backup is incompatible with this VBH-VW version.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    return@launch
                }

                application.container.db.close()

                val dbPath = componentActivity.getDatabasePath(roomDbName)
                val shmFile = File(dbPath.parent, "$roomDbName-shm")
                val walFile = File(dbPath.parent, "$roomDbName-wal")

                // Delete existing database files
                if (dbPath.exists()) dbPath.delete()
                if (shmFile.exists()) shmFile.delete()
                if (walFile.exists()) walFile.delete()

                val dbFile = File(dbPath.absolutePath)

                componentActivity.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    dbFile.outputStream().use { outputStream ->
                        copyFile(inputStream, outputStream)
                    }
                } ?: throw IllegalArgumentException("Unable to open source Uri for reading")

                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "Database imported successfully!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(componentActivity, "Reopen the app to finish import process!", Toast.LENGTH_LONG).show()
                    componentActivity.finishAffinity()
                }
            } catch (e: Exception) {
                Log.e("ScanScreenController", "Error importing database $e")
                componentActivity.runOnUiThread {
                    Toast.makeText(componentActivity, "Error importing database: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun validateImportBackup(sourceUri: Uri): ValidationResult {
        val tempDbName = "import_validation_temp.db"
        val tempDbPath = componentActivity.getDatabasePath(tempDbName)
        val tempShm = File(tempDbPath.parentFile, "$tempDbName-shm")
        val tempWal = File(tempDbPath.parentFile, "$tempDbName-wal")

        try {
            if (tempDbPath.exists()) tempDbPath.delete()
            if (tempShm.exists()) tempShm.delete()
            if (tempWal.exists()) tempWal.delete()

            componentActivity.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                tempDbPath.outputStream().use { outputStream ->
                    copyFile(inputStream, outputStream)
                }
            } ?: return ValidationResult(false, "Import blocked: unable to read selected backup file.")

            // Fast static checks before attempting full Room open/migration.
            val legacyVersion = readPragmaUserVersion(tempDbPath)
            if (legacyVersion !in requiredLegacyVersions) {
                return ValidationResult(
                    false,
                    "Import blocked: unsupported DB version ($legacyVersion). Use a VBH-VW export from a compatible app version."
                )
            }

            val tableNames = readTableNames(tempDbPath)
            if (!tableNames.contains("room_master_table")) {
                return ValidationResult(
                    false,
                    "Import blocked: backup is not a Room VBH-VW database (room metadata missing)."
                )
            }

            val missingTables = requiredCoreTables.filterNot { tableNames.contains(it) }
            if (missingTables.isNotEmpty()) {
                return ValidationResult(
                    false,
                    "Import blocked: backup is missing required tables (${missingTables.joinToString(", ")})."
                )
            }

            // Hard validation: open with the app's exact Room schema + migrations.
            val probeDb = Room.databaseBuilder(
                componentActivity,
                AppDatabase::class.java,
                tempDbName
            )
                .addMigrations(AppDatabase.MIGRATION_1_2)
                .addMigrations(AppDatabase.MIGRATION_2_3)
                .addMigrations(AppDatabase.MIGRATION_3_5)
                .addMigrations(AppDatabase.MIGRATION_4_5)
                .addMigrations(AppDatabase.MIGRATION_5_6)
                .build()

            try {
                probeDb.openHelper.writableDatabase.query("SELECT 1").close()
            } catch (e: Exception) {
                return ValidationResult(
                    false,
                    "Import blocked: backup cannot be opened/migrated by this VBH-VW build."
                )
            } finally {
                probeDb.close()
            }

            return ValidationResult(true)
        } finally {
            if (tempDbPath.exists()) tempDbPath.delete()
            if (tempShm.exists()) tempShm.delete()
            if (tempWal.exists()) tempWal.delete()
        }
    }

    private fun readPragmaUserVersion(dbFile: File): Int {
        val sqliteDb = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        try {
            sqliteDb.rawQuery("PRAGMA user_version", null).use { cursor ->
                if (cursor.moveToFirst()) {
                    return cursor.getInt(0)
                }
            }
            return -1
        } finally {
            sqliteDb.close()
        }
    }

    private fun readTableNames(dbFile: File): Set<String> {
        val sqliteDb = SQLiteDatabase.openDatabase(
            dbFile.absolutePath,
            null,
            SQLiteDatabase.OPEN_READONLY
        )
        try {
            val tableNames = mutableSetOf<String>()
            sqliteDb.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null)
                .use { cursor ->
                    while (cursor.moveToNext()) {
                        tableNames.add(cursor.getString(0))
                    }
                }
            return tableNames
        } finally {
            sqliteDb.close()
        }
    }

    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val cursor = componentActivity.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                fileName = it.getString(nameIndex)
            }
        }
        return fileName
    }
}