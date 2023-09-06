package me.mendez.ela.vpn

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import kotlinx.coroutines.*
import me.mendez.ela.notifications.VpnChannel
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.settings.ElaSettingsModule

class ElaVpn : VpnService() {
    enum class Commands {
        START, STOP, RESTART
    }

    val TAG = "VPN"

    private val elaSettingsStore: DataStore<ElaSettings> = ElaSettingsModule.provideElaSettingsStore(this)

    private val vpnThread = ElaVpnThread(this)

    private fun getCurrentSettings(): ElaSettings {
        lateinit var settings: ElaSettings
        runBlocking {
            delay(200)
            elaSettingsStore.data.collect {
                settings = it
            }
        }

        return settings
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
    }

    private fun restart() {
        Log.i(TAG, "restarting vpn")
        vpnThread.stop()
        vpnThread.start(Builder())
    }

    private fun stop() {
        Log.i(TAG, "stopped vpn")
        vpnThread.stop()
        stopSelf()
    }

    private fun disableVpnInSettings() {
        runBlocking {
            elaSettingsStore.updateData {
                it.copy(vpnRunning = false)
            }
        }
    }

    private fun errorStop(reason: String) {
        Log.e(TAG, reason)
        disableVpnInSettings()

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