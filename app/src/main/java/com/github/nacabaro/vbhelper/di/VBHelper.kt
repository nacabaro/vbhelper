package com.github.nacabaro.vbhelper.di

import DefaultAppContainer
import android.app.Application
import com.github.nacabaro.vbhelper.companion.validation.ValidatedCardManager
import com.github.nacabaro.vbhelper.companion.logs.CompanionLogService

class VBHelper : Application() {
    lateinit var container: DefaultAppContainer

    val validatedCardManager: ValidatedCardManager
        get() = container.validatedCardManager

    val companionLogService: CompanionLogService
        get() = container.companionLogService

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}