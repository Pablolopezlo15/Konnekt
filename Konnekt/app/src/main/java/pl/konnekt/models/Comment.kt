package pl.konnekt.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class Comment(
    @SerializedName("_id") val id: String,
    @SerializedName("post_id") val postId: String,
    @SerializedName("user_id") val authorId: String,
    @SerializedName("username") val authorUsername: String,
    @SerializedName("comment") val content: String,
    @SerializedName("timestamp") val timestamp: String
) : Serializable