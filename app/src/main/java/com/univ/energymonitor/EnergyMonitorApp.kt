package com.univ.energymonitor

import android.app.Application
import com.univ.energymonitor.data.AppContainer

/**
 * Custom Application class — instantiated once when the process starts.
 * Holds the AppContainer (our manual DI singleton).
 *
 * Declared in AndroidManifest.xml via android:name=".EnergyMonitorApp".
 */
class EnergyMonitorApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}