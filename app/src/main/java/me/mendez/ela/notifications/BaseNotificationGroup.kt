package me.mendez.ela.notifications

import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi

abstract class BaseNotificationGroup {
    protected abstract val NAME: String
    protected abstract val GROUP_ID: String

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(context: Context, channels: List<BaseNotificationChannel>) {
        val group = NotificationChannelGroup(GROUP_ID, NAME)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannelGroup(group)

        channels.forEach {
            it.createChannel(
                context,
                GROUP_ID,
            )
        }
    }
}
