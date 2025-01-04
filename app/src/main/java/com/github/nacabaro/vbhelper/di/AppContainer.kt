package com.github.nacabaro.vbhelper.di

import com.github.nacabaro.vbhelper.database.AppDatabase.AppDatabase

interface AppContainer {
    val db: AppDatabase
}