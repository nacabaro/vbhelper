package com.github.nacabaro.vbhelper.di

import DefaultAppContainer
import android.app.Application
import androidx.room.Room
import com.github.nacabaro.vbhelper.companion.logging.TinyLogTree
import com.github.nacabaro.vbhelper.companion.logs.CompanionLogService
import com.github.nacabaro.vbhelper.companion.validation.CompanionValidatedDatabase
import com.github.nacabaro.vbhelper.companion.validation.ValidatedCardManager
import timber.log.Timber

class VBHelper : Application() {
    lateinit var container: DefaultAppContainer
    lateinit var validatedCardDatabase: CompanionValidatedDatabase
    lateinit var validatedCardManager: ValidatedCardManager
    val companionLogService = CompanionLogService()

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)

        Timber.plant(Timber.DebugTree())
        Timber.plant(TinyLogTree(this))
        val originalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Timber.e(throwable, "VBHelper crashed on thread ${thread.name}")
            TinyLogTree.shutdown()
            originalExceptionHandler?.uncaughtException(thread, throwable)
        }

        validatedCardDatabase = Room.databaseBuilder(
            applicationContext,
            CompanionValidatedDatabase::class.java,
            "vbhelper_companion_tools"
        ).build()
        validatedCardManager = ValidatedCardManager(validatedCardDatabase.validatedCardDao())
    }
}