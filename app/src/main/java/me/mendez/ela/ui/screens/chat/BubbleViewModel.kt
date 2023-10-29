package me.mendez.ela.ui.screens.chat

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
import kotlinx.coroutines.runBlocking
import me.mendez.ela.chat.Message
import me.mendez.ela.persistence.database.chats.MessageDao
import me.mendez.ela.persistence.settings.ElaSettings
import me.mendez.ela.chat.ChatApi
import me.mendez.ela.chat.Sender


class BubbleViewModel @AssistedInject constructor(
    private val chatApi: ChatApi,
    private val messagesDao: MessageDao,
    var elaSettingsStore: DataStore<ElaSettings>,
    @Assisted
    val domain: String,
    @Assisted
    val conversation: Long,
) : ViewModel() {
    val messages = messagesDao
        .getMessages(conversation)

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

            // add question to database
            val userQuestion = Message(content, Sender.USER)
            messagesDao.addMessage(conversation, userQuestion)

            calculatingResponse.value = true

            // query question
            val latestConversation = messages
                .toMutableList()
                .apply { add(userQuestion) }

            var response = chatApi.answer(latestConversation)
            if (response.isNullOrEmpty())
                response = Message.noInternetMessage()

            messagesDao.addMessages(conversation, response)

            calculatingResponse.value = false
        }
    }

    fun addToWhitelist(state: Boolean) {
        if (state) {
            runBlocking {
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
        fun create(domain: String, conversation: Long): BubbleViewModel
    }

    companion object {
        fun provideBubbleFactory(factory: Factory, domain: String, conversation: Long): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return factory.create(domain, conversation) as T
                }
            }
        }
    }
}
