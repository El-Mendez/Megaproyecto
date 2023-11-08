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
import me.mendez.ela.chat.Message
import me.mendez.ela.chat.Sender
import me.mendez.ela.services.SuspiciousCommand
import me.mendez.ela.services.broadcast

object SuspiciousTrafficChannel : BaseNotificationChannel<SuspiciousTrafficChannel.NotificationCreator>() {
    override val CHANNEL_ID = "suspicious_traffic"
    override val IMPORTANCE = NotificationManager.IMPORTANCE_HIGH
    override val NAME = "Tráfico sospechoso"
    override val bubbles = true
    private const val REMOTE_INPUT_TAG = "reply_ela_traffic_input_tag"

    override fun createNotification(context: Context): NotificationCreator = NotificationCreator(context)

    private fun ela(context: Context): Person = Person.Builder()
        .setName("Ela")
        .setImportant(true)
        .setIcon(
            IconCompat.createWithResource(
                context,
                R.drawable.ic_launcher_foreground
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
                IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground)
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

    fun addMessagesToNotification(context: Context, domain: String, conversation: Long, messages: List<Message>) {
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
        messages.forEach {
            newMessages.addMessage(it.content, it.date.time, if (it.user == Sender.USER) you else ela)
        }

        notify(context, domain.hashCode()) {
            newSuspiciousTraffic(
                domain,
                conversation,
                newMessages,
            )
        }
    }

    class NotificationCreator(val context: Context) {
        private fun replyOnNotificationAction(domain: String, conversation: Long): NotificationCompat.Action {
            val remoteInput = RemoteInput
                .Builder(REMOTE_INPUT_TAG)
                .setLabel("Escribe aquí...")
                .build()

            val replyPendingIntent = SuspiciousCommand.SUBMIT_FROM_NOTIFICATION
                .broadcast(context, domain, conversation)

            return NotificationCompat.Action
                .Builder(R.drawable.round_send_24, "Responder", replyPendingIntent)
                .addRemoteInput(remoteInput)
                .build()
        }

        private fun addToWhitelistFromNotificationAction(
            domain: String,
            conversation: Long
        ): NotificationCompat.Action {
            val allowPendingIntent = SuspiciousCommand.ADD_TO_WHITELIST_FROM_NOTIFICATION
                .broadcast(context, domain, conversation)

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
            conversation: Long,
            messages: List<Message>
        ): Notification {
            val messageStyle = newChatConversation(domain)
            messages.forEach {
                messageStyle.addMessage(it.content, it.date.time, if (it.user == Sender.USER) you else ela(context))
            }
            return newSuspiciousTraffic(domain, conversation, messageStyle)
        }

        fun newSuspiciousTraffic(
            domain: String,
            conversation: Long,
            messageStyle: NotificationCompat.MessagingStyle,
        ): Notification {
            addShortcut(domain, context)

            val bubbleMetadata = NotificationCompat.BubbleMetadata
                .Builder(
                    BubbleActivity.createLaunchIntent(context, domain, conversation),
                    IconCompat.createWithResource(context, R.drawable.ic_launcher_foreground)
                ).setDesiredHeight(600)
                .setAutoExpandBubble(true)
                .setDeleteIntent(
                    SuspiciousCommand.DISMISS_BUBBLE
                        .broadcast(context, domain, conversation)
                )
                .build()

            return NotificationCompat.Builder(context, CHANNEL_ID)
                .setBubbleMetadata(bubbleMetadata)
                .setSmallIcon(R.drawable.logo_24)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setShortcutId(domain)
                .setLocusId(LocusIdCompat(domain))
                .addPerson(ela(context))
                .addAction(replyOnNotificationAction(domain, conversation))
                .addAction(addToWhitelistFromNotificationAction(domain, conversation))
                .setShowWhen(true)
                .setStyle(messageStyle)
                .setDeleteIntent(
                    SuspiciousCommand.DISMISS_NOTIFICATION
                        .broadcast(context, domain, conversation)
                )
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()
        }
    }
}
