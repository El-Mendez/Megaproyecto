package me.mendez.ela.vpn

import android.util.Log
import me.mendez.ela.persistence.settings.ElaSettings
import org.pcap4j.packet.*
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import java.nio.ByteBuffer

private const val TAG = "ELA_DNS_FILTER"

class DnsFilter(private val service: ElaVpnService, var elaSettings: ElaSettings) {
    private val upstreamDnsServer = Inet4Address.getByName("1.1.1.1")

    fun filter(request: ByteBuffer, output: FileOutputStream) {
        val (ipPacket, udpPacket, dnsPacket) = parseRawIpPacket(request) ?: return

        if (shouldForward(dnsPacket))
            forward(udpPacket, dnsPacket, ipPacket, output)
    }

    private fun shouldForward(dnsPacket: DnsPacket): Boolean {
        val header: DnsPacket.DnsHeader = dnsPacket.header!!

        // why forward a response?
        if (header.isResponse) return false

        // for now, let's assume only single question per packet
        val question = header.questions.first()

        if (inWhitelist(question.qName.name)) return true

        return false
    }

    private fun inWhitelist(target: String): Boolean {
        return elaSettings.whitelist.contains(target)
    }

    private fun forward(udpPacket: UdpPacket, dnsRequest: DnsPacket, ipPacket: IpPacket, output: FileOutputStream) {
        val rawDnsRequest = dnsRequest.rawData

        val socket = DatagramSocket()
        service.protect(socket)

        socket.send(DatagramPacket(rawDnsRequest, 0, rawDnsRequest.size, upstreamDnsServer, 53))

        val rawDnsResponseBuffer = ByteBufferPool.poll()
        val rawDnsResponse = rawDnsResponseBuffer.array()

        socket.receive(DatagramPacket(rawDnsResponse, rawDnsResponse.size))
        socket.close()

        val dnsResponse = parseRawDns(rawDnsResponse)
        if (dnsResponse != null) {
            Log.d(TAG, "req: $dnsRequest \n\n\nres: $dnsResponse")
        }

        val udpToWrite = UdpPacket.Builder(udpPacket)
            .srcPort(udpPacket.header.dstPort)
            .dstPort(udpPacket.header.srcPort)
            .srcAddr(ipPacket.header.dstAddr)
            .dstAddr(ipPacket.header.srcAddr)
            .payloadBuilder(
                UnknownPacket.Builder()
                    .rawData(rawDnsResponse)
            )

        val ipToWrite: IpPacket = when (ipPacket) {
            is IpV4Packet -> {
                IpV4Packet.Builder()
                    .srcAddr(ipPacket.header.dstAddr)
                    .dstAddr(ipPacket.header.srcAddr)
                    .correctChecksumAtBuild(true)
                    .correctLengthAtBuild(true)
                    .payloadBuilder(udpToWrite)
                    .build()
            }

            is IpV6Packet -> {
                IpV6Packet.Builder()
                    .srcAddr(ipPacket.header.dstAddr)
                    .dstAddr(ipPacket.header.srcAddr)
                    .correctLengthAtBuild(true)
                    .payloadBuilder(udpToWrite)
                    .build()
            }

            else -> {
                Log.i(TAG, "no idea what this is supposed to be $ipPacket")
                return
            }
        }

        Log.i(TAG, "req: $ipPacket \n\nres: $ipToWrite")

        output.write(ipToWrite.rawData, 0, ipToWrite.rawData.size)
        ByteBufferPool.put(rawDnsResponseBuffer)
    }

    fun destroy() {

    }

    companion object {
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
