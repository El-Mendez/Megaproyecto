package me.mendez.ela.persistence.database.blocks

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity
data class Block(
    val domain: String,
    val date: Date,
    @PrimaryKey
    val id: Int? = null,
)
