package me.mendez.ela.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import me.mendez.ela.notifications.SuspiciousTrafficChannel
import me.mendez.ela.persistence.settings.ElaSettings
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
    private val supervisor = SupervisorJob()


    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val domain = intent.getStringExtra("domain") ?: return
        val action = SuspiciousCommand.valueOf(intent.getStringExtra("action") ?: return)

        Log.i(TAG, "new action: $action")

        when (action) {
            SuspiciousCommand.SUBMIT_FROM_NOTIFICATION -> messageFromNotification(domain, context, intent)

            SuspiciousCommand.ADD_TO_WHITELIST_FROM_NOTIFICATION -> addToWhitelist(domain, context)

            SuspiciousCommand.DISMISS_NOTIFICATION, SuspiciousCommand.DISMISS_BUBBLE ->
                dismissNotification(domain, context)
        }
    }

    private fun messageFromNotification(domain: String, context: Context, intent: Intent) {
        val inputtedText = SuspiciousTrafficChannel.recoverSubmittedText(intent) ?: return

        SuspiciousTrafficChannel
            .addMessageToChat(
                context,
                domain,
                inputtedText,
                true,
            )
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
    }

    companion object {
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
