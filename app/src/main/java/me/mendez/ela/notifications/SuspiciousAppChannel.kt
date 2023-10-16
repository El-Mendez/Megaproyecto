package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import me.mendez.ela.R

object SuspiciousAppChannel : BaseNotificationChannel() {
    override val CHANNEL_ID = "suspicious_apps"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Aplicaciones sospechoso"
    val SUSPICIOUS_APP_ID = 3

    fun newSuspiciousApp(context: Context, packages: List<String>): Notification {
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
