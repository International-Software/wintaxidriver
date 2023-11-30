package com.example.taxi

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.multidex.MultiDex
import com.example.taxi.di.NetworkModule
import com.example.taxi.di.koinModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

import org.koin.core.logger.Level

class TaxiApp : Application() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate() {
        super.onCreate()

        createNotificationChannels()
        startKoin {
            androidLogger(Level.ERROR)
            androidContext(this@TaxiApp)
            modules(listOf(koinModule, NetworkModule))
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    private fun createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "channel_notif",
                "Channel Notif",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "notification"


            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}