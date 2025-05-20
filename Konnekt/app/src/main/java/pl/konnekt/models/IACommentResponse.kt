package pl.konnekt.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class IACommentResponse (
    @SerializedName("comment")
    var comment: String? = null
): Serializable