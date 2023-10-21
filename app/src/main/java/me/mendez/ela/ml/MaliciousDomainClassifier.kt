package me.mendez.ela.ml

import android.content.Context
import io.mailguru.whois.model.WhoisResult
import io.mailguru.whois.service.WhoisService
import org.pcap4j.packet.DnsPacket
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.ln

class MaliciousDomainClassifier(val context: Context) {
    private var buffer: TensorBuffer? = null
    private val lock = Any()

    enum class Result {
        BENIGN, MALWARE, PHISHING, RANSOMWARE,
    }

    fun load() {
        if (buffer == null) {
            buffer = TensorBuffer.createFixedSize(intArrayOf(1, 11), DataType.FLOAT32)
        }
    }

    fun destroy() {

    }

    fun predict(domain: String, response: DnsPacket): Result {
        val segments = domain.split(".")
        if (segments.isEmpty()) return Result.BENIGN

        val topDomain = if (segments.last().isEmpty()) {
            segments
                .slice(maxOf(0, segments.size - 3)..<segments.size)
                .joinToString(".") { it }
        } else {
            segments
                .slice(maxOf(0, segments.size - 2)..<segments.size)
                .joinToString(".") { it }
        }

        val result = WhoisService.lookup(topDomain)
        val input = encode(domain, response, result)

        return synchronized(lock) {
            buffer!!.loadArray(input)
            Result.BENIGN
        }
    }


    private fun encode(domain: String, response: DnsPacket, whois: WhoisResult?): FloatArray {
        return listOf(
            associatedIps(response),
            lifetime(whois),
            activeLifetime(whois),
            entropy(domain),
            numAmount(domain),
            letterAmount(domain),
            symbolAmount(domain),
            consecutiveNumbers(domain),
            consecutiveLetters(domain),
            consecutiveSymbols(domain),
            domainSize(domain)
        ).toFloatArray()
    }

    // encoder
    companion object {
        private fun associatedIps(response: DnsPacket): Float {
            return response.header.answers.size.toFloat()
        }

        private fun lifetime(whois: WhoisResult?): Float {
            return if (whois == null) {
                // expiración - creación (años)
                0f
            } else {
                TODO()
            }
        }

        private fun activeLifetime(whois: WhoisResult?): Float {
            // actualización - creación (días)
            return if (whois == null) {
                -1f
            } else {
                TODO()
            }
        }

        private fun entropy(domain: String): Float {
            val occurrences = mutableMapOf<Char, Int>()
            domain.forEach {
                occurrences[it] = (occurrences[it] ?: 0) + 1
            }

            return occurrences.values.map {
                val p = it.toDouble() / domain.length.toDouble()
                (p * ln(p) / ln(2.0))
            }.sum().toFloat()
        }

        private fun numAmount(domain: String): Float {
            return domain.count { "012345679".contains(it) }.toFloat()
        }

        private fun letterAmount(domain: String): Float {
            return domain.count { "abcdefghijklmopqrstuvwxyz".contains(it.lowercaseChar()) }.toFloat()
        }

        private fun symbolAmount(domain: String): Float {
            return domain.count { "-_".contains(it.lowercaseChar()) }.toFloat()
        }

        private fun consecutiveNumbers(domain: String): Float {
            return "[0-9]+".toRegex()
                .findAll(domain)
                .maxBy { it.value.length }
                .value.length.toFloat()
        }

        private fun consecutiveLetters(domain: String): Float {
            return "[a-zA-Z]+".toRegex()
                .findAll(domain)
                .maxBy { it.value.length }
                .value.length.toFloat()
        }

        private fun consecutiveSymbols(domain: String): Float {
            return "[_\\-]+".toRegex()
                .findAll(domain)
                .maxBy { it.value.length }
                .value.length.toFloat()
        }

        private fun domainSize(domain: String): Float = domain.length.toFloat()
    }
}

fun MaliciousDomainClassifier.Result.isBenign(): Boolean {
    return this == MaliciousDomainClassifier.Result.BENIGN
}

fun MaliciousDomainClassifier.Result.prompt(): String {
    return when (this) {
        MaliciousDomainClassifier.Result.BENIGN -> "dame un dato interesante de ciberseguridad"
        else -> {
            val type = when (this) {
                MaliciousDomainClassifier.Result.MALWARE -> "malware"
                MaliciousDomainClassifier.Result.PHISHING -> "phishing"
                MaliciousDomainClassifier.Result.RANSOMWARE -> "ransomware"
                MaliciousDomainClassifier.Result.BENIGN -> TODO()
            }
            "Qué es $type y cómo puedo protegerme ante posibles ataques?"
        }
    }
}
