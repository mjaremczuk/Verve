package com.mjaremczuk.motiv

import android.app.Application
import com.mjaremczuk.motiv.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MotivApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MotivApplication)
            modules(appModule)
        }
    }
}
