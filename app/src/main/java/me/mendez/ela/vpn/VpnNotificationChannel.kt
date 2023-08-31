package me.mendez.ela.vpn

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import me.mendez.ela.BuildConfig
import me.mendez.ela.R

internal object VpnNotificationChannel {
    const val NOTIFICATION_CHANNEL_ID = BuildConfig.APPLICATION_ID
    const val FOREGROUND_ID = 1


    fun runningNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_warning_24)
            .setContentTitle("Ela te está cuidando")
            .setContentText("Supervisando tráfico red...")
            .build()
    }

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Servicio de VPN",
                NotificationManager.IMPORTANCE_LOW,
            )

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
