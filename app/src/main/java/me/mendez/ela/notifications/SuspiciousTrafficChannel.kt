package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import me.mendez.ela.R

object SuspiciousTrafficChannel : BaseNotificationChannel() {
    override val CHANNEL_ID = "suspicious_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Tráfico sospechoso"


    fun newSuspiciousApp(context: Context, app: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_24)
            .setColor(Color.RED)
            .setContentTitle(app)
            .setContentText(":O")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
}
