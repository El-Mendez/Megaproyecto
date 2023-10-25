package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import me.mendez.ela.R

object DailyTipChannel : BaseNotificationChannel<DailyTipChannel.NotificationCreator>() {
    override val CHANNEL_ID = "daily_tip"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_LOW
    override val NAME = "Consejo Diario de Ciberseguridad"
    val TIP_ID = 4

    override fun createNotification(context: Context): NotificationCreator = NotificationCreator(context)

    class NotificationCreator(val context: Context) {
        fun newDailyTip(tip: String): Notification {
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_24)
                .setColor(Color.WHITE)
                .setContentText("Sabías qué...?")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(tip)
                )
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
        }
    }
}
