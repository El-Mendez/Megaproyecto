package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import me.mendez.ela.R
import java.util.Date

object SuspiciousTrafficChannel : BaseNotificationChannel() {
    override val CHANNEL_ID = "suspicious_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Tráfico sospechoso"


    fun newSuspiciousTraffic(context: Context, app: String): Notification {
        val ela = Person.Builder()
            .setName("Ela")
            .setBot(true)
            .build()

        val you: Person = Person.Builder()
            .setName("Tú")
            .setImportant(true)
            .build()

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_24)
            .setStyle(
                NotificationCompat
                    .MessagingStyle(ela)
                    .addMessage("what what what", Date().time, ela)
                    .addMessage("no u", Date().time, you)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
    }
}
