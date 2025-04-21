package pl.konnekt.network

import pl.konnekt.models.*
import retrofit2.http.*

interface KonnektApiService {
    @GET("users/{userId}")
    suspend fun getUserProfile(@Path("userId") userId: String): User

    @GET("users")
    suspend fun getAllUsers(): List<User>

    @POST("login")
    suspend fun login(@Body loginRequest: Map<String, String>): LoginResponse

    @POST("register")
    suspend fun register(@Body userCreate: UserCreate): UserResponse

    @POST("users/{userId}/follow")
    suspend fun followUser(@Path("userId") userId: String): User

    @DELETE("users/{userId}/unfollow")
    suspend fun unfollowUser(@Path("userId") userId: String): User

    @GET("users/{userId}/followers")
    suspend fun getUserFollowers(@Path("userId") userId: String): List<User>

    @GET("users/{userId}/following")
    suspend fun getUserFollowing(@Path("userId") userId: String): List<User>

    @PUT("users/{userId}")
    suspend fun updateUserProfile(
        @Path("userId") userId: String,
        @Body updateRequest: Map<String, String>
    ): User

    @GET("messages/{chatId}")
    suspend fun getMessages(@Path("chatId") chatId: String): List<Message>
}