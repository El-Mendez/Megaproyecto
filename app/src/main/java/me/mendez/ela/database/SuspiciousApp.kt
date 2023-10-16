package me.mendez.ela.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SuspiciousApp(
    @PrimaryKey val packageName: String,
)
