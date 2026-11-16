package com.shadowsystem

import android.app.Application
import com.shadowsystem.di.AppContainer

class ShadowApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
