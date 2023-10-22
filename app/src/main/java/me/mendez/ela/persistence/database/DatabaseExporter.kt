package me.mendez.ela.persistence.database

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider

// https://stackoverflow.com/questions/68329072/export-room-database-and-attach-to-email-android-kotlin
class DatabaseProvider : FileProvider() {
    fun getUri(context: Context, database: String): Triple<Uri, Uri, Uri> {
        val file = context.getDatabasePath(database)
        val shm = context.getDatabasePath("$database-shm")
        val wal = context.getDatabasePath("$database-wal")

        val authority = "${context.packageName}.databaseprovider"

        return Triple(
            getUriForFile(context, authority, file),
            getUriForFile(context, authority, shm),
            getUriForFile(context, authority, wal),
        )
    }
}
