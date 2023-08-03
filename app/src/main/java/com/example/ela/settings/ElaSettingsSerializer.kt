package com.example.ela.settings

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream


object SettingsSerializer : Serializer<ElaSettings> {
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

val Context.settingsDataStore: DataStore<ElaSettings> by dataStore(
    fileName = "ela-settings.json",
    serializer = SettingsSerializer
)
