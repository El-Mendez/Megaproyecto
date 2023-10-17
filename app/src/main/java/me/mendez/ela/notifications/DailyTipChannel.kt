package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import me.mendez.ela.R

object DailyTipChannel : BaseNotificationChannel() {
    override val CHANNEL_ID = "daily_tip"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_LOW
    override val NAME = "Consejo Diario de Ciberseguridad"
    val TIP_ID = 4

    fun newDailyTip(context: Context, tip: String): Notification {
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_24)
            .setColor(Color.WHITE)
            .setContentText(tip)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }
}
