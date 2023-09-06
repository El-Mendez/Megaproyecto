package me.mendez.ela.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

abstract class BaseNotificationChannel {
    protected abstract val NAME: String
    protected abstract val CHANNEL_ID: String
    protected abstract val IMPORTANCE: Int

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(context: Context) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            NAME,
            IMPORTANCE,
        )

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}