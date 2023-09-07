package me.mendez.ela.vpn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import me.mendez.ela.notifications.VpnChannel
import me.mendez.ela.settings.ElaSettings
import javax.inject.Inject

private const val TAG = "VPN"

@AndroidEntryPoint
class ElaVpn : VpnService() {
    private enum class Commands {
        START, STOP, RESTART
    }

    @Inject
    lateinit var elaSettingsStore: DataStore<ElaSettings>
    private val vpnThread = ElaVpnThread(this)

    companion object {
        @JvmStatic
        suspend fun showRunning(running: Boolean, store: DataStore<ElaSettings>) {
            store.updateData { it.copy(vpnRunning = running) }
        }

        @JvmStatic
        fun sendStart(context: Context) {
            Intent(context, ElaVpn::class.java).also { intent ->
                intent.action = Commands.START.toString()
                context.startService(intent)
            }
        }

        @JvmStatic
        fun sendStop(context: Context) {
            Intent(context, ElaVpn::class.java).also { intent ->
                intent.action = Commands.STOP.toString()
                context.startService(intent)
            }
        }

        @JvmStatic
        fun sendRestart(context: Context) {
            Intent(context, ElaVpn::class.java).also { intent ->
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
            VpnChannel.runningNotification(this)
        )

        vpnThread.start(Builder())

        runBlocking {
            showRunning(true, elaSettingsStore)
        }
    }

    private fun restart() {
        Log.i(TAG, "restarting vpn")
        vpnThread.stop()
        vpnThread.start(Builder())

        runBlocking {
            showRunning(true, elaSettingsStore)
        }
    }

    private fun stop() {
        Log.i(TAG, "stopped vpn")
        vpnThread.stop()

        runBlocking {
            showRunning(false, elaSettingsStore)
        }

        stopSelf()
    }

    private fun errorStop(reason: String) {
        Log.e(TAG, reason)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@ElaVpn,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(
                    VpnChannel.ERROR_ID,
                    VpnChannel.errorNotification(this@ElaVpn),
                )
            }
        }
        stop()
    }
}