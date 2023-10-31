package me.mendez.ela.vpn

import android.util.Log
import com.google.common.net.InternetDomainName
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
    private val upstreamDnsServer = Inet4Address.getByName("8.8.8.8")
    private val cache = ConcurrentHashMap<String, FilterStatus>()

    init {
        domainClassifier.load()
    }

    fun filter(request: ByteBuffer, output: FileOutputStream) {
        val (ipPacket, udpPacket, dnsPacket) = parseRawIpPacket(request) ?: return
        val domain = dnsPacket.header.questions.first().qName.name

        if (dnsPacket.header.isResponse) return
        var rawResponse: ByteBuffer? = null

        if (!elaSettings.blockDefault) {
            rawResponse = lazyWaitForAnswer(dnsPacket.rawData, rawResponse, domain)
            if (rawResponse == null) {
                Log.e(TAG, "error $domain (forwarding udp packet)")
                return
            }
        }

        // if cached
        when (checkCache(dnsPacket)) {
            true -> {
                Log.i(TAG, "allow $domain (cache)")
                rawResponse = lazyWaitForAnswer(dnsPacket.rawData, rawResponse, domain)
                if (rawResponse == null) {
                    Log.e(TAG, "error $domain (forwarding udp packet)")
                } else {
                    accept(ipPacket, udpPacket, rawResponse, output, domain)
                }
                return
            }

            false -> {
                Log.i(TAG, "block $domain (cache)")
                if (rawResponse == null) { // if blocking by default
                    deny(ipPacket, udpPacket, dnsPacket, output)
                } else {
                    accept(ipPacket, udpPacket, rawResponse, output, domain)
                }

                runBlocking {
                    blockDao.insert(Block(domain, Date()))
                }
                return
            }

            null -> {}
        }

        // not cached, manually check the packet
        rawResponse = lazyWaitForAnswer(dnsPacket.rawData, rawResponse, domain)
        if (rawResponse == null) {
            Log.e(TAG, "error $domain (forwarding udp packet)")
            return
        }

        val dnsResponse = DnsPacket.newPacket(rawResponse.array(), 0, rawResponse.array().size)
        val whoisData = getWhoisData(domain)

        val linkType = domainClassifier.predict(domain, dnsResponse, whoisData)

        if (linkType.isBenign()) {
            putInCache(domain, true)
            Log.i(TAG, "allow $domain")
            accept(ipPacket, udpPacket, rawResponse, output, domain)
        } else {
            putInCache(domain, false)
            Log.i(TAG, "blocked $domain ($linkType)")
            if (elaSettings.blockDefault) {
                deny(ipPacket, udpPacket, dnsPacket, output, rawResponse)
            } else {
                accept(ipPacket, udpPacket, rawResponse, output, domain)
            }
            val conversation = runBlocking {
                blockDao.insert(Block(domain, Date()))
            }
            SuspiciousNotification.createChat(service, domain, conversation, linkType)
        }
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
                    return true
                }
            }

            is FilterStatus.NotAllowed -> {
                if (cached.date + MAX_FORBIDDEN_CACHE_TIME >= Date().time) {
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

    private fun waitForAnswer(rawRequest: ByteArray, rawResponse: ByteArray, domain: String): Boolean {
        val socket = try {
            val socket = DatagramSocket()
            socket.soTimeout = 5_000
            service.protect(socket)
            socket
        } catch (_: Exception) {
            return false
        }

        val sent = try {
            Log.v(TAG, "sending udp packet ($domain)")
            socket.send(DatagramPacket(rawRequest, 0, rawRequest.size, upstreamDnsServer, 53))
            Log.v(TAG, "waiting for udp response $domain")
            socket.receive(DatagramPacket(rawResponse, rawResponse.size))
            Log.v(TAG, "got udp response $domain")
            true
        } catch (_: Exception) {
            Log.d(TAG, "could not send or receive udp packet $domain")
            false
        }

        return try {
            socket.close()
            sent
        } catch (e: Exception) {
            Log.w(TAG, "could not cleanup socket")
            sent
        }
    }

    private fun lazyWaitForAnswer(rawRequest: ByteArray, oldAnswer: ByteBuffer?, domain: String): ByteBuffer? {
        if (oldAnswer != null)
            return oldAnswer

        val response = ByteBufferPool.poll()
        return if (waitForAnswer(rawRequest, response.array(), domain)) {
            response
        } else {
            ByteBufferPool.put(response)
            null
        }
    }

    private fun accept(
        ipPacket: IpPacket,
        udpPacket: UdpPacket,
        udpContents: ByteBuffer,
        output: FileOutputStream,
        domain: String,
    ) {
        val packet = DnsPacket.newPacket(
            udpContents.array(),
            0,
            udpContents.array().size
        )
        Log.v(
            TAG,
            "accepting package for domain $domain. $packet"
        )
        writeBack(ipPacket, udpPacket, packet.rawData, output)
        ByteBufferPool.put(udpContents)
    }

    private fun deny(
        ipPacket: IpPacket,
        udpPacket: UdpPacket,
        dnsRequest: DnsPacket,
        output: FileOutputStream,
        buffer: ByteBuffer? = null,
    ) {
        writeBack(ipPacket, udpPacket, notFoundAnswer(dnsRequest), output)

        if (buffer != null)
            ByteBufferPool.put(buffer)
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
        // TODO maybe clear cache from old domains?
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
                if (packet !is IpPacket) {
                    Log.w(TAG, "expected IP packet: $packet")
                    return null
                }
                packet
            }

            // ipPacket -> UdpPacket
            val udpPacket: UdpPacket = run {
                val packet = ipPacket.payload
                if (packet !is UdpPacket) {
                    Log.w(TAG, "expected UDP packet: $ipPacket \npayload was $packet")
                    return null
                }
                packet
            }

            // UdpPacket -> DnsPacket
            val dnsPacket: DnsPacket = run {
                val packet = udpPacket.payload
                if (packet !is DnsPacket) {
                    Log.w(TAG, "expected UDP packet: $packet")
                    return null
                }
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

            val topPrivateDomain = InternetDomainName.from(domain)
                .topPrivateDomain()
                .toString()

            return try {
                Log.v(TAG, "starting whois request for $domain ($topPrivateDomain)")
                val whois = WhoisClient()
                whois.connect(WhoisClient.DEFAULT_HOST)
                val result = whois.query(topPrivateDomain).toString()
                whois.disconnect()
                Log.v(TAG, "finish whois request for $domain")

                result
            } catch (e: Exception) {
                Log.e(TAG, "whois request for $domain failed $e")
                null
            }
        }
    }
}
