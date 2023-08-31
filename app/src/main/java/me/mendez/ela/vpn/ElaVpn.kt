package me.mendez.ela.vpn

import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.datastore.core.DataStore
import kotlinx.coroutines.*
import me.mendez.ela.settings.ElaSettings
import me.mendez.ela.settings.ElaSettingsModule
import java.io.FileInputStream
import java.net.DatagramSocket
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.atomic.AtomicBoolean

class ElaVpn : VpnService() {
    enum class Commands {
        START, STOP, RESTART
    }

    val TAG = "VPN"

    val elaSettingsStore: DataStore<ElaSettings> = ElaSettingsModule.provideElaSettingsStore(this)
    private val globalJob = SupervisorJob()
    private var scope = CoroutineScope(Dispatchers.IO + globalJob)
    private var job: Job? = null

    private var vpnInterface: ParcelFileDescriptor? = null
    private var thread: Thread? = null

    private var shouldStop = AtomicBoolean(false)

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
        Log.d(TAG, "got notification ${intent?.action}")
        when (intent?.action) {
            Commands.START.toString() -> start()
            Commands.RESTART.toString() -> restart()
            Commands.STOP.toString() -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun restart() {
        stopThread()
        startThread()
    }

    private fun start() {
        Log.i(TAG, "starting vpn")
        startForeground(
            VpnNotificationChannel.FOREGROUND_ID,
            VpnNotificationChannel.runningNotification(this)
        )
    }

    private fun stop() {
        Log.i(TAG, "stopped vpn")
        stopThread()
        stopSelf()
    }

    private fun stopThread() {
        shouldStop.set(true)
        thread?.interrupt()
        try {
            thread?.join(2000)
        } catch (e: Exception) {
            Log.e(TAG, "could not join thread $e")
        }

        try {
            vpnInterface?.close()
        } catch (e: Exception) {
            Log.i(TAG, "could not close file descriptor $e")
        }


        try {
            freeInterface?.close()
        } catch (e: Exception) {
            Log.i(TAG, "could not close file descriptor $e")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        globalJob.cancel()
    }

    private fun startThread() {
        shouldStop.set(false)
        openInterface()
        thread = Thread {
            val buffer = ByteBuffer.allocate(32767)
            while (!shouldStop.get()) {
                try {
                    val stream = FileInputStream(vpnInterface.fileDescriptor)
                    val size = stream.read(buffer.array())

                    if (size <= 0) continue

                    forwardPacket()

                } catch (e: Exception) {
                    Log.e(TAG, "at thread $e")
                }
            }
        }
    }

    private fun forwardPacket(buffer: ByteBuffer) {
        try {
            val datagramSocket = DatagramSocket()
            protect(datagramSocket)
            datagramSocket.send()
        } catch (e: Exception) {

        }
    }

    private fun openInterface() {
        val builder = Builder()
        builder.addAddress("192.168.56.55", 32) // TODO
        builder.addRoute("0.0.0.0", 0)

        vpnInterface = builder.setSession("ElaVPN")
            .establish()
    }

}