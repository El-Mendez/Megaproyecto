package me.mendez.ela.persistence.database.chats

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Dao
interface MessageDao {
    @Query("SELECT * FROM Message WHERE domain = :domain ORDER BY date ASC")
    fun get(domain: String): Flow<List<Message>>

    @Insert
    suspend fun add(message: Message)

    @Query("DELETE FROM Message WHERE domain = :domain")
    suspend fun deleteChat(domain: String)

    fun getMessages(domain: String): Flow<List<me.mendez.ela.chat.Message>> {
        return get(domain)
            .map {
                it.map {
                    me.mendez.ela.chat.Message(
                        it.content,
                        it.user,
                        it.date
                    )
                }
            }
    }

    suspend fun addMessage(domain: String, message: me.mendez.ela.chat.Message) {
        add(Message(message.userCreated, message.date, message.content, domain))
    }

}
