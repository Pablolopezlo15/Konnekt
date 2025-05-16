package pl.konnekt.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
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
    suspend fun register(@Body userCreate: UserCreate): RegisterResponse

    @POST("users/{userId}/follow")
    suspend fun followUser(
        @Path("userId") userId: String,
        @Query("current_user_id") currentUserId: String
    ): User

    @POST("users/{userId}/unfollow")
    suspend fun unfollowUser(
        @Path("userId") userId: String,
        @Query("current_user_id") currentUserId: String
    ): User

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

    @GET("users/search")
    suspend fun searchUsers(@Query("username") query: String): List<User>

    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image: MultipartBody.Part
    ): ImageUploadResponse

    @Multipart
    @POST("posts")
    suspend fun createPost(
        @Header("Authorization") authorization: String,  // Changed parameter name to match backend
        @Part("caption") caption: RequestBody,
        @Part image: MultipartBody.Part
    ): Post

    @GET("posts/{userId}")
    suspend fun getUserPosts(
        @Header("Authorization") authorization: String,
        @Path("userId") userId: String
    ): List<Post>

    @DELETE("posts/{postId}")
    suspend fun deletePost(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: String
    ): Unit

    @POST("posts/{postId}/like")
    suspend fun likePost(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: String
    ): Map<String, String>

    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: String,
        @Body comment: RequestBody
    ): Comment

    @GET("posts/{postId}/comments")
    suspend fun getPostComments(
        @Header("Authorization") authHeader: String,
        @Path("postId") postId: String
    ): List<pl.konnekt.models.Comment>

    @GET("posts")
    suspend fun getAllPosts(
        @Header("Authorization") authorization: String
    ): List<Post>
}