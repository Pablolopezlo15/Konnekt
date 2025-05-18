package pl.konnekt.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class User(
    @SerializedName("id") val id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("profile_image_url") val profileImageUrl: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("birth_date") val birthDate: String? = null,
    @SerializedName("followers") val followers: List<String> = emptyList(),
    @SerializedName("following") val following: List<String> = emptyList(),
    @SerializedName("private_account") val private_account: Boolean = false
) : Serializable

fun createPabloUser() = User(
    id = "user123",
    username = "pablo",
    profileImageUrl = "https://picsum.photos/400/348",
    email = "pablo@example.com",
    phone = null,
    birthDate = null,
    followers = emptyList(),
    following = emptyList(),
    private_account = false
)

data class UserCreate(
    val username: String,
    val password: String,
    val email: String,
    val phone: String? = null,
    val birthDate: String? = null,
    val profileImageUrl: String? = null
)

data class UserResponse(
    @SerializedName("_id") val _id: String,
    @SerializedName("username") val username: String,
    @SerializedName("email") val email: String,
    @SerializedName("profile_image_url") val profileImageUrl: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("birth_date") val birthDate: String? = null,
    @SerializedName("followers") val followers: List<String> = emptyList(),
    @SerializedName("following") val following: List<String> = emptyList(),
    @SerializedName("private_account") val private_account: Boolean = false
)

data class LoginResponse(
    val access_token: String,
    val token_type: String
)

data class RegisterResponse(
    val access_token: String,
    val token_type: String,
    val user: UserResponse
)