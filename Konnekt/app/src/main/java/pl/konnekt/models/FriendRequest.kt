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
    @SerializedName("updated_at") val updatedAt: String? = null,
    @SerializedName("senderUsername") val senderUsername: String? = null,
    @SerializedName("senderProfileImage") val senderProfileImage: String? = null,
    @SerializedName("receiverUsername") val receiverUsername: String? = null,
    @SerializedName("receiverProfileImage") val receiverProfileImage: String? = null
) : Serializable {
    fun getFormattedDate(): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(createdAt)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            createdAt
        }
    }
}