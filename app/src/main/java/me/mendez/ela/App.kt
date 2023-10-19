package me.mendez.ela

import android.app.Application
import android.content.BroadcastReceiver
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import me.mendez.ela.notifications.*
import me.mendez.ela.services.DailyTip
import me.mendez.ela.services.PermissionCheck

@HiltAndroidApp
class App : Application() {
    private lateinit var listener: BroadcastReceiver
    override fun onCreate() {
        super.onCreate()
        createDailyTip()
        createNotificationChannel()
        listener = PermissionCheck.registerListening(this)
    }

    override fun onTerminate() {
        PermissionCheck.unregisterListening(this, listener)
        super.onTerminate()
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
