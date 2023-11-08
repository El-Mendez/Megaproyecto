package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.*
import me.mendez.ela.R

object SuspiciousTrafficChannel : BaseNotificationChannel<SuspiciousTrafficChannel.NotificationCreator>() {
    override val CHANNEL_ID = "suspicious_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Tráfico sospechoso"
    override val bubbles = true

    override fun createNotification(context: Context): NotificationCreator = NotificationCreator(context)


    class NotificationCreator(val context: Context) {
        fun newSuspiciousTraffic(
            domain: String,
        ): Notification {
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_24)
                .setColor(Color.RED)
                .setContentTitle("Dominio bloqueado")
                .setContentText(domain)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(domain)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }
    }
}
