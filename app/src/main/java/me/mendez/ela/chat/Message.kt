package me.mendez.ela.chat

import java.util.Date

enum class Sender {
    SYSTEM, ELA, USER
}

data class Message(
    val content: String,
    val user: Sender,
    val date: Date = Date(),
) {
    companion object {
        fun noInternetMessage(): List<Message> {
            return listOf(
                Message(
                    "Vaya, parece que no tienes internet. Debes tener internet para que pueda apoyarte mejor",
                    Sender.SYSTEM,
                    Date(),
                )
            )
        }
    }
}
