package me.mendez.ela.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import me.mendez.ela.BuildConfig
import org.pcap4j.packet.IpPacket
import org.pcap4j.packet.IpSelector
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean

private data class ThreadContainer(
    val vpnInterface: ParcelFileDescriptor,
    val thread: Thread
)

private const val TAG = "ELA_VPN"

class ElaVpnThread(private val service: ElaVpn) {

    private var shouldStop = AtomicBoolean(false)
    private var threadContainer: ThreadContainer? = null

    @Synchronized
    fun start(builder: VpnService.Builder) {
        if (threadContainer != null) {
            Log.e(TAG, "could not start vpn thread because it wasn't closed properly")
            return
        }

        shouldStop.set(false)
        val vpnInterface = startVpnInterface(builder)!!
        val thread = threadStart(vpnInterface)

        thread.setUncaughtExceptionHandler { _, e -> service.errorStop(e.toString()) }
        thread.start()
        threadContainer = ThreadContainer(
            vpnInterface,
            thread
        )
    }

    private fun threadStart(vpnInterface: ParcelFileDescriptor): Thread {
        Log.d(TAG, "starting vpn thread")
        return Thread {
            val input = FileInputStream(vpnInterface.fileDescriptor)
            val output = FileOutputStream(vpnInterface.fileDescriptor)

            val buffer = ByteBuffer.allocate(32767)
            while (!shouldStop.get()) {
                try {
                    val size = input.read(buffer.array())

                    if (size <= 0) continue // empty packet

                    val packet = IpSelector.newPacket(buffer.array(), 0, size) as IpPacket
                    Log.d(TAG, "got $packet")
                } catch (e: Exception) {
                    Log.e(TAG, "vpn thread exception $e")
                } finally {
                    buffer.clear()
                }
            }
        }
    }

    private fun startVpnInterface(builder: VpnService.Builder): ParcelFileDescriptor? {
        Log.d(TAG, "starting vpn interface")
        return builder
            .addAddress("10.0.2.0", 32) // TODO
            .addRoute("0.0.0.0", 0)
            .addDisallowedApplication(BuildConfig.APPLICATION_ID)
            .setBlocking(true)
            .setSession("ElaVPN")
            .establish()
    }

    @Synchronized
    fun stop() {
        Log.d(TAG, "Stopping thread")
        shouldStop.set(true)

        val threadContainer = threadContainer ?: return
        closeThreadOnly(threadContainer.thread)
        closeInterfaceOnly(threadContainer.vpnInterface)
        this.threadContainer = null
    }

    private fun closeThreadOnly(thread: Thread) {
        thread.interrupt()

        try {
            thread.join(2000)
            Log.d(TAG, "joined old vpn thread")
        } catch (e: Exception) {
            Log.e(TAG, "could not join thread $e")
        }
    }

    private fun closeInterfaceOnly(vpnInterface: ParcelFileDescriptor) {
        Log.d(TAG, "closing vpn interface")
        try {
            vpnInterface.close()
            Log.d(TAG, "successfully closed vpn interface")
        } catch (e: Exception) {
            Log.e(TAG, "could not close vpn interface $e")
        }
    }
}