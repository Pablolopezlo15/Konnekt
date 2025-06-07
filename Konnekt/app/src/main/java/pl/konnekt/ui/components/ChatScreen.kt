package pl.konnekt.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import pl.konnekt.models.Message
import pl.konnekt.models.User
import pl.konnekt.viewmodel.ChatViewModel
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Calendar

@Composable
fun ChatScreen(
    viewModel: ChatViewModel, 
    recipientId: String, 
    userId: String, 
    recipientName: String,
    navController: NavController
) {
    var isInitialLoadDone by remember { mutableStateOf(false) }
    val messages by viewModel.messages.collectAsState(initial = emptyList())
    val isConnected by viewModel.isConnected.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState(initial = null)
    var messageText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!isInitialLoadDone) {
            Log.d("ChatScreen", "Loading messages for chat with: $recipientId")
            viewModel.loadInitialMessages("${userId}_${recipientId}")
            isInitialLoadDone = true
        }
    }

    val groupedMessages = remember(messages) {
        messages.sortedBy { message ->
            try {
                val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                inputFormat.parse(message.timestamp)?.time ?: 0
            } catch (e: Exception) {
                0
            }
        }.groupBy { message ->
            try {
                val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.US).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val date = inputFormat.parse(message.timestamp)
                val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getDefault()
                }
                outputFormat.format(date)
            } catch (e: Exception) {
                "unknown"
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        MyTopBar(
            title = recipientName,
            onBackClick = { navController.popBackStack() },
            onChatsClick = { navController.navigate("userList") }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    reverseLayout = true
                ) {
                    groupedMessages.entries.reversed().forEach { (dateKey, messagesForDate) ->
                        items(messagesForDate.reversed()) { message ->
                            val isSentByUser = message.sender_id == userId
                            MessageItem(message, isSentByUser)
                        }
                        
                        item(key = "date_$dateKey") {
                            DateSeparator(dateKey)
                        }
                    }
                }

                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (!isConnected) {
                Text(
                    text = "Desconectado del chat",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            if (messageText.isEmpty()) {
                                Text("Escribe un mensaje...", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    }
                )
                Button(
                    onClick = {
                        Log.d("ChatScreen", "Sending message: $messageText")
                        if (messageText.isNotBlank()) {
                            viewModel.sendMessage(
                                Message(
                                    chat_id = "",
                                    sender_id = "",
                                    recipient_id = "",
                                    message = messageText.trim(),
                                    timestamp = ""
                                )
                            )
                            messageText = ""
                        }
                    },
                    enabled = isConnected && messageText.isNotBlank()
                ) {
                    Text("Enviar")
                }
            }
        }
    }
}

@Composable
private fun DateSeparator(dateKey: String) {
    val displayDate = remember(dateKey) {
        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateKey)
            val today = Calendar.getInstance()
            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
            val messageDate = Calendar.getInstance().apply { time = date }

            when {
                isSameDay(today, messageDate) -> "Hoy"
                isSameDay(yesterday, messageDate) -> "Ayer"
                else -> SimpleDateFormat("dd 'de' MMMM", Locale.getDefault()).format(date)
            }
        } catch (e: Exception) {
            dateKey
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ),
            modifier = Modifier.wrapContentWidth()
        ) {
            Text(
                text = displayDate,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

@Composable
private fun MessageItem(message: Message, isSentByUser: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = if (isSentByUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isSentByUser) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant
                }
            ),
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .wrapContentWidth()
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = message.message,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = try {
                        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.US).apply {
                            timeZone = java.util.TimeZone.getTimeZone("UTC")
                        }
                        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault()).apply {
                            timeZone = java.util.TimeZone.getDefault()
                        }
                        val date = inputFormat.parse(message.timestamp)
                        outputFormat.format(date)
                    } catch (e: Exception) {
                        "00:00"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(if (isSentByUser) Alignment.End else Alignment.Start)
                )
            }
        }
    }
}