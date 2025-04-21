package pl.konnekt.models

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Message(
    val chat_id: String,
    val sender_id: String,
    val recipient_id: String,
    val message: String,
    val timestamp: String
)

// Add this extension function to the Message class
fun Message.getFormattedTime(): String {
    return try {
        val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT' yyyy", Locale.US)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        outputFormat.format(date)
    } catch (e: Exception) {
        "00:00" // Fallback if parsing fails
    }
}
