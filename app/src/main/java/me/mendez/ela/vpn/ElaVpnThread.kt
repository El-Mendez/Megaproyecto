package me.mendez.ela.vpn

import android.net.VpnService
import android.net.VpnService.Builder
import android.os.ParcelFileDescriptor
import android.util.Log
import me.mendez.ela.BuildConfig
import me.mendez.ela.persistence.settings.ElaSettings
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

private data class RunningContext(
    val vpnInterface: ParcelFileDescriptor,
    val producer: Thread,
    val consumers: ExecutorService,
    val filteringService: DnsFilter,
)

private const val TAG = "ELA_VPN"

class ElaVpnThread(private val service: ElaVpnService) {
    private val running = AtomicBoolean(false)
    private var runningContext: RunningContext? = null
    private val lock = Any()

    private fun startNoLock(builder: Builder, settings: ElaSettings) {
        if (runningContext != null) {
            Log.w(TAG, "could not restart execution because it was already running.")
            return
        }

        running.set(true)

        val filteringService = DnsFilter(service, settings.copy())
        val consumers = Executors.newFixedThreadPool(2)
        val vpnInterface = startVpnInterface(builder)!!
        val producer = producerThread(vpnInterface, consumers, filteringService)

        producer.start()
        runningContext = RunningContext(vpnInterface, producer, consumers, filteringService)
    }

    fun start(builder: VpnService.Builder, settings: ElaSettings) = synchronized(lock) {
        startNoLock(builder, settings)
    }

    fun restart(builder: Builder, settings: ElaSettings) = synchronized(lock) {
        running.set(false)

        if (runningContext == null) {
            Log.w(TAG, "trying to restart when the service was off. Defaulting to start")
            startNoLock(builder, settings) // No lock just to avoid a deadlock
            return
        }

        // stop old jobs
        endProducer(runningContext!!.producer)
        endConsumers(runningContext!!.consumers)
        closeVpnInterface(runningContext!!.vpnInterface)


        // start new jobs
        running.set(true)

        // we can recycle the old filtering model
        val filteringService = runningContext!!.filteringService
        filteringService.recycle(settings)

        val consumers = Executors.newFixedThreadPool(2)
        val vpnInterface = startVpnInterface(builder)!!
        val producer = producerThread(vpnInterface, consumers, filteringService)

        runningContext = RunningContext(vpnInterface, producer, consumers, filteringService)
    }

    fun stop() = synchronized(lock) {
        if (runningContext == null) {
            Log.w(TAG, "vpn was already stopped")
            return
        }

        Log.d(TAG, "Stopping thread")
        running.set(false)

        endProducer(runningContext!!.producer)
        endConsumers(runningContext!!.consumers)
        closeVpnInterface(runningContext!!.vpnInterface)
        runningContext!!.filteringService.destroy()

        runningContext = null
    }

    private fun producerTask(vpnInterface: ParcelFileDescriptor, consumers: ExecutorService, filter: DnsFilter) {
        val input = FileInputStream(vpnInterface.fileDescriptor)
        val output = FileOutputStream(vpnInterface.fileDescriptor)

        while (running.get()) {
            val buffer = ByteBufferPool.poll()
            try {
                val size = input.read(buffer.array())

                if (size <= 0)
                    Log.i(TAG, "got empty packet!")

                consumers.submit {
                    filter.filter(buffer, output)
                    ByteBufferPool.put(buffer)
                }
            } catch (e: Exception) {
                Log.e(TAG, "vpn thread exception $e")
                buffer.clear()
                ByteBufferPool.put(buffer)
            }
        }
    }

    private fun producerThread(
        vpnInterface: ParcelFileDescriptor,
        consumers: ExecutorService,
        filter: DnsFilter
    ): Thread {
        Log.d(TAG, "creating vpn producer thread")

        val producer = Thread {
            producerTask(vpnInterface, consumers, filter)
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

    private fun endConsumers(consumers: ExecutorService) {
        consumers.shutdown()
        val finished = consumers.awaitTermination(2000, TimeUnit.MILLISECONDS)
        if (!finished)
            consumers.shutdownNow()
    }
}
