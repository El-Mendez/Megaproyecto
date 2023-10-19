package me.mendez.ela.vpn

import me.mendez.ela.persistence.settings.ElaSettings
import java.io.FileOutputStream
import java.nio.ByteBuffer

class DnsFilter(private val service: ElaVpnService, var elaSettings: ElaSettings? = null) {
    fun filter(request: ByteBuffer, output: FileOutputStream) {
        if (shouldForward(request)) {
            forward(request, output)
        } else {
            shouldBlock(request, output)
        }

        ByteBufferPool.put(request)
    }

    @Synchronized
    private fun shouldForward(request: ByteBuffer): Boolean {
        TODO()
    }

    private fun forward(request: ByteBuffer, output: FileOutputStream) {
        TODO()
    }

    private fun shouldBlock(request: ByteBuffer, output: FileOutputStream) {
        TODO()
    }
}
