package me.mendez.ela.vpn

import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

object ByteBufferPool {
    private val pool = ConcurrentLinkedQueue<ByteBuffer>()

    fun poll(): ByteBuffer {
        return pool.poll() ?: ByteBuffer.allocate(32767)
    }

    fun put(byteBuffer: ByteBuffer) {
        byteBuffer.clear()
        pool.add(byteBuffer)
    }
}
