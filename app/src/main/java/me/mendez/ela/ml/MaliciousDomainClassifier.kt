package me.mendez.ela.ml

import android.content.Context

class MaliciousDomainClassifier(val context: Context) {
    enum class Result {
        BENIGN
    }

    fun load() {}

    fun destroy() {}

    fun predict(domain: String): Result {
        return Result.BENIGN
    }

    private fun encodePermissions(): FloatArray {
        return FloatArray(0)
    }
}
