package me.mendez.ela.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import me.mendez.ela.BuildConfig
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.util.concurrent.atomic.AtomicBoolean

private data class ThreadContainer(
//    val bypassTunnel: DatagramChannel,
    val vpnInterface: ParcelFileDescriptor,
    val thread: Thread
)

private const val TAG = "ELA_VPN"

class ElaVpnThread(val service: VpnService) {

    private var shouldStop = AtomicBoolean(false)
    private var threadContainer: ThreadContainer? = null

    @Synchronized
    fun start(builder: VpnService.Builder) {
        if (threadContainer != null) {
            Log.e(TAG, "could not start vpn thread because it wasn't closed properly")
            return
        }

        shouldStop.set(false)
//        val tunnel = startBypassTunnel()
        val vpnInterface = startVpnInterface(builder)!!
//        val thread = threadStart(vpnInterface, tunnel)
        val thread = threadStart(vpnInterface)

        thread.start()
        threadContainer = ThreadContainer(
//            tunnel,
            vpnInterface,
            thread
        )
    }

    private fun threadStart(vpnInterface: ParcelFileDescriptor): Thread {
//        private fun threadStart(vpnInterface: ParcelFileDescriptor, bypassTunnel: DatagramChannel): Thread {
        Log.d(TAG, "starting vpn thread")
        return Thread {
            val buffer = ByteBuffer.allocate(32767)
            while (!shouldStop.get()) {
                try {
                    val stream = FileInputStream(vpnInterface.fileDescriptor)
                    val size = stream.read(buffer.array())

                    if (size <= 0) continue // empty packet

                    Log.d(TAG, "got packet $buffer")
//                    bypassTunnel.write(buffer)
                } catch (e: Exception) {
                    Log.e(TAG, "vpn thread exception $e")
                } finally {
                    buffer.clear()
                }
            }
        }
    }

//    private fun startBypassTunnel(): DatagramChannel {
//        Log.d(TAG, "starting bypass tunnel")
//        val server = InetSocketAddress("127.0.0.0.1", 5555) // TODO
//
//        val tunnel = DatagramChannel.open()
//        tunnel.configureBlocking(false)
//        return tunnel
//    }

    private fun startVpnInterface(builder: VpnService.Builder): ParcelFileDescriptor? {
        Log.d(TAG, "starting vpn interface")
        builder.addAddress("10.0.2.0", 32) // TODO
        builder.addRoute("0.0.0.0", 0)
        builder.addDisallowedApplication(BuildConfig.APPLICATION_ID)

        return builder.setSession("ElaVPN")
            .establish()
    }

    @Synchronized
    fun stop() {
        Log.d(TAG, "Stopping thread")
        shouldStop.set(true)

        val threadContainer = threadContainer ?: return
//        closeBypassTunnel(threadContainer.bypassTunnel)
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

    private fun closeBypassTunnel(tunnel: DatagramChannel) {
        tunnel.close()
    }
}