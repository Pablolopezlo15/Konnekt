package pl.konnekt.models

import com.google.gson.annotations.SerializedName

data class UserListResponse(
    @SerializedName("users")
    val users: List<User>
)