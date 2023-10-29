package me.mendez.ela.persistence.database.chat

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.mendez.ela.chat.Sender
import java.util.*

@Entity
data class ChatMessage(
    val date: Date,
    val sender: Sender,
    val content: String,
    @PrimaryKey
    val id: Int? = null,
)
