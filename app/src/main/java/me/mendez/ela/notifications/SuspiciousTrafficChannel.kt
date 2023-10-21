package me.mendez.ela.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.core.app.*
import androidx.core.content.LocusIdCompat
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import me.mendez.ela.BubbleActivity
import me.mendez.ela.MainActivity
import me.mendez.ela.R
import me.mendez.ela.services.SuspiciousCommand
import me.mendez.ela.services.broadcast
import java.util.Date

object SuspiciousTrafficChannel : BaseNotificationChannel<SuspiciousTrafficChannel.NotificationCreator>() {
    override val CHANNEL_ID = "suspicious_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Tráfico sospechoso"
    override val bubbles = true
    private const val REMOTE_INPUT_TAG = "reply_ela_traffic_input_tag"

    override fun createNotification(context: Context): NotificationCreator = NotificationCreator(context)

    private fun ela(context: Context): Person = Person.Builder()
        .setName("Ela")
        .setIcon(
            IconCompat.createWithResource(
                context,
                R.drawable.logo_24
            )
        )
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

    private fun addShortcut(domain: String, context: Context) {
        val shortcut = ShortcutInfoCompat.Builder(context, domain)
            .setLocusId(LocusIdCompat(domain))
            .setActivity(ComponentName(context, MainActivity::class.java))
            .setShortLabel(domain)
            .setIcon(
                IconCompat.createWithResource(context, R.drawable.logo_24)
            )
            .setLongLived(true)
            .setCategories(setOf("me.mendez.ela.bubbles.category.CHAT_BLOCK"))
            .setIntent(
                Intent(context, MainActivity::class.java)
                    .setAction(Intent.ACTION_VIEW)
            ).setPerson(ela(context))
            .build()

        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
    }

    fun addMessageToChat(context: Context, domain: String, message: String, user: Boolean) {
        val ela = ela(context)
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

    class NotificationCreator(val context: Context) {
        private fun replyOnNotificationAction(domain: String): NotificationCompat.Action {
            val remoteInput = RemoteInput
                .Builder(REMOTE_INPUT_TAG)
                .setLabel("Escribe aquí...")
                .build()

            val replyPendingIntent = SuspiciousCommand.SUBMIT_FROM_NOTIFICATION
                .broadcast(context, domain)

            return NotificationCompat.Action
                .Builder(R.drawable.round_send_24, "Responder", replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build()
        }

        private fun addToWhitelistFromNotificationAction(domain: String): NotificationCompat.Action {
            val allowPendingIntent = SuspiciousCommand.ADD_TO_WHITELIST_FROM_NOTIFICATION
                .broadcast(context, domain)

            return NotificationCompat.Action
                .Builder(
                    R.drawable.baseline_gpp_good_24,
                    "Permitir",
                    allowPendingIntent,
                ).build()
        }

        private fun newChatConversation(domain: String): NotificationCompat.MessagingStyle {
            return NotificationCompat
                .MessagingStyle(ela(context))
                .setConversationTitle(domain)
        }

        fun newSuspiciousTraffic(
            domain: String,
            message: String
        ): Notification {
            val messages = newChatConversation(domain)
            messages.addMessage(message, Date().time, ela(context))
            return newSuspiciousTraffic(domain, messages)
        }

        fun newSuspiciousTraffic(
            domain: String,
            messages: NotificationCompat.MessagingStyle,
        ): Notification {
            addShortcut(domain, context)

            val bubbleMetadata = NotificationCompat.BubbleMetadata
                .Builder(
                    BubbleActivity.createLaunchIntent(context, domain),
                    IconCompat.createWithResource(context, R.drawable.logo_24)
                ).setDesiredHeight(600)
                .setAutoExpandBubble(true)
                .setDeleteIntent(
                    SuspiciousCommand.DISMISS_BUBBLE
                        .broadcast(context, domain)
                )
                .build()

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setBubbleMetadata(bubbleMetadata)
                .setSmallIcon(R.drawable.logo_24)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setShortcutId(domain)
                .setLocusId(LocusIdCompat(domain))
                .addPerson(ela(context))
                .addAction(replyOnNotificationAction(domain))
                .addAction(addToWhitelistFromNotificationAction(domain))
                .setShowWhen(true)
                .setStyle(messages)
                .setDeleteIntent(
                    SuspiciousCommand.DISMISS_NOTIFICATION
                        .broadcast(context, domain)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }
    }
}
