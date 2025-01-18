package com.github.nacabaro.vbhelper.screens.settingsScreen

import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.github.nacabaro.vbhelper.di.VBHelper
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.InputStream
import java.io.OutputStream


class NewSettingsScreenControllerImpl(
    private val context: ComponentActivity,
): NewSettingsScreenController {
    private val filePickerLauncher: ActivityResultLauncher<String>
    private val filePickerOpenerLauncher: ActivityResultLauncher<Array<String>>

    init {
        filePickerLauncher = context.registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/octet-stream")
        ) { uri ->
                if (uri != null) {
                    exportDatabase("internalDb", uri)
                } else {
                    context.runOnUiThread {
                        Toast.makeText(context, "No destination selected", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        filePickerOpenerLauncher = context.registerForActivityResult(
            ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                importDatabase("internalDb", uri)
            } else {
                context.runOnUiThread {
                    Toast.makeText(context, "No source selected", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onClickOpenDirectory() {
        filePickerLauncher.launch("My application data.vbhelper")
    }

    override fun onClickImportDatabase() {
        filePickerOpenerLauncher.launch(arrayOf("application/octet-stream"))
    }

    private fun exportDatabase(roomDbName: String, destinationUri: Uri) {
        context.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val application = context.applicationContext as VBHelper
                val dbFile = File(context.getDatabasePath(roomDbName).absolutePath)
                if (!dbFile.exists()) {
                    throw IllegalStateException("Database file does not exist!")
                }

                application.container.db.close()

                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    dbFile.inputStream().use { inputStream ->
                        copyFile(inputStream, outputStream)
                    }
                } ?: throw IllegalArgumentException("Unable to open destination Uri for writing")

                context.runOnUiThread {
                    Toast.makeText(context, "Database exported successfully!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, "Closing application to avoid changes.", Toast.LENGTH_LONG).show()
                    context.finishAffinity()
                }
            } catch (e: Exception) {
                Log.e("ScanScreenController", "Error exporting database $e")
                context.runOnUiThread {
                    Toast.makeText(context, "Error exporting database: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun importDatabase(roomDbName: String, sourceUri: Uri) {
        context.lifecycleScope.launch(Dispatchers.IO) {
            try {
                var application = context.applicationContext as VBHelper

                if (!getFileNameFromUri(sourceUri)!!.endsWith(".vbhelper")) {
                    context.runOnUiThread {
                        Toast.makeText(context, "Invalid file format", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                application.container.db.close()

                val dbPath = context.getDatabasePath(roomDbName)
                val shmFile = File(dbPath.parent, "$roomDbName-shm")
                val walFile = File(dbPath.parent, "$roomDbName-wal")

                // Delete existing database files
                if (dbPath.exists()) dbPath.delete()
                if (shmFile.exists()) shmFile.delete()
                if (walFile.exists()) walFile.delete()

                val dbFile = File(dbPath.absolutePath)

                context.contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                    dbFile.outputStream().use { outputStream ->
                        copyFile(inputStream, outputStream)
                    }
                } ?: throw IllegalArgumentException("Unable to open source Uri for reading")

                context.runOnUiThread {
                    Toast.makeText(context, "Database imported successfully!", Toast.LENGTH_SHORT).show()
                    Toast.makeText(context, "Reopen the app to finish import process!", Toast.LENGTH_LONG).show()
                    context.finishAffinity()
                }
            } catch (e: Exception) {
                Log.e("ScanScreenController", "Error importing database $e")
                context.runOnUiThread {
                    Toast.makeText(context, "Error importing database: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        var fileName: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                fileName = it.getString(nameIndex)
            }
        }
        return fileName
    }

    private fun copyFile(inputStream: InputStream, outputStream: OutputStream) {
        val buffer = ByteArray(1024)
        var bytesRead: Int
        while (inputStream.read(buffer).also { bytesRead = it } != -1) {
            outputStream.write(buffer, 0, bytesRead)
        }
        outputStream.flush()
    }
}