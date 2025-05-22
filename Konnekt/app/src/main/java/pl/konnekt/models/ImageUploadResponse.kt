package pl.konnekt.models

import com.google.gson.annotations.SerializedName

data class ImageUploadResponse(
    @SerializedName("imageUrl") val imageUrl: String
)