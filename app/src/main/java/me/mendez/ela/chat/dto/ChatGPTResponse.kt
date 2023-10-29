package me.mendez.ela.chat.dto

import kotlinx.serialization.Serializable
import me.mendez.ela.chat.Message
import me.mendez.ela.chat.Sender
import java.util.*

@Serializable
internal data class Choice(
    val message: MessageGPTRequest,
)

@Serializable
internal data class ChatGPTResponse(
    val id: String,
    val choices: List<Choice>
) {
    fun unwrap(): List<Message> {
        return choices.map {
            Message(
                content = it.message.content,
                user = if (it.message.role == "assistant") Sender.ELA else Sender.USER,
                date = Date()
            )
        }
    }
}
