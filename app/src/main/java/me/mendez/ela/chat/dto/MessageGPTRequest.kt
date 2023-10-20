package me.mendez.ela.chat.dto

import kotlinx.serialization.Serializable
import me.mendez.ela.chat.Message


@Serializable
internal data class MessageGPTRequest(
    val role: String,
    val content: String,
)

internal fun List<Message>.wrapForTransfer(): List<MessageGPTRequest> {
    return map {
        MessageGPTRequest(
            if (it.userCreated) "user" else "assistant",
            it.content
        )
    }
}
