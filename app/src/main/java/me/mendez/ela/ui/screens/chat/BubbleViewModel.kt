package me.mendez.ela.ui.screens.chat

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import me.mendez.ela.chat.Message
import me.mendez.ela.persistence.database.chats.MessageDao
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.remote.ChatApi


class BubbleViewModel @AssistedInject constructor(
    val chatApi: ChatApi,
    val messagesDao: MessageDao,
    var elaSettingsStore: DataStore<ElaSettings>,
    @Assisted
    val domain: String,
) : ViewModel() {
    val messages = messagesDao
        .getMessages(domain)

    val ignoreAddToWhitelist = mutableStateOf(false)
    val inWhitelist = elaSettingsStore
        .data
        .map {
            it.whitelist.contains(domain)
        }

    val calculatingResponse = mutableStateOf(false)

    fun sendMessage(content: String) {
        viewModelScope.launch {
            val messages = messages.first()

            val userQuestion = Message(content, true)
            messagesDao.addMessage(domain, userQuestion)

            calculatingResponse.value = true

            val latestConversation = messages
                .toMutableList()
                .apply { add(userQuestion) }

            try {
                val elaResponse = chatApi.getResponse(latestConversation).last()
                messagesDao.addMessage(domain, elaResponse)
            } catch (e: Exception) {
                messagesDao.addMessage(domain, Message("parece que no tienes conexión a internet", false))
            }

            calculatingResponse.value = false
        }
        Log.d("ChatScreen", "sending message :$content")
    }

    fun addToWhitelist(state: Boolean) {
        if (state) {
            viewModelScope.launch {
                elaSettingsStore.updateData {
                    it.withAddedInWhitelist(domain)
                }
            }
        } else {
            ignoreAddToWhitelist.value = true
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(domain: String): BubbleViewModel
    }

    companion object {
        fun provideBubbleFactory(factory: Factory, domain: String): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(domain) as T
                }
            }
        }
    }
}
