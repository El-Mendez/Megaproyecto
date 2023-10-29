package me.mendez.ela.persistence.database.blocks.chats

import androidx.room.Entity
import androidx.room.PrimaryKey
import me.mendez.ela.chat.Sender
import java.util.Date


@Entity
data class Message(
    val conversation: Long,
    val user: Sender,
    val date: Date,
    val content: String,
    @PrimaryKey
    val id: Int? = null,
)
