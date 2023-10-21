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
import me.mendez.ela.ml.MaliciousDomainClassifier
import me.mendez.ela.ml.prompt
import java.util.*
import javax.inject.Inject

private const val TAG = "ELA_NOTIFICATION_SERVICE"


enum class SuspiciousCommand {
    SUBMIT_FROM_NOTIFICATION,
    ADD_TO_WHITELIST_FROM_NOTIFICATION,
    DISMISS_NOTIFICATION, DISMISS_BUBBLE,
}

fun SuspiciousCommand.broadcast(context: Context, domain: String): PendingIntent {
    return SuspiciousNotification.broadcast(context, domain, this)
}


@AndroidEntryPoint
class SuspiciousNotification : BroadcastReceiver() {
    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>

    @Inject
    lateinit var chatApi: ChatApi

    @Inject
    lateinit var messageDao: MessageDao
    private val supervisor = SupervisorJob()

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val domain = intent.getStringExtra("domain") ?: return
        val actionString = intent.getStringExtra("action") ?: return
        Log.i(TAG, "new action: $actionString")

        if (actionString == "CREATE_CHAT") {
            initializeChat(domain, context, intent)
            return
        }

        val action = SuspiciousCommand.valueOf(actionString)
        when (action) {
            SuspiciousCommand.SUBMIT_FROM_NOTIFICATION -> messageFromNotification(domain, context, intent)

            SuspiciousCommand.ADD_TO_WHITELIST_FROM_NOTIFICATION -> addToWhitelist(domain, context)

            SuspiciousCommand.DISMISS_NOTIFICATION, SuspiciousCommand.DISMISS_BUBBLE ->
                dismissNotification(domain, context)
        }
    }

    private fun initializeChat(domain: String, context: Context, intent: Intent) {
        val reasonString = intent.getStringExtra("reason") ?: return
        val reason = MaliciousDomainClassifier.Result.valueOf(reasonString)

        val text = runBlocking {
            val response = chatApi.answer(
                listOf(Message(reason.prompt(), true, Date()))
            ).firstOrNull() ?: Message(
                "Vaya, parece que no tienes internet. No puedo darte tips de ciberseguridad hasta que vuelvas a conectarte",
                false,
                Date()
            )
            messageDao.addMessage(domain, response)

            response.content
        }

        SuspiciousTrafficChannel.notify(context, domain.hashCode()) {
            newSuspiciousTraffic(domain, text)
        }
    }

    private fun messageFromNotification(domain: String, context: Context, intent: Intent) {
        val inputtedText = SuspiciousTrafficChannel.recoverSubmittedText(intent) ?: return
        val question = Message(inputtedText, true)

        SuspiciousTrafficChannel
            .addMessageToChat(
                context,
                domain,
                inputtedText,
                true,
            )

        CoroutineScope(Dispatchers.IO + supervisor).launch {
            val messages = messageDao
                .getMessages(domain)
                .first()

            messageDao.addMessage(domain, question)

            val response = try {
                val conversation = messages
                    .toMutableList()
                    .apply { add(question) }
                val response = chatApi.answer(conversation)
                messageDao.addMessage(domain, response.last())
                response.last()
            } catch (e: Exception) {
                Message("parece que no tienes internet", false)
            }

            SuspiciousTrafficChannel
                .addMessageToChat(
                    context,
                    domain,
                    response.content,
                    false
                )
        }
    }

    private fun addToWhitelist(domain: String, context: Context) {
        SuspiciousTrafficChannel.removeNotification(
            context,
            domain.hashCode()
        )
        CoroutineScope(Dispatchers.IO + supervisor).launch {
            elaSettingsStore.updateData {
                it.withAddedInWhitelist(domain)
            }
        }
    }

    private fun dismissNotification(domain: String, context: Context) {
        SuspiciousTrafficChannel.removeNotification(
            context,
            domain.hashCode()
        )

        CoroutineScope(Dispatchers.IO + supervisor).launch {
            messageDao.deleteChat(domain)
        }
    }

    companion object {
        fun createChat(context: Context, domain: String, reason: MaliciousDomainClassifier.Result) {
            val intent = Intent(context, SuspiciousNotification::class.java).apply {
                putExtra("action", "CREATE_CHAT")
                putExtra("domain", domain)
                putExtra("reason", reason.toString())
            }

            context.sendBroadcast(intent)
        }

        fun broadcast(context: Context, domain: String, command: SuspiciousCommand): PendingIntent {
            return PendingIntent
                .getBroadcast(
                    context,
                    ":$command:$domain:".hashCode(),
                    Intent(context, SuspiciousNotification::class.java).apply {
                        putExtra("action", command.toString())
                        putExtra("domain", domain)
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
