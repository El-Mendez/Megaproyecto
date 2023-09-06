package me.mendez.ela

import android.app.Application
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import me.mendez.ela.notifications.SuspiciousAppChannel
import me.mendez.ela.notifications.SuspiciousTrafficChannel
import me.mendez.ela.notifications.VpnChannel

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SuspiciousAppChannel.createChannel(this)
            SuspiciousTrafficChannel.createChannel(this)
            VpnChannel.createChannel(this)
        }
    }
}