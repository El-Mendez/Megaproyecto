package me.mendez.ela.persistence.settings

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.MultiProcessDataStoreFactory
import androidx.datastore.core.Serializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Serializable
data class ElaSettings(
    val vpnRunning: Boolean,
    val startOnBoot: Boolean,
    val blockDefault: Boolean,
    val ready: Boolean,
    val whitelist: List<String>,
) {
    fun withAddedInWhitelist(newDomain: String): ElaSettings {
        val domain = extractDomain(newDomain) ?: return this

        val newDomains = whitelist.toMutableList()
        newDomains.add(domain)

        return withWhitelist(newDomains)
    }

    fun withWhitelist(newDomains: List<String>): ElaSettings {
        val finalDomains = newDomains.toHashSet().toList().sorted()

        if (whitelist.size == finalDomains.size && whitelist.containsAll(finalDomains)) {
            return this
        }

        return copy(whitelist = newDomains)
    }

    companion object {

        fun extractDomain(url: String): String? {
            if (url.matches("([a-zA-Z0-9\\-_]+:\\/\\/)?([a-zA-Z0-9\\-_]+\\.)*([a-zA-Z0-9\\-_]+@)?[a-zA-Z0-9\\-_]+\\.[a-zA-Z0-9\\-_]+(:[0-9]+)?[\\/@a-zA-Z0-9%&=+]*".toRegex())) {
                return url
                    .split("://", limit = 2).last()
                    .split("@", limit = 2).last()
                    .split("/", limit = 2).first()
                    .split(":", limit = 2).first()
                    .lowercase()
            }
            return null
        }


        fun default(): ElaSettings {
            return ElaSettings(
                vpnRunning = false,
                blockDefault = true,
                startOnBoot = false,
                ready = true,
                whitelist = emptyList(),
            )
        }
    }
}

enum class ActionNeeded {
    START, STOP, RESTART, NONE
}

suspend fun DataStore<ElaSettings>.nextAction(transform: suspend (ElaSettings) -> ElaSettings): ActionNeeded {
    var action = ActionNeeded.NONE
    updateData {
        val old = it
        val updated = transform(old)

        if (old.vpnRunning != updated.vpnRunning) {
            action = if (updated.vpnRunning) ActionNeeded.START else ActionNeeded.STOP
        } else if (updated.vpnRunning && old != updated) {
            val changes = (
                    old.blockDefault != updated.blockDefault ||
                            old.whitelist.size != updated.whitelist.size ||
                            !old.whitelist.containsAll(updated.whitelist)
                    )

            if (changes)
                action = ActionNeeded.RESTART
        }

        return@updateData updated
    }

    this.updateData(transform)
    return action
}

@Module
@InstallIn(SingletonComponent::class)
object ElaSettingsModule {
    @Singleton
    class SettingsSerializer @Inject constructor() : Serializer<ElaSettings> {
        override val defaultValue = ElaSettings.default()

        override suspend fun readFrom(input: InputStream): ElaSettings {
            return try {
                Json.decodeFromString(
                    deserializer = ElaSettings.serializer(),
                    string = input.readBytes().decodeToString(),
                )
            } catch (e: SerializationException) {
                Log.e(null, "cannot read ElaSettings $e")
                defaultValue
            }
        }

        override suspend fun writeTo(
            t: ElaSettings,
            output: OutputStream
        ) {
            output.write(
                Json.encodeToString(
                    serializer = ElaSettings.serializer(),
                    value = t
                ).encodeToByteArray()
            )
        }
    }

    @Provides
    @Singleton
    fun provideElaSettingsStore(@ApplicationContext context: Context): DataStore<ElaSettings> =
        MultiProcessDataStoreFactory.create(
            serializer = SettingsSerializer(),
            produceFile = {
                File("${context.cacheDir.path}/ela-settings.json")
            }
        )
}
