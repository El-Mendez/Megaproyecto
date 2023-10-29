package me.mendez.ela.persistence.database.chat

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.mendez.ela.chat.Message

@Dao
interface ChatDao {
    @Query("SELECT * FROM ChatMessage ORDER BY date ASC")
    fun get(): Flow<List<ChatMessage>>

    @Insert
    suspend fun add(message: ChatMessage)

    suspend fun add(message: Message) {
        add(ChatMessage(message.date, message.user, message.content))
    }

    suspend fun addAll(messages: List<Message>) {
        messages.forEach { add(it) }
    }

    fun getAll(): Flow<List<Message>> {
        val flow = get()
        return flow.map {
            it.map { msg ->
                Message(msg.content, msg.sender, msg.date)
            }
        }
    }
}
