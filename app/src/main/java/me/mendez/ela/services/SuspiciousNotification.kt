package me.mendez.ela.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import me.mendez.ela.chat.Message
import me.mendez.ela.notifications.SuspiciousTrafficChannel
import me.mendez.ela.persistence.database.chats.MessageDao
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.chat.ChatApi
import me.mendez.ela.chat.Sender
import me.mendez.ela.ml.MaliciousDomainClassifier
import me.mendez.ela.vpn.ElaVpnService
import javax.inject.Inject

private const val TAG = "ELA_NOTIFICATION_SERVICE"


enum class SuspiciousCommand {
    SUBMIT_FROM_NOTIFICATION,
    ADD_TO_WHITELIST_FROM_NOTIFICATION,
    DISMISS_NOTIFICATION, DISMISS_BUBBLE,
}

fun SuspiciousCommand.broadcast(context: Context, domain: String, conversation: Long): PendingIntent {
    return SuspiciousNotification.broadcast(context, domain, conversation, this)
}


@AndroidEntryPoint
class SuspiciousNotification : BroadcastReceiver() {
    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>

    private var chatApi: ChatApi? = null

    @Inject
    lateinit var messageDao: MessageDao
    private val supervisor = SupervisorJob()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        if (chatApi == null) chatApi = ChatApi.create()

        val domain = intent.getStringExtra("domain") ?: return
        val actionString = intent.getStringExtra("action") ?: return
        val conversation = intent.getStringExtra("conversation")?.toLongOrNull() ?: return

        Log.d(TAG, "new action: $actionString")

        if (actionString == "CREATE_CHAT") {
            initializeChat(domain, conversation, context, intent)
            return
        }

        val action = SuspiciousCommand.valueOf(actionString)
        when (action) {
            SuspiciousCommand.SUBMIT_FROM_NOTIFICATION -> messageFromNotification(domain, conversation, context, intent)

            SuspiciousCommand.ADD_TO_WHITELIST_FROM_NOTIFICATION -> addToWhitelist(domain, context)

            SuspiciousCommand.DISMISS_NOTIFICATION, SuspiciousCommand.DISMISS_BUBBLE ->
                dismissNotification(domain, context)
        }
    }

    private fun initializeChat(domain: String, conversation: Long, context: Context, intent: Intent) {
        val reasonString = intent.getStringExtra("reason") ?: return
        val reason = MaliciousDomainClassifier.Result.valueOf(reasonString)

        CoroutineScope(Dispatchers.IO + supervisor).launch {
            var response = chatApi!!.explainMalware(reason)

            if (response.isNullOrEmpty()) {
                response = Message.noInternetMessage()
            }

            messageDao.addMessages(conversation, response)

            SuspiciousTrafficChannel.notify(context, domain.hashCode()) {
                newSuspiciousTraffic(domain, conversation, response)
            }
        }
    }

    private fun messageFromNotification(domain: String, conversation: Long, context: Context, intent: Intent) {
        val inputtedText = SuspiciousTrafficChannel.recoverSubmittedText(intent) ?: return
        val question = Message(inputtedText, Sender.USER)

        Log.i(TAG, "got chat ($domain) response: ($inputtedText) at conversation $conversation")
        SuspiciousTrafficChannel
            .addMessagesToNotification(
                context,
                domain,
                conversation,
                listOf(question),
            )

        CoroutineScope(Dispatchers.IO + supervisor).launch {
            val messages = messageDao
                .getMessages(conversation)
                .first()

            messageDao.addMessage(conversation, question)

            // get the response
            val chat = messages
                .toMutableList()
                .apply { add(question) }

            var response = chatApi!!.answer(chat)
            if (response.isNullOrEmpty())
                response = Message.noInternetMessage()

            // add the response
            messageDao.addMessages(conversation, response)

            Log.d(TAG, "Updating notification for chat $domain")
            SuspiciousTrafficChannel
                .addMessagesToNotification(
                    context,
                    domain,
                    conversation,
                    response,
                )
        }
    }

    private fun addToWhitelist(domain: String, context: Context) {
        Log.i(TAG, "Trying to add $domain to whitelist")
        SuspiciousTrafficChannel.removeNotification(
            context,
            domain.hashCode()
        )

        runBlocking {
            elaSettingsStore.updateData {
                it.withAddedInWhitelist(domain)
            }
        }

        ElaVpnService.sendRestart(context)
    }

    private fun dismissNotification(domain: String, context: Context) {
        Log.d(TAG, "dismiss chat ($domain)")
        SuspiciousTrafficChannel.removeNotification(
            context,
            domain.hashCode()
        )
    }

    companion object {
        fun createChat(context: Context, domain: String, conversation: Long, reason: MaliciousDomainClassifier.Result) {
            val intent = Intent(context, SuspiciousNotification::class.java).apply {
                putExtra("action", "CREATE_CHAT")
                putExtra("domain", domain)
                putExtra("conversation", conversation.toString())
                putExtra("reason", reason.toString())
            }

            context.sendBroadcast(intent)
        }

        fun broadcast(context: Context, domain: String, conversation: Long, command: SuspiciousCommand): PendingIntent {
            return PendingIntent
                .getBroadcast(
                    context,
                    ":$command:$domain:".hashCode(),
                    Intent(context, SuspiciousNotification::class.java).apply {
                        putExtra("action", command.toString())
                        putExtra("domain", domain)
                        putExtra("conversation", conversation.toString())
                    },
                    getFlags(command),
                )
        }

        private fun getFlags(command: SuspiciousCommand): Int {
            return when (command) {
                SuspiciousCommand.SUBMIT_FROM_NOTIFICATION -> PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
                SuspiciousCommand.ADD_TO_WHITELIST_FROM_NOTIFICATION -> PendingIntent.FLAG_IMMUTABLE
                SuspiciousCommand.DISMISS_NOTIFICATION -> PendingIntent.FLAG_IMMUTABLE
                SuspiciousCommand.DISMISS_BUBBLE -> PendingIntent.FLAG_IMMUTABLE
            }
        }
    }
}
