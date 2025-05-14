package pl.konnekt.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pl.konnekt.models.Post
import pl.konnekt.network.KonnektApi
import java.io.File
import pl.konnekt.utils.ImageUploader

class PostViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun createPost(context: Context, imageUri: Uri, caption: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Get token and add Bearer prefix
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
                
                val file = ImageUploader.createTempFileFromUri(context, imageUri)
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)
                val captionPart = caption.toRequestBody("text/plain".toMediaTypeOrNull())

                val post = KonnektApi.retrofitService.createPost(authHeader, captionPart, imagePart)
                _posts.value = listOf(post) + _posts.value
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserPosts(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val userPosts = KonnektApi.retrofitService.getUserPosts(userId)
                _posts.value = userPosts
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likePost(postId: String, context: Context) {
        viewModelScope.launch {
            try {
                // Get token and add Bearer prefix
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
                
                // Make API call with authorization header
                val response = KonnektApi.retrofitService.likePost(authHeader, postId)
                
                // Update post in the list
                _posts.value = _posts.value.map { post ->
                    if (post.id == postId) {
                        post.copy(likesCount = if (response["message"] == "Post liked") 
                            post.likesCount + 1 
                        else 
                            post.likesCount - 1
                        )
                    } else post
                }
            } catch (e: Exception) {
                _error.value = "Error liking post: ${e.message}"
            }
        }
    }

    fun getAllPosts(context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"

                val allPosts = KonnektApi.retrofitService.getAllPosts(authHeader)
                _posts.value = allPosts
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}