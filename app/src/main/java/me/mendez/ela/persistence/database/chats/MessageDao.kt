package me.mendez.ela.persistence.database.chats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface MessageDao {
    @Query("SELECT * FROM Message WHERE conversation = :conversation ORDER BY date ASC")
    fun get(conversation: Long): Flow<List<Message>>

    @Insert
    suspend fun add(message: Message)

    @Query("DELETE FROM Message WHERE content = :conversation")
    suspend fun deleteChat(conversation: Long)

    fun getMessages(conversation: Long): Flow<List<me.mendez.ela.chat.Message>> {
        val flow = get(conversation)
        return flow.map {
            it.map { message ->
                me.mendez.ela.chat.Message(
                    message.content,
                    message.user,
                    message.date
                )
            }
        }
    }

    suspend fun addMessage(conversation: Long, message: me.mendez.ela.chat.Message) {
        add(Message(conversation, message.user, message.date, message.content))
    }

    suspend fun addMessages(conversation: Long, messages: List<me.mendez.ela.chat.Message>) {
        messages.forEach { addMessage(conversation, it) }
    }
}
