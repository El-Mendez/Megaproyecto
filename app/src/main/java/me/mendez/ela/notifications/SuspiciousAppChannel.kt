package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import me.mendez.ela.R

object SuspiciousAppChannel : BaseNotificationChannel<SuspiciousAppChannel.NotificationCreator>() {
    override val CHANNEL_ID = "suspicious_apps"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Aplicaciones sospechosas"
    val SUSPICIOUS_APP_ID = 3

    override fun createNotification(context: Context): NotificationCreator = NotificationCreator(context)

    class NotificationCreator(val context: Context) {
        fun newSuspiciousApp(packages: List<String>): Notification {
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_24)
                .setColor(Color.RED)
                .setContentTitle("Ela")
                .setContentText("¡Nueva aplicación sospechosa!")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(packages.joinToString("\n") { " - $it" })
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }
    }
}
