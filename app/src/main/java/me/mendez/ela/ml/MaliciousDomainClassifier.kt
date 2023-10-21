package me.mendez.ela.ml

import android.content.Context
import android.util.Log
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Years
import org.joda.time.format.DateTimeFormat
import org.pcap4j.packet.DnsPacket
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.ln

private const val TAG = "ELA_DOMAIN_CLASSIFIER"

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

    fun predict(domain: String, response: DnsPacket, whois: String?): Result {
        val input = encode(domain, response, whois)
        Log.d(TAG, "$domain: [${input.joinToString(", ") { it.toString() }}]")

        val result = synchronized(lock) {
            buffer!!.loadArray(input)
            0
        }

        return when (result) {
            0 -> Result.BENIGN
            1 -> Result.MALWARE
            2 -> Result.PHISHING
            3 -> Result.RANSOMWARE
            4 -> Result.RANSOMWARE
            else -> {
                Log.e(TAG, "Unknown model result type $result. Defaulting to benign.")
                Result.BENIGN
            }
        }
    }


    private fun encode(
        domain: String,
        response: DnsPacket,
        whois: String?,
    ): FloatArray {
        val (expiration, updated, creation) = extractWhoisData(whois)

        return listOf(
            associatedIps(response),
            lifetime(creation, expiration),
            activeLifetime(creation, updated),
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

        private fun lifetime(creationDate: DateTime?, expirationDateTime: DateTime?): Float {
            if (creationDate == null || expirationDateTime == null) return 0f

            return Years.yearsBetween(creationDate, expirationDateTime).years.toFloat()
        }

        private fun activeLifetime(creationDate: DateTime?, lastUpdatedDate: DateTime?): Float {
            if (creationDate == null || lastUpdatedDate == null) return -1f

            return Days.daysBetween(creationDate, lastUpdatedDate).days.toFloat()
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
            return charAmount(domain, "123456789")
        }

        private fun letterAmount(domain: String): Float {
            return charAmount(domain, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
        }

        private fun symbolAmount(domain: String): Float {
            return charAmount(domain, "-_")
        }


        private fun consecutiveNumbers(domain: String): Float {
            return consecutiveChars(domain, "1234567890")
        }

        private fun consecutiveLetters(domain: String): Float {
            return consecutiveChars(domain, "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ")
        }

        private fun consecutiveSymbols(domain: String): Float {
            return consecutiveChars(domain, "-_")
        }

        private fun domainSize(domain: String): Float = domain.length.toFloat()

        private fun extractWhoisData(whoisValue: String?): Triple<DateTime?, DateTime?, DateTime?> {
            if (whoisValue == null)
                return Triple(null, null, null)

            val date = "\\d{4}-\\d{1,2}-\\d{1,2}"

            val creationDateRegex = "Creation Date: $date".toRegex()
            val updatedDateRegex = "Updated Date: $date".toRegex()
            val expirationDateRegex =
                "((Registrar Registration Expiration Date)|(Registry Expiry Date)): $date".toRegex()

            return try {
                Triple(
                    findAndParseDate(whoisValue, expirationDateRegex),
                    findAndParseDate(whoisValue, updatedDateRegex),
                    findAndParseDate(whoisValue, creationDateRegex),
                )
            } catch (e: Exception) {
                Triple(null, null, null)
            }
        }

        private fun findAndParseDate(whois: String, expression: Regex): DateTime? {
            val line = expression.find(whois)?.value ?: return null
            val string = line.split(" ").lastOrNull() ?: return null

            val formatter = DateTimeFormat.forPattern("yyyy-MM-dd")
            return DateTime.parse(string, formatter)
        }

        private fun charAmount(domain: String, chars: String): Float {
            var amount = 0
            domain.forEach {
                if (chars.contains(it))
                    amount += 1
            }
            return amount.toFloat()
        }

        private fun consecutiveChars(domain: String, chars: String): Float {
            var chain = 0
            var longestChain = 0

            domain.forEach {
                if (chars.contains(it)) {
                    chain += 1
                } else {
                    if (chain > longestChain)
                        longestChain = chain
                    chain = 0
                }
            }

            if (chain > longestChain)
                longestChain = chain

            return longestChain.toFloat()
        }
    }
}

fun MaliciousDomainClassifier.Result.isBenign(): Boolean {
    return this == MaliciousDomainClassifier.Result.BENIGN
}

fun MaliciousDomainClassifier.Result.prompt(): String {
    val type = when (this) {
        MaliciousDomainClassifier.Result.BENIGN -> return "dame un dato interesante de ciberseguridad"
        MaliciousDomainClassifier.Result.MALWARE -> "malware"
        MaliciousDomainClassifier.Result.PHISHING -> "phishing"
        MaliciousDomainClassifier.Result.RANSOMWARE -> "ransomware"
    }

    return "Qué es $type y cómo puedo protegerme ante posibles ataques?"
}
