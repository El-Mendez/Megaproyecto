package me.mendez.ela.notifications

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat

abstract class BaseNotificationChannel<T> {
    protected abstract val NAME: String
    protected abstract val CHANNEL_ID: String
    protected abstract val IMPORTANCE: Int
    protected open val bubbles = false

    protected abstract fun createNotification(context: Context): T

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(context: Context, group: String? = null) {
        val channel = NotificationChannel(
            CHANNEL_ID,
            NAME,
            IMPORTANCE,
        )

        if (group != null) {
            channel.group = group
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    fun notify(context: Context, notificationId: Int, block: T.() -> Notification) {
        with(NotificationManagerCompat.from(context)) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(
                    notificationId,
                    block(createNotification(context))
                )
            }
        }
    }

    fun removeNotification(context: Context, notificationId: Int) {
        with(NotificationManagerCompat.from(context)) {
            cancel(notificationId)
        }
    }
}
