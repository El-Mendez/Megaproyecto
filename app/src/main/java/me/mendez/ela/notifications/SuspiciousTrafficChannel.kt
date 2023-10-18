package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.*
import androidx.core.graphics.drawable.IconCompat
import me.mendez.ela.BubbleActivity
import me.mendez.ela.R
import me.mendez.ela.services.SuspiciousNotification
import java.util.Date

object SuspiciousTrafficChannel : BaseNotificationChannel<SuspiciousTrafficChannel.NotificationCreator>() {
    override val CHANNEL_ID = "suspicious_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Tráfico sospechoso"
    private const val REMOTE_INPUT_TAG = "reply_ela_traffic_input_tag"

    private val ela: Person = Person.Builder()
        .setName("Ela")
        .setBot(true)
        .build()

    private val you: Person = Person.Builder()
        .setName("Tú")
        .setImportant(true)
        .build()

    fun recoverSubmittedText(intent: Intent): String? {
        return RemoteInput
            .getResultsFromIntent(intent)
            ?.getCharSequence(REMOTE_INPUT_TAG)
            ?.toString()
    }

    fun addMessageToChat(context: Context, domain: String, message: String, user: Boolean) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = notificationManager
            .activeNotifications
            .find { it.id == domain.hashCode() }
            ?.notification ?: return

        val oldMessages = NotificationCompat.MessagingStyle
            .extractMessagingStyleFromNotification(notification)
            ?.messages ?: return

        val newMessages = NotificationCompat.MessagingStyle(ela)
            .setConversationTitle(domain)

        oldMessages.forEach {
            newMessages.addMessage(it.text, it.timestamp, it.person)
        }
        newMessages.addMessage(message, Date().time, if (user) you else ela)

        notify(context, domain.hashCode()) {
            newSuspiciousTraffic(
                domain,
                newMessages,
            )
        }
    }

    fun replyActionHash(domain: String): Int = ":replyAction:${domain}".hashCode()

    fun whitelistActionHash(domain: String): Int = ":whitelistAction:${domain}".hashCode()

    fun dismissNotificationHash(domain: String): Int = ":dismissNotification:${domain}".hashCode()

    fun bubbleNotificationHash(domain: String): Int = ":bubble:${domain}".hashCode()

    override fun createNotification(context: Context): NotificationCreator = NotificationCreator(context)

    class NotificationCreator(val context: Context) {
        private fun createReplyAction(domain: String): NotificationCompat.Action {
            val remoteInput = RemoteInput
                .Builder(REMOTE_INPUT_TAG)
                .setLabel("Escribe aquí...")
                .build()

            val replyPendingIntent = PendingIntent
                .getBroadcast(
                    context,
                    replyActionHash(domain),
                    SuspiciousNotification.submitFromNotification(context, domain),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE,
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
                    SuspiciousNotification.addToWhitelist(context, domain),
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
                    SuspiciousNotification.dismissNotification(context, domain),
                    PendingIntent.FLAG_IMMUTABLE,
                )
        }

        private fun createBubbleIntent(domain: String): PendingIntent {
            return PendingIntent
                .getActivity(
                    context,
                    bubbleNotificationHash(domain),
                    Intent(context, BubbleActivity::class.java)
                        .setAction(Intent.ACTION_VIEW),
                    PendingIntent.FLAG_MUTABLE,
                )
        }

        private fun createNewChatConversation(domain: String): NotificationCompat.MessagingStyle {
            return NotificationCompat
                .MessagingStyle(ela)
                .setConversationTitle(domain)
        }

        fun newSuspiciousTraffic(
            domain: String,
            message: String
        ): Notification {
            val messages = createNewChatConversation(domain)
            messages.addMessage(message, Date().time, ela)
            return newSuspiciousTraffic(domain, messages)
        }

        fun newSuspiciousTraffic(
            domain: String,
            messages: NotificationCompat.MessagingStyle,
        ): Notification {
            val bubbleMetadata = NotificationCompat.BubbleMetadata
                .Builder(
                    createBubbleIntent(domain),
                    IconCompat.createWithResource(context, R.drawable.logo_24)
                ).setDesiredHeight(600)
                .build()

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_24)
                .setStyle(messages)
                .addAction(createReplyAction(domain))
                .addAction(createAddToWhitelistAction(domain))
                .setDeleteIntent(createDismissIntent(domain))
                .setBubbleMetadata(bubbleMetadata)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }
    }
}
