package me.mendez.ela.vpn

import android.util.Log
import kotlinx.coroutines.runBlocking
import me.mendez.ela.ml.MaliciousDomainClassifier
import me.mendez.ela.ml.isBenign
import me.mendez.ela.persistence.database.blocks.Block
import me.mendez.ela.persistence.database.blocks.BlockDao
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.services.SuspiciousNotification
import org.apache.commons.net.whois.WhoisClient
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
        val domain = dnsPacket.header.questions.first().qName.name

        if (dnsPacket.header.isResponse) return

        // if cached
        when (checkCache(dnsPacket)) {
            true -> {
                Log.i(TAG, "allow $domain (cache)")
                val response = ByteBufferPool.poll()
                forwardAndWaitResponse(dnsPacket.rawData, response.array())
                writeBack(ipPacket, udpPacket, response.array(), output)
                ByteBufferPool.put(response)
                return
            }

            false -> {
                Log.i(TAG, "block $domain (cache)")
                writeBack(ipPacket, udpPacket, notFoundAnswer(dnsPacket), output)
                runBlocking {
                    blockDao.insert(Block(domain, Date()))
                }
                return
            }

            null -> {}
        }

        // not cached, manually check the packet
        val rawResponse = ByteBufferPool.poll()
        forwardAndWaitResponse(dnsPacket.rawData, rawResponse.array())
        val response = DnsPacket.newPacket(rawResponse.array(), 0, rawResponse.array().size)
        val whoisData = getWhoisData(domain)

        val linkType = domainClassifier.predict(domain, response, whoisData)

        if (linkType.isBenign()) {
            putInCache(domain, true)
            Log.i(TAG, "allow $domain")
            writeBack(ipPacket, udpPacket, rawResponse.array(), output)
        } else {
            putInCache(domain, false)
            Log.i(TAG, "blocked $domain ($linkType)")
            writeBack(ipPacket, udpPacket, notFoundAnswer(dnsPacket), output)
            SuspiciousNotification.createChat(service, domain, linkType)
            runBlocking {
                blockDao.insert(Block(domain, Date()))
            }
        }

        ByteBufferPool.put(rawResponse)
    }

    private fun putInCache(domain: String, allowed: Boolean) {
        cache[domain] = if (allowed) {
            FilterStatus.Allowed(Date().time)
        } else {
            FilterStatus.NotAllowed(Date().time)
        }
    }

    private fun checkCache(dnsPacket: DnsPacket): Boolean? {
        val header: DnsPacket.DnsHeader = dnsPacket.header!!

        // for now, let's assume only single question per packet
        val question = header.questions.first().qName.name


        // lets ignore all in-addr.arpa domains
        if (question.endsWith(".arpa") || question.endsWith(".arpa.")) return true

        if (inWhitelist(question)) return true

        // check caches
        val cached = cache[question] ?: return null
        when (cached) {
            is FilterStatus.Allowed -> {
                if (cached.date + MAX_ALLOWED_CACHE_TIME >= Date().time) {
                    Log.d(TAG, "allowed cache hit for $question")
                    return true
                }
            }

            is FilterStatus.NotAllowed -> {
                if (cached.date + MAX_FORBIDDEN_CACHE_TIME >= Date().time) {
                    Log.d(TAG, "forbidden cache hit for $question")
                    return false
                }
            }
        }

        return null
    }

    private fun inWhitelist(target: String): Boolean {
        return elaSettings.whitelist.contains(target)
    }

    private fun notFoundAnswer(dnsPacket: DnsPacket): ByteArray {
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

        private fun getWhoisData(domain: String): String? {
            val segments = domain.split(".")
            if (segments.isEmpty()) return null

            val topDomain = if (segments.last().isEmpty()) {
                segments
                    .slice(maxOf(0, segments.size - 3)..<segments.size)
                    .joinToString(".") { it }
            } else {
                segments
                    .slice(maxOf(0, segments.size - 2)..<segments.size)
                    .joinToString(".") { it }
            }

            val whois = WhoisClient()
            whois.connect(WhoisClient.DEFAULT_HOST)
            val result = whois.query(topDomain).toString()
            whois.disconnect()

            return result
        }
    }
}
