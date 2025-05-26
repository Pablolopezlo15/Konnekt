package pl.konnekt.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import pl.konnekt.models.Comment
import pl.konnekt.models.Post
import pl.konnekt.network.KonnektApi
import pl.konnekt.network.UnsafeOkHttpClient
import pl.konnekt.utils.ImageUploader
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HostnameVerifier
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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
                // El nombre del archivo no es relevante ya que el servidor lo renombrará
                val imagePart = MultipartBody.Part.createFormData("image", "image.jpg", requestBody)
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

    suspend fun getPost(postId: String, context: Context): Post? {
        return try {
            _isLoading.value = true
            val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .getString("token", "") ?: ""
            val authHeader = "Bearer $token"
            KonnektApi.retrofitService.getPostDetails(authHeader, postId)

        } catch (e: Exception) {
            _error.value = e.message
            null
        } finally {
            _isLoading.value = false
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

    
    private val _isGeneratingComment = MutableStateFlow(false)
    val isGeneratingComment: StateFlow<Boolean> = _isGeneratingComment.asStateFlow()
    
    @OptIn(ExperimentalEncodingApi::class)
    fun generateAIComment(url: String, context: Context, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isGeneratingComment.value = true
                Log.d("PostViewModel", "Iniciando generación de comentario IA")
    
                // Obtener token
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                if (token.isEmpty()) {
                    _error.value = "No se encontró el token de autenticación"
                    return@launch
                }
                val authHeader = "Bearer $token"
    
                // Usar el cliente OkHttp inseguro para la conexión
                val client = UnsafeOkHttpClient.getUnsafeOkHttpClient()
                val connection = URL(url).openConnection() as HttpURLConnection
                
                // Configurar la conexión para aceptar cualquier certificado
                if (connection is javax.net.ssl.HttpsURLConnection) {
                    connection.sslSocketFactory = client.sslSocketFactory
                    connection.hostnameVerifier = client.hostnameVerifier
                }
    
                // Descargar y convertir imagen a base64
                val base64Image = withContext(Dispatchers.IO) {
                    try {
                        connection.apply {
                            connectTimeout = 15000
                            readTimeout = 15000
                            requestMethod = "GET"
                            setRequestProperty("User-Agent", "Mozilla/5.0")
                            setRequestProperty("Accept", "image/*")
                        }
    
                        Log.d("PostViewModel", "Conectando a la URL de la imagen: $url")
                        connection.connect()
    
                        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                            connection.inputStream.use { input ->
                                val bitmap = BitmapFactory.decodeStream(input)
                                ByteArrayOutputStream().use { output ->
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
                                    Base64.encode(output.toByteArray())
                                }
                            }
                        } else {
                            Log.e("PostViewModel", "Error HTTP: ${connection.responseCode}")
                            null
                        }
                    } catch (e: Exception) {
                        Log.e("PostViewModel", "Error al cargar la imagen: ${e.message}", e)
                        null
                    } finally {
                        connection.disconnect()
                    }
                }

                if (base64Image == null) {
                    _error.value = "No se pudo cargar la imagen. Verifica la URL."
                    return@launch
                }

                // Enviar solicitud a la API
                val requestBody = mapOf("url" to base64Image)
                Log.d("PostViewModel", "Enviando solicitud a la API con base64")

                val response = KonnektApi.retrofitService.generateComment(authHeader, requestBody)
                if (response.comment != null) {
                    _error.value = null
                    onSuccess(response.comment!!)
                } else {
                    _error.value = "No se recibió un comentario válido de la API"
                }

            } catch (e: Exception) {
                Log.e("PostViewModel", "Error generando comentario IA: ${e.message}", e)
                _error.value = "Error al generar comentario: ${e.message}"
            } finally {
                _isGeneratingComment.value = false
            }
        }
    }

    private val _savedPosts = MutableStateFlow<List<Post>>(emptyList())
    val savedPosts: StateFlow<List<Post>> = _savedPosts.asStateFlow()

    fun savePost(postId: String, context: Context, callback: (Boolean, Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
                
                val response = KonnektApi.retrofitService.savePost(authHeader, postId)
                val isSaved = response["message"] == "Post guardado exitosamente"
                
                // Actualizar el estado del post en la lista
                _posts.value = _posts.value.map { post ->
                    if (post.id == postId) {
                        post.copy(isSaved = isSaved)
                    } else post
                }
                
                callback(true, isSaved)
            } catch (e: Exception) {
                _error.value = "Error al guardar el post: ${e.message}"
                callback(false, false)
            }
        }
    }

    fun getSavedPosts(context: Context, userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
                
                val savedPosts = KonnektApi.retrofitService.getSavedPosts(authHeader, userId)
                _savedPosts.value = savedPosts
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getSavedPosts(userId: String, context: Context) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
                
                val savedPosts = KonnektApi.retrofitService.getSavedPosts(authHeader, userId)
                _savedPosts.value = savedPosts
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePost(postId: String, context: Context, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                    .getString("token", "") ?: ""
                val authHeader = "Bearer $token"
                
                KonnektApi.retrofitService.deletePost(authHeader, postId)
                
                // Actualizar la lista de posts eliminando el post borrado
                _posts.value = _posts.value.filter { it.id != postId }
                
                onSuccess()
            } catch (e: Exception) {
                _error.value = "Error al borrar el post: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}