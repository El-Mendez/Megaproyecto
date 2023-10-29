package me.mendez.ela.chat.dto

import kotlinx.serialization.Serializable
import me.mendez.ela.chat.Message
import me.mendez.ela.chat.Sender


@Serializable
internal data class MessageGPTRequest(
    val role: String,
    val content: String,
)

internal fun List<Message>.wrapForTransfer(): List<MessageGPTRequest> {
    val cleaned = mutableListOf<MessageGPTRequest>()

    forEach {
        if (it.user == Sender.SYSTEM)
            return@forEach

        cleaned.add(
            MessageGPTRequest(
                if (it.user == Sender.USER) "user" else "assistant",
                it.content,
            )
        )
    }

    return cleaned
}
