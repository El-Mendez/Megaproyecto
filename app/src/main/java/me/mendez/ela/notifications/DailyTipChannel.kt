package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import androidx.core.app.NotificationCompat
import me.mendez.ela.R

object DailyTipChannel : BaseNotificationChannel<DailyTipChannel.NotificationCreator>() {
    override val CHANNEL_ID = "daily_tip"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Consejo Diario de Ciberseguridad"
    const val TIP_ID = 4

    override fun createNotification(context: Context): NotificationCreator = NotificationCreator(context)

    class NotificationCreator(val context: Context) {
        fun newDailyTip(tip: List<String>): Notification {
            val tipText = tip.joinToString(" \n") {
                val trimmed = it.trim()
                if (trimmed.lastOrNull() != '.') {
                    "$it."
                } else {
                    it
                }
            }

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_24)
                .setColor(Color.WHITE)
                .setContentTitle("¿Sabías qué...?")
                .setContentText(tipText)
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText(tipText)
                )
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .build()
        }
    }
}
