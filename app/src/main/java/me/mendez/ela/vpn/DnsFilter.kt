package me.mendez.ela.vpn

import android.util.Log
import kotlinx.coroutines.runBlocking
import me.mendez.ela.ml.MaliciousDomainClassifier
import me.mendez.ela.persistence.database.blocks.Block
import me.mendez.ela.persistence.database.blocks.BlockDao
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.services.SuspiciousNotification
import org.pcap4j.packet.*
import org.xbill.DNS.*
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.nio.ByteBuffer
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ELA_DNS_FILTER"

private sealed interface FilterStatus {
    data class Allowed(val date: Long) : FilterStatus
    data class NotAllowed(val date: Long) : FilterStatus
}

private sealed interface ForwardAction {
    data object FORWARD : ForwardAction
    data object DENY : ForwardAction
    class DenyWithNotification(val reason: MaliciousDomainClassifier.Result) : ForwardAction
}

class DnsFilter(
    private val service: ElaVpnService,
    private var elaSettings: ElaSettings,
    private var blockDao: BlockDao,
) {
    private val domainClassifier = MaliciousDomainClassifier(service)
    private val upstreamDnsServer = Inet4Address.getByName("1.1.1.2")
    private val cache = ConcurrentHashMap<String, FilterStatus>()

    init {
        domainClassifier.load()
    }

    fun filter(request: ByteBuffer, output: FileOutputStream) {
        val (ipPacket, udpPacket, dnsPacket) = parseRawIpPacket(request) ?: return

        when (val reason = shouldForward(dnsPacket)) {
            ForwardAction.FORWARD -> {
                Log.i(TAG, "allow ${dnsPacket.header.questions.joinToString(", ") { it.qName.name }}")

                val response = ByteBufferPool.poll()
                forwardAndWaitResponse(dnsPacket.rawData, response.array())

                writeBack(ipPacket, udpPacket, response.array(), output)
                ByteBufferPool.put(response)
            }

            ForwardAction.DENY -> {
                val domain = dnsPacket.header.questions.first().qName.name
                Log.i(TAG, "block $domain")
                val response = fakeNoAnswer(dnsPacket)
                writeBack(ipPacket, udpPacket, response, output)

                runBlocking {
                    blockDao.insert(Block(domain, Date()))
                }
            }

            is ForwardAction.DenyWithNotification -> {
                val domain = dnsPacket.header.questions.first().qName.name
                Log.i(TAG, "block and notify $domain")

                val response = fakeNoAnswer(dnsPacket)
                writeBack(ipPacket, udpPacket, response, output)
                runBlocking {
                    blockDao.insert(Block(domain, Date()))
                }
                SuspiciousNotification.createChat(service, domain, reason.reason)
            }
        }
    }

    private fun shouldForward(dnsPacket: DnsPacket): ForwardAction {
        val header: DnsPacket.DnsHeader = dnsPacket.header!!

        // why forward a response?
        if (header.isResponse) return ForwardAction.DENY

        // for now, let's assume only single question per packet
        val question = header.questions.first().qName.name

        if (inWhitelist(question)) return ForwardAction.FORWARD

        // read the cached values
        when (val cached = cache[question]) {
            is FilterStatus.Allowed -> {
                if (cached.date + MAX_ALLOWED_CACHE_TIME >= Date().time) {
                    Log.d(TAG, "allowed cache hit for $question")
                    return ForwardAction.FORWARD
                }
            }

            is FilterStatus.NotAllowed -> {
                if (cached.date + MAX_FORBIDDEN_CACHE_TIME >= Date().time) {
                    Log.d(TAG, "forbidden cache hit for $question")
                    return ForwardAction.DENY
                }
            }

            null -> {}
        }

        val reason = domainClassifier.predict(question)
        if (reason == MaliciousDomainClassifier.Result.BENIGN) {
            cache[question] = FilterStatus.Allowed(Date().time)
            return ForwardAction.FORWARD
        }

        cache[question] = FilterStatus.NotAllowed(Date().time)
        return ForwardAction.DenyWithNotification(reason)
    }

    private fun inWhitelist(target: String): Boolean {
        return elaSettings.whitelist.contains(target)
    }

    private fun fakeNoAnswer(dnsPacket: DnsPacket): ByteArray {
        val dnsRequest = Message(dnsPacket.rawData)
        val name = Name("ela.ela.ela.")
        val fakeAuthority = SOARecord(name, DClass.IN, 5, name, name, 0, 0, 0, 0, 5)

        dnsRequest.header.setFlag(Flags.QR.toInt())
        dnsRequest.header.rcode = Rcode.NOERROR
        dnsRequest.addRecord(fakeAuthority, Section.AUTHORITY)
        return dnsRequest.toWire()
    }

    private fun forwardAndWaitResponse(rawRequest: ByteArray, rawResponse: ByteArray) {
        val socket = DatagramSocket()
        service.protect(socket)

        socket.send(DatagramPacket(rawRequest, 0, rawRequest.size, upstreamDnsServer, 53))

        socket.receive(DatagramPacket(rawResponse, rawResponse.size))
        socket.close()
    }

    private fun writeBack(
        ipPacket: IpPacket,
        udpPacket: UdpPacket,
        rawUdpContents: ByteArray,
        output: FileOutputStream
    ) {
        val udpToWrite = UdpPacket.Builder(udpPacket)
            .srcPort(udpPacket.header.dstPort)
            .dstPort(udpPacket.header.srcPort)
            .srcAddr(ipPacket.header.dstAddr)
            .dstAddr(ipPacket.header.srcAddr)
            .correctChecksumAtBuild(true)
            .correctLengthAtBuild(true)
            .payloadBuilder(
                UnknownPacket.Builder()
                    .rawData(rawUdpContents)
            )

        val ipToWrite: IpPacket = when (ipPacket) {
            is IpV4Packet -> {
                IpV4Packet.Builder(ipPacket)
                    .srcAddr(ipPacket.header.dstAddr)
                    .dstAddr(ipPacket.header.srcAddr)
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true)
                    .payloadBuilder(udpToWrite)
                    .build()
            }

            is IpV6Packet -> {
                IpV6Packet.Builder(ipPacket)
                    .srcAddr(ipPacket.header.dstAddr)
                    .dstAddr(ipPacket.header.srcAddr)
                    .correctLengthAtBuild(true)
                    .payloadBuilder(udpToWrite)
                    .build()
            }

            else -> {
                Log.e(TAG, "no idea what kind of package this is $ipPacket")
                return
            }
        }

        output.write(ipToWrite.rawData)
    }

    fun recycle(elaSettings: ElaSettings) {
        this.elaSettings = elaSettings.copy()
        cache.clear()
    }

    fun destroy() {
        domainClassifier.destroy()
    }

    companion object {
        private const val MAX_ALLOWED_CACHE_TIME = 5 * 60 * 1000 // milliseconds
        private const val MAX_FORBIDDEN_CACHE_TIME = 30 * 60 * 1000 // milliseconds

        private fun parseRawIpPacket(request: ByteBuffer): Triple<IpPacket, UdpPacket, DnsPacket>? {
            val bytes = request.array()

            val ipPacket: IpPacket = run {
                val packet = IpSelector.newPacket(bytes, 0, bytes.size)
                if (packet !is IpPacket) return null
                packet
            }

            // ipPacket -> UdpPacket
            val udpPacket: UdpPacket = run {
                val packet = ipPacket.payload
                if (packet !is UdpPacket) return null
                packet
            }

            // UdpPacket -> DnsPacket
            val dnsPacket: DnsPacket = run {
                val packet = udpPacket.payload
                if (packet !is DnsPacket) return null
                packet
            }

            return Triple(ipPacket, udpPacket, dnsPacket)
        }

        private fun parseRawDns(response: ByteArray): DnsPacket? {
            return try {
                DnsPacket.newPacket(response, 0, response.size)
            } catch (e: Exception) {
                null
            }
        }
    }
}
