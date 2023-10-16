package me.mendez.ela.persistence.database.apps

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SuspiciousApp(
    @PrimaryKey val packageName: String,
)
