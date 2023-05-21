package com.example.ela.services

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.ela.MainActivity
import com.example.ela.R

class NotificationService(
    private val context: Context
) {
    companion object {
        const val COUNTER_CHANNEL_ID = "network_traffic_filter"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun showNotification(amount: Int, application: String) {
        val activityIntent = Intent(context, MainActivity::class.java)
        val activityPendingIntent = PendingIntent.getActivity(
            context,
            1,
            activityIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, COUNTER_CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_warning_24)
            .setContentTitle("¡Dominio bloqueado!")
            .setContentText("Se bloquearon $amount comunicaciones sospechosas de la aplicación $application")
            .setContentIntent(activityPendingIntent)
            .build()

        notificationManager.notify(1, notification)
    }
}