package me.mendez.ela.vpn

import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import me.mendez.ela.BuildConfig
import me.mendez.ela.persistence.settings.ElaSettings
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

private data class ThreadContainer(
    val vpnInterface: ParcelFileDescriptor,
    val thread: Thread
)

private const val TAG = "ELA_VPN"

class ElaVpnThread(private val service: ElaVpnService) {

    private val shouldStop = AtomicBoolean(false)
    private val dnsFilter = DnsFilter(service)

    private lateinit var consumers: ExecutorService
    private var threadContainer: ThreadContainer? = null

    @Synchronized
    fun start(builder: VpnService.Builder, settings: ElaSettings) {
        if (threadContainer != null) {
            Log.e(TAG, "could not start vpn thread because it wasn't closed properly")
            return
        }

        shouldStop.set(false)
        dnsFilter.elaSettings = settings.copy()
        consumers = Executors.newFixedThreadPool(2)
        val vpnInterface = startVpnInterface(builder)!!
        val thread = producerThread(vpnInterface)

        thread.start()
        threadContainer = ThreadContainer(
            vpnInterface,
            thread
        )
    }

    @Synchronized
    fun stop() {
        Log.d(TAG, "Stopping thread")
        shouldStop.set(true)

        val threadContainer = threadContainer ?: return
        endProducer(threadContainer.thread)
        consumers.shutdownNow()
        closeVpnInterface(threadContainer.vpnInterface)
        this.threadContainer = null
    }

    @Synchronized
    fun halt() {
        stop()
    }

    private fun producerTask(vpnInterface: ParcelFileDescriptor, consumer: ExecutorService, filter: DnsFilter) {
        val input = FileInputStream(vpnInterface.fileDescriptor)
        val output = FileOutputStream(vpnInterface.fileDescriptor)

        val buffer = ByteBufferPool.poll()

        while (!shouldStop.get()) {
            try {
                val size = input.read(buffer.array())

                if (size <= 0) {
                    Log.w(TAG, "got empty packet!")
                }

                consumers.submit {
                    dnsFilter.filter(buffer, output)
                }
            } catch (e: Exception) {
                Log.e(TAG, "vpn thread exception $e")
            } finally {
                buffer.clear()
            }
        }
    }

    private fun producerThread(vpnInterface: ParcelFileDescriptor): Thread {
        Log.d(TAG, "creating vpn producer thread")

        val producer = Thread {
            producerTask(vpnInterface, consumers, dnsFilter)
        }

        producer.setUncaughtExceptionHandler { _, e ->
            service.errorStop(e.toString())
        }
        return producer
    }

    private fun endProducer(thread: Thread) {
        thread.interrupt()

        try {
            thread.join(2000)
            Log.d(TAG, "joined old vpn thread")
        } catch (e: Exception) {
            Log.e(TAG, "could not join thread $e")
        }
    }

    private fun startVpnInterface(builder: VpnService.Builder): ParcelFileDescriptor? {
        Log.d(TAG, "starting vpn interface")
        return builder
            .addRoute("10.255.0.0", 29)
            .addAddress("10.255.0.1", 29)
            .addDnsServer("10.255.0.2")
            .addDisallowedApplication(BuildConfig.APPLICATION_ID)
            .setBlocking(true)
            .setSession("ElaVPN")
            .establish()
    }

    private fun closeVpnInterface(vpnInterface: ParcelFileDescriptor) {
        Log.d(TAG, "closing vpn interface")
        try {
            vpnInterface.close()
            Log.d(TAG, "successfully closed vpn interface")
        } catch (e: Exception) {
            Log.e(TAG, "could not close vpn interface $e")
        }
    }
}
