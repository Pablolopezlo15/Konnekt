package pl.konnekt.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FriendRequest(
    @SerializedName("id") val id: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("receiver_id") val receiverId: String,
    @SerializedName("status") val status: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("senderUsername") val senderUsername: String?,
    @SerializedName("senderProfileImage") val senderProfileImage: String?,
    @SerializedName("receiverUsername") val receiverUsername: String?,
    @SerializedName("receiverProfileImage") val receiverProfileImage: String?
) : Serializable {
    fun getFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(createdAt.substring(0, 19))
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            createdAt
        }
    }
}