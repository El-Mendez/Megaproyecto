package me.mendez.ela.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.util.Log
import androidx.datastore.core.DataStore
import kotlinx.coroutines.*
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.settings.ElaSettingsModule

class ElaVpn : VpnService() {
    enum class Commands {
        START, STOP, RESTART
    }

    val TAG = "VPN"

    val elaSettingsStore: DataStore<ElaSettings> = ElaSettingsModule.provideElaSettingsStore(this)
    private val globalJob = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + globalJob)
    private var job: Job? = null

    companion object {
        fun createChannel(context: Context) = VpnNotificationChannel.createChannel(context)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "got notification ${intent?.action}")
        when (intent?.action) {
            Commands.START.toString() -> start()
            Commands.RESTART.toString() -> restart()
            Commands.STOP.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun restart() {
        Log.i(TAG, "restarting vpn")
        job?.cancel()
        job = scope.launch {
            delay(100)
            Log.i(TAG, "COLLECTING")
            elaSettingsStore.data.collect {
                Log.i(TAG, it.toString())
            }
        }
    }

    private fun start() {
        startForeground(
            VpnNotificationChannel.FOREGROUND_ID,
            VpnNotificationChannel.runningNotification(this)
        )

        Log.i(TAG, "starting vpn")

        job?.cancel()
        job = scope.launch {
            delay(100)
            Log.i(TAG, "COLLECTING")
            elaSettingsStore.data.collect {
                Log.i(TAG, it.toString())
            }
        }
    }

    private fun stop() {
        Log.i(TAG, "stopped vpn")
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        globalJob.cancel()
    }
}