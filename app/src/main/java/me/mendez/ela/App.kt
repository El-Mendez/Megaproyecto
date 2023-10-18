package me.mendez.ela

import android.app.Application
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import me.mendez.ela.notifications.*
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
            SuspiciousGroup.createChannel(
                this,
                listOf(
                    SuspiciousTrafficChannel,
                    SuspiciousAppChannel,
                )
            )
            VpnChannel.createChannel(this)
            DailyTipChannel.createChannel(this)

        }
    }

    private fun createDailyTip() {
        DailyTip.scheduleNotifications(this)
    }
}
