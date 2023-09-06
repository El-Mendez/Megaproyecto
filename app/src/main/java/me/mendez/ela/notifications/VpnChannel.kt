package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import me.mendez.ela.R

object VpnChannel: BaseNotificationChannel() {
    override val CHANNEL_ID = "vpn_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_LOW
    override val NAME = "VPN"
    const val FOREGROUND_ID = 1
    const val ERROR_ID = 2

    fun runningNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_warning_24)
            .setContentTitle("Ela te está cuidando")
            .setContentText("Supervisando tráfico red...")
            .build()
    }

    fun errorNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_warning_24)
            .setContentTitle("No se pudo iniciar Ela")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
}