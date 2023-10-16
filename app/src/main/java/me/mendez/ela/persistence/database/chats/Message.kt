package me.mendez.ela.persistence.database.chats

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date


@Entity
data class Message(
    val user: Boolean,
    val date: Date,
    val content: String,
    @PrimaryKey
    val id: Int? = null,
)
