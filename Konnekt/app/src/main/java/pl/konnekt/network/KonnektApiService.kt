package pl.konnekt.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.w3c.dom.Comment
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
    suspend fun getUserPosts(@Path("userId") userId: String): List<Post>

    @POST("posts/{postId}/like")
    suspend fun likePost(
        @Header("Authorization") authorization: String,
        @Path("postId") postId: String
    ): Map<String, String>

    @POST("posts/{postId}/comments")
    suspend fun addComment(
        @Path("postId") postId: String,
        @Body comment: Map<String, String>
    ): Comment

    @GET("posts/{postId}/comments")
    suspend fun getPostComments(@Path("postId") postId: String): List<Comment>

    @GET("posts")
    suspend fun getAllPosts(): List<Post>
}