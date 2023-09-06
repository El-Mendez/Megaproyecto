package me.mendez.ela.notifications

import android.app.NotificationManager

object SuspiciousTrafficChannel: BaseNotificationChannel() {
    override val CHANNEL_ID = "suspicious_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Tráfico sospechoso"
}