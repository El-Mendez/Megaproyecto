package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import me.mendez.ela.R

object VpnChannel : BaseNotificationChannel() {
    override val CHANNEL_ID = "vpn_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_LOW
    override val NAME = "VPN"
    const val FOREGROUND_ID = 1
    const val ERROR_ID = 2

    fun runningNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.log_24)
            .setColor(Color.RED)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentText("Supervisando tráfico red")
            .setOngoing(true)
            .build()
    }

    fun errorNotification(context: Context): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.log_24)
            .setColor(Color.RED)
            .setContentTitle("Ela")
            .setContentText("No se pudo iniciar Ela")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
}