package com.github.nacabaro.vbhelper.di

import com.github.nacabaro.vbhelper.database.AppDatabase

interface AppContainer {
    val db: AppDatabase
}