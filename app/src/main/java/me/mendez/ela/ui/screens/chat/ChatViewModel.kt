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
import me.mendez.ela.chat.ChatApi
import java.util.*
import javax.inject.Inject

private const val TAG = "ELA_BUBBLE"

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
            Log.i(TAG, "sending message $content")
            calculatingResponse.value = true
            val res = chatApi.answer(messages)

            if (res.isEmpty()) {
                messages.add(
                    Message(
                        "Vaya, parece que no tienes conexión a internet",
                        false,
                        Date(),
                    )
                )
            } else {
                messages.addAll(res)
            }

            calculatingResponse.value = false
        }
    }
}
