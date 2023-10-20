package me.mendez.ela.vpn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import me.mendez.ela.notifications.VpnChannel
import me.mendez.ela.persistence.database.blocks.BlockDao
import me.mendez.ela.persistence.settings.ElaSettings
import javax.inject.Inject

private const val TAG = "VPN"

@AndroidEntryPoint
class ElaVpnService : VpnService() {
    private enum class Commands {
        START, STOP, RESTART
    }

    @Inject
    lateinit var blockDao: BlockDao

    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>
    private var vpnThread: ElaVpnThread? = null

    companion object {
        @JvmStatic
        suspend fun showRunning(running: Boolean, store: DataStore<ElaSettings>) {
            store.updateData { it.copy(vpnRunning = running) }
        }

        @JvmStatic
        fun sendStart(context: Context) {
            Intent(context, ElaVpnService::class.java).also { intent ->
                intent.action = Commands.START.toString()
                context.startService(intent)
            }
        }

        @JvmStatic
        fun sendStop(context: Context) {
            Intent(context, ElaVpnService::class.java).also { intent ->
                intent.action = Commands.STOP.toString()
                context.startService(intent)
            }
        }

        @JvmStatic
        fun sendRestart(context: Context) {
            Intent(context, ElaVpnService::class.java).also { intent ->
                intent.action = Commands.RESTART.toString()
                context.startService(intent)
            }
        }

        @JvmStatic
        fun haveAllPermissions(context: Context): Boolean {
            return (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED) && prepare(context) == null

        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (vpnThread == null) {
            vpnThread = ElaVpnThread(this, blockDao)
        }

        when (intent?.action) {
            Commands.START.toString() -> start()
            Commands.RESTART.toString() -> restart()
            Commands.STOP.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        Log.i(TAG, "starting vpn")

        val error = prepare(this)
        if (error != null) {
            errorStop(error.toString())
            return
        }

        startForeground(
            VpnChannel.FOREGROUND_ID,
            VpnChannel
                .createNotification(this)
                .runningNotification()
        )

        runBlocking {
            vpnThread!!.start(
                Builder(),
                elaSettingsStore.data.first()
            )

            showRunning(true, elaSettingsStore)
        }
    }

    private fun restart() {
        Log.i(TAG, "restarting vpn")

        runBlocking {
            vpnThread!!.restart(
                Builder(),
                elaSettingsStore.data.first()
            )

            showRunning(true, elaSettingsStore)
        }
    }

    private fun stop() {
        Log.i(TAG, "stopped vpn")
        vpnThread!!.stop()

        runBlocking {
            showRunning(false, elaSettingsStore)
        }

        stopSelf()
    }

    fun errorStop(reason: String) {
        Log.e(TAG, reason)

        VpnChannel.notify(
            this@ElaVpnService,
            VpnChannel.ERROR_ID,
        ) {
            errorNotification()
        }

        try {
            vpnThread!!.stop()
        } catch (e: Exception) {
            Log.e(TAG, "could not force stop vpn")
        }
        stop()
    }
}
