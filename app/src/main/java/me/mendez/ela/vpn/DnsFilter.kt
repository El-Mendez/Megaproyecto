package me.mendez.ela.vpn

import android.util.Log
import me.mendez.ela.persistence.settings.ElaSettings
import org.pcap4j.packet.*
import org.xbill.DNS.*
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

        if (shouldForward(dnsPacket)) {
            Log.i(TAG, "allow ${dnsPacket.header.questions.joinToString(", ") { it.toString() }}")

            val response = ByteBufferPool.poll()
            forwardAndWaitResponse(dnsPacket.rawData, response.array())

            writeBack(ipPacket, udpPacket, response.array(), output)
            ByteBufferPool.put(response)
        } else {
            Log.i(TAG, "block ${dnsPacket.header.questions.joinToString(", ") { it.toString() }}")
            val response = fakeNoAnswer(dnsPacket)
            writeBack(ipPacket, udpPacket, response, output)
        }
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
        return !elaSettings.whitelist.contains(target)
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
