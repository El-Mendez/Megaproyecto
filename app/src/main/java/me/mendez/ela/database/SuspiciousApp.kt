package me.mendez.ela.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class SuspiciousApp(
    @PrimaryKey val packageName: String,
    val installedDate: Date,
    val uninstalledDate: Date?,
)
