package me.mendez.ela.ui.screens.chat

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import me.mendez.ela.chat.Message
import me.mendez.ela.remote.ChatApi
import java.util.*
import javax.inject.Inject


@HiltViewModel
class ChatViewModel @Inject constructor(
    val chatApi: ChatApi
) : ViewModel() {
    val messages: SnapshotStateList<Message> = mutableStateListOf()
    val calculatingResponse = mutableStateOf(false)

    fun sendMessage(content: String) {
        messages.add(
            Message(
                content,
                userCreated = true,
                date = Date()
            )
        )

        viewModelScope.launch {
            calculatingResponse.value = true
            try {
                val res = chatApi.getResponse(messages)
                messages.addAll(res)
            } catch (e: Exception) {
                messages.add(
                    Message(
                        "Vaya, parece que no tienes conexión a internet",
                        false,
                        Date(),
                    )
                )
            }
            calculatingResponse.value = false
        }
        Log.d("ChatScreen", "sending message :$content")
    }
}
