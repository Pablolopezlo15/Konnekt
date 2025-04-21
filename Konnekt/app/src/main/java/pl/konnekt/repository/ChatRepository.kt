package pl.konnekt.repository

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import pl.konnekt.models.Message
import pl.konnekt.network.UnsafeWebSocketClient
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

class ChatRepository(baseUri: URI, private val currentUserId: String) {
    private val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
    private val serverUri: URI = URI("${baseUri.scheme}://${baseUri.host}:${baseUri.port}/ws/${currentUserId}")
    private val _messagesFlow: MutableSharedFlow<Message> = MutableSharedFlow()
    val messagesFlow: SharedFlow<Message> = _messagesFlow.asSharedFlow()
    private val gson: Gson = Gson()
    private val dateFormat: SimpleDateFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.US)
    var webSocketClient: UnsafeWebSocketClient? = null

    init {
        Log.d("ChatRepository", "Initializing ChatRepository with URI: $serverUri for user: $currentUserId")
        connectWebSocket()
    }

    private fun connectWebSocket() {
        webSocketClient = UnsafeWebSocketClient(
            serverUri = serverUri,
            onOpen = {
                Log.d("ChatRepository", "🔌 WebSocket Connected")
            },
            onMessage = { message: String ->
                Log.d("ChatRepository", "Message Received: $message")
                try {
                    val receivedMessage: Message = gson.fromJson(message, Message::class.java)
                    coroutineScope.launch {
                        _messagesFlow.emit(receivedMessage)
                    }
                } catch (e: Exception) {
                    Log.e("ChatRepository", "Error parsing message", e)
                }
            },
            onClose = { code: Int, reason: String, remote: Boolean ->
                Log.d("ChatRepository", "WebSocket Closed - Code: $code, Reason: $reason, Remote: $remote")
            },
            onError = { error: Exception ->
                Log.e("ChatRepository", "❌ WebSocket Error", error)
            }
        ).apply {
            connect()
        }
    }
    // Generar chat_id dinámicamente como en el backend
    private fun generateChatId(userId1: String, userId2: String): String {
        val sortedIds = listOf(userId1, userId2).sorted()
        return "${sortedIds[0]}_${sortedIds[1]}"
    }

    suspend fun sendMessage(recipientId: String, messageText: String) {
        try {
            val messageToSend = mapOf(
                "recipient_id" to recipientId,
                "message" to messageText
            )
            val jsonMessage = gson.toJson(messageToSend)
            Log.d("ChatRepository", "Sending message: $jsonMessage")
            webSocketClient?.send(jsonMessage)
        } catch (e: Exception) {
            Log.e("ChatRepository", "Error sending message", e)
        }
    }
}