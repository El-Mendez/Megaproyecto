package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.RemoteInput
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import me.mendez.ela.R
import me.mendez.ela.services.SuspiciousNotification
import java.util.Date

object SuspiciousTrafficChannel : BaseNotificationChannel<SuspiciousTrafficChannel.NotificationCreator>() {
    override val CHANNEL_ID = "suspicious_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Tráfico sospechoso"

    val ela = Person.Builder()
        .setName("Ela")
        .setBot(true)
        .build()

    val you: Person = Person.Builder()
        .setName("Tú")
        .setImportant(true)
        .build()

    fun replyActionHash(domain: String): Int = ":replyAction:${domain}".hashCode()

    fun whitelistActionHash(domain: String): Int = ":whitelistAction:${domain}".hashCode()

    fun dismissNotificationHash(domain: String): Int = ":dismissNotification:${domain}".hashCode()

    override fun createNotification(context: Context): NotificationCreator = NotificationCreator(context)


    class NotificationCreator(val context: Context) {
        private fun createReplyAction(domain: String): NotificationCompat.Action {
            val remoteInput = RemoteInput
                .Builder("reply_ela_traffic")
                .setLabel("Escribe aquí...")
                .build()

            val replyPendingIntent = PendingIntent
                .getBroadcast(
                    context,
                    replyActionHash(domain),
                    Intent(context, SuspiciousNotification::class.java).apply {
                        putExtra("domain", domain)
                        putExtra("action", "reply")
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                )

            return NotificationCompat.Action
                .Builder(
                    R.drawable.round_send_24,
                    "Responder",
                    replyPendingIntent
                )
                .addRemoteInput(remoteInput)
                .build()
        }

        private fun createAddToWhitelistAction(domain: String): NotificationCompat.Action {
            val allowPendingIntent = PendingIntent
                .getBroadcast(
                    context,
                    whitelistActionHash(domain),
                    Intent(context, SuspiciousNotification::class.java).apply {
                        putExtra("domain", domain)
                        putExtra("action", "whitelist")
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
                )

            return NotificationCompat.Action
                .Builder(
                    R.drawable.baseline_gpp_good_24,
                    "Permitir",
                    allowPendingIntent,
                ).build()
        }

        private fun createDismissIntent(domain: String): PendingIntent {
            return PendingIntent
                .getBroadcast(
                    context,
                    dismissNotificationHash(domain),
                    Intent(context, SuspiciousNotification::class.java).apply {
                        putExtra("domain", domain)
                        putExtra("action", "dismiss")
                    },
                    PendingIntent.FLAG_IMMUTABLE,
                )
        }

        fun newSuspiciousTraffic(domain: String): Notification {
            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_24)
                .setStyle(
                    NotificationCompat
                        .MessagingStyle(ela)
                        .setConversationTitle(domain)
                        .addMessage("what what what", Date().time, ela)
                        .addMessage("no u", Date().time, you)
                )
                .addAction(createReplyAction(domain))
                .addAction(createAddToWhitelistAction(domain))
                .setDeleteIntent(createDismissIntent(domain))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }
    }
}
