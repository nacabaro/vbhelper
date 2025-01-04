package com.github.nacabaro.vbhelper.di

import DefaultAppContainer
import android.app.Application

class VBHelper : Application() {
    lateinit var container: DefaultAppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(applicationContext)
    }
}