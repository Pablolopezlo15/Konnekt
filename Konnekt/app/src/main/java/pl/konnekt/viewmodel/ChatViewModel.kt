// ChatViewModel.kt
package pl.konnekt.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import pl.konnekt.models.Message
import pl.konnekt.repository.ChatRepository
import java.net.URI
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.delay
import pl.konnekt.network.KonnektApi
import pl.konnekt.config.AppConfig
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(
    private val currentUserId: String,
    private val recipientId: String
) : ViewModel() {
    private val repository = ChatRepository(
        URI(AppConfig.WEBSOCKET_URI),
        currentUserId
    )
    private val gson = Gson().newBuilder()
        .setDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy")
        .create()

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    val messages: StateFlow<List<Message>> = _messages.asStateFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        setupWebSocketListeners()
        startConnectionMonitoring()
        val chatId = createChatId(currentUserId, recipientId)
        loadInitialMessages(chatId)
    }

    private fun createChatId(userId1: String, userId2: String): String {
        return listOf(userId1, userId2).sorted().joinToString("_")
    }

    fun loadInitialMessages(chatId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val sortedChatId = createChatId(currentUserId, recipientId)
                Log.d("ChatViewModel", "Loading messages for chat: $sortedChatId")

                val loadedMessages = KonnektApi.retrofitService.getMessages(sortedChatId)
                _messages.value = loadedMessages
                Log.d("ChatViewModel", "Loaded ${loadedMessages.size} messages")
                
            } catch (e: Exception) {
                _error.value = "Error loading messages: ${e.message}"
                Log.e("ChatViewModel", "Error loading messages", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun setupWebSocketListeners() {
        viewModelScope.launch(Dispatchers.Main) {
            try {
                repository.messagesFlow.collectLatest { message ->
                    Log.d("ChatViewModel", "📩 WebSocket message received in ViewModel: $message")
                    val expectedChatId = createChatId(currentUserId, recipientId)
                    Log.d("ChatViewModel", "🔍 Checking chat IDs - Expected: $expectedChatId, Received: ${message.chat_id}")
                    
                    if (message.chat_id == expectedChatId) {
                        Log.d("ChatViewModel", "✅ Message belongs to this chat, adding: $message")
                        val currentMessages = _messages.value.toMutableList()
                        currentMessages.add(message)
                        _messages.value = currentMessages
                        Log.d("ChatViewModel", "📊 Total messages now: ${currentMessages.size}")
                    } else {
                        Log.d("ChatViewModel", "⚠️ Message from different chat ignored: ${message.chat_id}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "❌ Error in WebSocket listener", e)
            }
        }
    }

    fun sendMessage(message: Message) {
        viewModelScope.launch {
            if (_isConnected.value) {
                val chatId = createChatId(currentUserId, recipientId)
                val timestamp = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.US).format(Date())
                Log.d("ChatViewModel", "Sending message with text: ${message.message}")
                // Send through WebSocket with timestamp
                repository.sendMessage(recipientId, message.message)
            } else {
                Log.e("ChatViewModel", "Cannot send message: WebSocket not connected")
                _error.value = "No se puede enviar el mensaje: conexión perdida"
            }
        }
    }

    private fun startConnectionMonitoring() {
        viewModelScope.launch {
            while (true) {
                try {
                    _isConnected.value = repository.webSocketClient?.isOpen ?: false
                } catch (e: Exception) {
                    _isConnected.value = false
                }
                delay(1000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}