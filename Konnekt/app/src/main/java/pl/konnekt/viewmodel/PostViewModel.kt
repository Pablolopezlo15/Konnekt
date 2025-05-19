package pl.konnekt.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
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
import pl.konnekt.models.Comment
import pl.konnekt.models.Post
import pl.konnekt.network.KonnektApi
import pl.konnekt.utils.ImageUploader

class PostViewModel : ViewModel() {
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun createPost(context: Context, imageUri: Uri, caption: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                if (token.isEmpty()) {
                    _error.value = "No se encontró el token de autenticación"
                    return@launch
                }
                
                val authHeader = "Bearer $token"
                
                val file = ImageUploader.createTempFileFromUri(context, imageUri)
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("image", file.name, requestBody)
                val captionPart = caption.toRequestBody("text/plain".toMediaTypeOrNull())

                try {
                    val post = KonnektApi.retrofitService.createPost(authHeader, captionPart, imagePart)
                    _posts.value = listOf(post) + _posts.value
                } finally {
                    file.delete() // Aseguramos que el archivo temporal se elimine
                }
            } catch (e: Exception) {
                _error.value = "Error al crear la publicación: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getUserPosts(userId: String, context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
                
                val userPosts = KonnektApi.retrofitService.getUserPosts(authHeader, userId)
                _posts.value = userPosts
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun likePost(postId: String, context: Context, isLiked: Boolean, callback: (Boolean, Boolean, Int) -> Unit) {
        viewModelScope.launch {
            try {
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
                
                // Make API call with authorization header
                val response = KonnektApi.retrofitService.likePost(authHeader, postId)
                
                // Determine new state based on API response
                val newIsLiked = response["message"] == "Post liked"
                val likesCountChange = if (newIsLiked) 1 else -1

                // Update post in the list
                _posts.value = _posts.value.map { post ->
                    if (post.id == postId) {
                        post.copy(
                            isLiked = newIsLiked,
                            likesCount = post.likesCount + likesCountChange
                        )
                    } else post
                }

                // Notify caller of success
                callback(true, newIsLiked, _posts.value.find { it.id == postId }?.likesCount ?: 0)
            } catch (e: Exception) {
                _error.value = "Error liking post: ${e.message}"
                // Notify caller of failure, revert to previous state
                callback(false, isLiked, _posts.value.find { it.id == postId }?.likesCount ?: 0)
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

    fun clearPosts() {
        _posts.value = emptyList()
    }

    fun getComments(postId: String, context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
                
                Log.d("PostViewModel", "Iniciando obtención de comentarios para el post: $postId")
                Log.d("PostViewModel", "Token: $authHeader")

                val comments = KonnektApi.retrofitService.getPostComments(authHeader, postId)
                Log.d("PostViewModel", "Respuesta del servidor recibida. Número de comentarios: ${comments.size}")
                
                val processedComments = comments.mapNotNull { comment ->
                    try {
                        Log.d("PostViewModel", "Procesando comentario: ${comment.content}")
                        comment.copy(
                            id = comment.id ?: "",
                            postId = comment.postId,
                            content = comment.content ?: "",
                            authorUsername = comment.authorUsername ?: "Usuario Desconocido",
                            timestamp = comment.timestamp ?: "",
                            authorId = comment.authorId ?: ""
                        )
                    } catch (e: Exception) {
                        Log.e("PostViewModel", "Error procesando comentario", e)
                        null
                    }
                }.sortedByDescending { it.timestamp } // Ordenar comentarios por fecha descendente
                
                Log.d("PostViewModel", "Comentarios procesados: ${processedComments.size}")
                _comments.value = processedComments
                Log.d("PostViewModel", "Estado actualizado con nuevos comentarios")
                
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error al obtener comentarios", e)
                _error.value = e.message ?: "Error al cargar comentarios"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun postComment(postId: String, content: String, context: Context, callback: (Boolean, Comment?) -> Unit) {
        if (content.isBlank()) {
            _error.value = "Comment cannot be empty"
            callback(false, null)
            return
        }

        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
    
                val formData = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("comment", content)
                    .build()
    
                val comment = KonnektApi.retrofitService.addComment(authHeader, postId, formData)
                val safeComment = comment?.copy(
                    id = comment.id ?: "",
                    postId = comment.postId,
                    content = comment.content ?: "",
                    authorUsername = comment.authorUsername ?: "Usuario Desconocido",
                    timestamp = comment.timestamp ?: "",
                    authorId = comment.authorId ?: ""
                )
                
                if (safeComment != null) {
                    // Agregar el nuevo comentario al principio de la lista
                    _comments.value = listOf(safeComment) + _comments.value
                    callback(true, safeComment)
                } else {
                    throw Exception("Failed to create comment")
                }
            } catch (e: Exception) {
                Log.e("PostViewModel", "Error al publicar comentario", e)
                _error.value = e.message ?: "Error posting comment"
                callback(false, null)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearComments() {
        _comments.value = emptyList()
    }
}