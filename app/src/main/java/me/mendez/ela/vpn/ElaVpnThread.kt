package me.mendez.ela.vpn

import android.net.VpnService
import android.net.VpnService.Builder
import android.os.ParcelFileDescriptor
import android.util.Log
import me.mendez.ela.BuildConfig
import me.mendez.ela.persistence.database.blocks.BlockDao
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

class ElaVpnThread(private val service: ElaVpnService, private val blockDao: BlockDao) {
    private val running = AtomicBoolean(false)
    private var runningContext: RunningContext? = null
    private val lock = Any()

    fun start(builder: VpnService.Builder, settings: ElaSettings) = synchronized(lock) {
        if (runningContext != null) {
            Log.w(TAG, "could not restart execution because it was already running.")
            return
        }

        running.set(true)

        val filteringService = DnsFilter(service, settings.copy(), blockDao)
        val consumers = Executors.newFixedThreadPool(2)
        val vpnInterface = startVpnInterface(builder)!!
        val producer = producerThread(vpnInterface, consumers, filteringService)

        producer.start()
        runningContext = RunningContext(vpnInterface, producer, consumers, filteringService)
    }

    fun restart(builder: Builder, settings: ElaSettings) = synchronized(lock) {
        running.set(false)

        if (runningContext == null) {
            Log.w(TAG, "trying to restart when the service was off. Do nothing")
            return
        }

        // stop old jobs
        endConsumers(runningContext!!.consumers)
        endProducer(runningContext!!.producer)
        closeVpnInterface(runningContext!!.vpnInterface)

        // start new jobs
        running.set(true)

        // we can recycle the old filtering model
        val filteringService = runningContext!!.filteringService
        filteringService.recycle(settings)

        val consumers = Executors.newFixedThreadPool(2)
        val vpnInterface = startVpnInterface(builder)!!
        val producer = producerThread(vpnInterface, consumers, filteringService)

        producer.start()
        runningContext = RunningContext(vpnInterface, producer, consumers, filteringService)
    }

    fun stop() = synchronized(lock) {
        running.set(false)

        if (runningContext == null) {
            Log.w(TAG, "vpn was already stopped")
            return
        }

        Log.d(TAG, "Stopping producers")
        endProducer(runningContext!!.producer)
        Log.d(TAG, "Stopping consumer")
        endConsumers(runningContext!!.consumers)
        Log.d(TAG, "Stopping vpnInterface")
        closeVpnInterface(runningContext!!.vpnInterface)
        Log.d(TAG, "destroying filtering service")
        runningContext!!.filteringService.destroy()
        Log.d(TAG, "destroying done destroying")

        runningContext = null
    }

    private fun producerTask(vpnInterface: ParcelFileDescriptor, consumers: ExecutorService, filter: DnsFilter) {
        val input = FileInputStream(vpnInterface.fileDescriptor)
        val output = FileOutputStream(vpnInterface.fileDescriptor)

        try {
            while (running.get()) {
                val buffer = ByteBufferPool.poll()
                val size = input.read(buffer.array())

                if (size <= 0)
                    Log.i(TAG, "got empty packet!")

                consumers.submit {
                    if (running.get()) {
                        filter.filter(buffer, output)
                    }
                    ByteBufferPool.put(buffer)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "error on vpn. Stopping. $e")
            running.set(false)
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
            thread.join(5000)
            Log.d(TAG, "joined old vpn thread")
        } catch (e: Exception) {
            Log.e(TAG, "could not join thread $e")
        }
    }

    private fun startVpnInterface(builder: VpnService.Builder): ParcelFileDescriptor? {
        Log.d(TAG, "starting vpn interface")
        return builder
            .addRoute("192.168.255.0", 29)
            .addAddress("192.168.255.1", 29)
            .addDnsServer("192.168.255.2")
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
        val finished = consumers.awaitTermination(5000, TimeUnit.MILLISECONDS)
        if (!finished)
            consumers.shutdownNow()
    }
}
