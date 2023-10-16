package me.mendez.ela

import android.app.Application
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import me.mendez.ela.notifications.DailyTipChannel
import me.mendez.ela.notifications.SuspiciousAppChannel
import me.mendez.ela.notifications.SuspiciousTrafficChannel
import me.mendez.ela.notifications.VpnChannel
import me.mendez.ela.services.DailyTip

@HiltAndroidApp
class App : Application() {
    override fun onCreate() {
        super.onCreate()
        createDailyTip()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            SuspiciousAppChannel.createChannel(this)
            SuspiciousTrafficChannel.createChannel(this)
            VpnChannel.createChannel(this)
            DailyTipChannel.createChannel(this)
        }
    }

    private fun createDailyTip() {
        DailyTip.scheduleNotifications(this)
    }
}
