package me.mendez.ela.vpn

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.VpnService
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import kotlinx.coroutines.*
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.settings.ElaSettingsModule

class ElaVpn : VpnService() {
    enum class Commands {
        START, STOP, RESTART
    }

    val TAG = "VPN"

    private val elaSettingsStore: DataStore<ElaSettings> = ElaSettingsModule.provideElaSettingsStore(this)
    private val globalJob = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + globalJob)
    private var job: Job? = null

    private val vpnThread = ElaVpnThread(this)


    companion object {
        fun createChannel(context: Context) = VpnNotificationChannel.createChannel(context)
    }

    fun updateStore(callback: (ElaSettings) -> ElaSettings) {
        job?.cancel()
        job = scope.launch {
            elaSettingsStore.updateData(callback)
        }
    }

    fun collectStore(callback: (ElaSettings) -> Unit) {
        job?.cancel()
        job = scope.launch {
            delay(100)
            elaSettingsStore.data.collect(callback)
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

    private fun restart() {
        Log.i(TAG, "restarting vpn")
        vpnThread.stop()
        vpnThread.start(Builder())
    }

    private fun start() {
        Log.i(TAG, "starting vpn")

        val error = prepare(this)
        if (error != null) {
            errorStop(error.toString())
            return
        }

        startForeground(
            VpnNotificationChannel.FOREGROUND_ID,
            VpnNotificationChannel.runningNotification(this)
        )
        vpnThread.start(Builder())
    }

    private fun errorStop(reason: String) {
        updateStore { old -> old.copy(vpnRunning = false) }
        Log.e(TAG, reason)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@ElaVpn,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                notify(
                    VpnNotificationChannel.ERROR_ID,
                    VpnNotificationChannel.errorNotification(this@ElaVpn)
                )
            }
        }
        stop()
    }

    private fun stop() {
        Log.i(TAG, "stopped vpn")
        vpnThread.stop()

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        globalJob.cancel()
    }
}