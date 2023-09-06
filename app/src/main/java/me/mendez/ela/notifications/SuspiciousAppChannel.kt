package me.mendez.ela.notifications

import android.app.NotificationManager

object SuspiciousAppChannel: BaseNotificationChannel() {
    override val CHANNEL_ID = "suspicious_apps"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Aplicaciones sospechoso"
}