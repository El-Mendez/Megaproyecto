package me.mendez.ela.ui.screens.chat

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.mendez.ela.chat.Message
import me.mendez.ela.chat.ChatApi
import me.mendez.ela.chat.Sender
import me.mendez.ela.persistence.database.chat.ChatDao
import java.util.*
import javax.inject.Inject

private const val TAG = "ELA_BUBBLE"

@HiltViewModel
class ChatViewModel @Inject constructor(
    val chatApi: ChatApi,
    val chatDao: ChatDao,
) : ViewModel() {
    val messages = chatDao.getAll()
    val calculatingResponse = mutableStateOf(false)

    fun sendMessage(content: String) {
        viewModelScope.launch {
            val newMessage = Message(content, Sender.USER)

            val messages = chatDao
                .getAll()
                .first()
                .toMutableList()
                .apply { add(newMessage) }
                .toMutableList()

            chatDao.add(newMessage)

            Log.i(TAG, "sending message $content")
            calculatingResponse.value = true
            val res = chatApi.answer(messages)

            if (res.isNullOrEmpty()) {
                chatDao.addAll(Message.noInternetMessage())
            } else {
                chatDao.addAll(res)
            }

            calculatingResponse.value = false
        }
    }
}
