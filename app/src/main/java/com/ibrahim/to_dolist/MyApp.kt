package com.ibrahim.to_dolist

import android.app.Application
import com.google.firebase.FirebaseApp
import com.ibrahim.to_dolist.di.mindListDi
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        startKoin {
            androidContext(this@MyApp)
            modules(mindListDi)
        }
    }
}
