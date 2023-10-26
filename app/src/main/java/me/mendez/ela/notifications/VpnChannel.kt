package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import me.mendez.ela.R

object VpnChannel : BaseNotificationChannel<VpnChannel.NotificationCreator>() {
    override val CHANNEL_ID = "vpn_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "VPN"
    const val FOREGROUND_ID = 1
    const val ERROR_ID = 2

    public override fun createNotification(context: Context): NotificationCreator = NotificationCreator(context)

    class NotificationCreator(val context: Context) {
        fun runningNotification(): Notification {
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_24)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentText("Supervisando tráfico red")
                .setOngoing(true)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .build()
        }

        fun errorNotification(): Notification {
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_24)
                .setColor(Color.RED)
                .setContentTitle("Ela")
                .setContentText("No se pudo iniciar Ela")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }
    }

}
