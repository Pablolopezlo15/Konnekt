package pl.konnekt.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import pl.konnekt.network.KonnektApi
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

object ImageUploader {
    private const val TAG = "ImageUploader"
    private const val MAX_IMAGE_SIZE = 1024 * 1024 * 2 // 2MB
    private const val COMPRESSION_QUALITY = 80 // Calidad de compresión inicial

    sealed class ImageUploadError : Exception() {
        object InvalidImage : ImageUploadError()
        object FileTooLarge : ImageUploadError()
        object NetworkError : ImageUploadError()
        data class CompressionError(override val message: String) : ImageUploadError()
        data class ServerError(override val message: String) : ImageUploadError()
        data class UnknownError(override val cause: Throwable) : ImageUploadError()
    }

    fun createTempFileFromUri(context: Context, uri: Uri): File {
        Log.d(TAG, "Creando archivo temporal desde URI: $uri")
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw ImageUploadError.InvalidImage
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        
        try {
            // Leer la imagen como bitmap
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
                ?: throw ImageUploadError.InvalidImage
            inputStream.close()
            
            if (originalBitmap.width == 0 || originalBitmap.height == 0) {
                throw ImageUploadError.InvalidImage
            }
            
            // Calcular el factor de escala si la imagen es muy grande
            val maxDimension = 1920 // máximo 1920px en cualquier dimensión
            val scale = if (originalBitmap.width > maxDimension || originalBitmap.height > maxDimension) {
                val scaleWidth = maxDimension.toFloat() / originalBitmap.width
                val scaleHeight = maxDimension.toFloat() / originalBitmap.height
                Math.min(scaleWidth, scaleHeight)
            } else {
                1f
            }
            
            // Escalar el bitmap si es necesario
            val scaledBitmap = if (scale < 1) {
                Bitmap.createScaledBitmap(
                    originalBitmap,
                    (originalBitmap.width * scale).toInt(),
                    (originalBitmap.height * scale).toInt(),
                    true
                )
            } else {
                originalBitmap
            }
            
            var quality = COMPRESSION_QUALITY
            val outputStream = ByteArrayOutputStream()
            
            // Comprimir la imagen hasta que esté por debajo del tamaño máximo
            do {
                outputStream.reset()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                Log.d(TAG, "Intentando comprimir con calidad: $quality%, tamaño actual: ${outputStream.size()} bytes")
                quality -= 10
            } while (outputStream.size() > MAX_IMAGE_SIZE && quality > 0)
            
            if (outputStream.size() > MAX_IMAGE_SIZE) {
                throw ImageUploadError.FileTooLarge
            }
            
            Log.d(TAG, "Imagen comprimida. Tamaño final: ${outputStream.size()} bytes, Calidad: $quality%")
            
            // Guardar la imagen comprimida
            FileOutputStream(tempFile).use { fos ->
                fos.write(outputStream.toByteArray())
            }
            
            if (scaledBitmap != originalBitmap) {
                scaledBitmap.recycle()
            }
            originalBitmap.recycle()
            
            Log.d(TAG, "Archivo temporal creado exitosamente")
            return tempFile
        } catch (e: Exception) {
            Log.e(TAG, "Error al crear archivo temporal", e)
            throw e
        } catch (e: OutOfMemoryError) {
            Log.e(TAG, "Error de memoria al procesar la imagen", e)
            throw ImageUploadError.CompressionError("No hay suficiente memoria para procesar la imagen")
        } catch (e: Exception) {
            when (e) {
                is ImageUploadError -> throw e
                else -> {
                    Log.e(TAG, "Error inesperado al crear archivo temporal", e)
                    throw ImageUploadError.UnknownError(e)
                }
            }
        }
    }

    suspend fun uploadImage(context: Context, imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Iniciando upload de imagen con URI: $imageUri")
                val file = createTempFileFromUri(context, imageUri)
                
                if (!file.exists() || file.length() == 0L) {
                    throw ImageUploadError.InvalidImage
                }
                
                Log.d(TAG, "Archivo temporal creado: ${file.absolutePath}")
                Log.d(TAG, "Tamaño del archivo: ${file.length()} bytes")
                
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", file.name, requestBody)
                
                try {
                    Log.d(TAG, "Iniciando petición al servidor")
                    val response = KonnektApi.retrofitService.uploadImage(part)
                    Log.d(TAG, "Imagen subida exitosamente. URL: ${response.imageUrl}")
                    response.imageUrl
                } catch (e: Exception) {
                    Log.e(TAG, "Error al subir la imagen", e)
                    throw when {
                        e is java.net.UnknownHostException -> ImageUploadError.NetworkError
                        e.message?.contains("timeout") == true -> ImageUploadError.NetworkError
                        e.message?.contains("5") == true -> ImageUploadError.ServerError("Error en el servidor")
                        else -> ImageUploadError.UnknownError(e)
                    }
                } finally {
                    file.delete()
                }
            } catch (e: Exception) {
                when (e) {
                    is ImageUploadError -> throw e
                    else -> throw ImageUploadError.UnknownError(e)
                }
            }
        }
    }

    suspend fun uploadImageProfile(context: Context, imageUri: Uri, userId: String): String {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Iniciando upload de imagen de perfil con URI: $imageUri")
                val file = createTempFileFromUri(context, imageUri)
                
                if (!file.exists() || file.length() == 0L) {
                    throw ImageUploadError.InvalidImage
                }
                
                Log.d(TAG, "Archivo temporal creado: ${file.absolutePath}")
                Log.d(TAG, "Tamaño del archivo: ${file.length()} bytes")
                
                val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("image", file.name, requestBody)
                
                try {
                    Log.d(TAG, "Iniciando petición al servidor")
                    // Obtener el token de autenticación
                    val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                        .getString("token", "") ?: throw ImageUploadError.UnknownError(Exception("No token found"))
                        
                    val response = KonnektApi.retrofitService.uploadProfileImage(
                        authorization = "Bearer $token",
                        image = part,
                        userId = userId
                    )
                    Log.d(TAG, "Imagen de perfil subida exitosamente. URL: ${response.imageUrl}")
                    response.imageUrl
                } catch (e: Exception) {
                    Log.e(TAG, "Error al subir la imagen de perfil", e)
                    throw when {
                        e is java.net.UnknownHostException -> ImageUploadError.NetworkError
                        e.message?.contains("timeout") == true -> ImageUploadError.NetworkError
                        e.message?.contains("5") == true -> ImageUploadError.ServerError("Error en el servidor")
                        else -> ImageUploadError.UnknownError(e)
                    }
                } finally {
                    Log.d(TAG, "Eliminando archivo temporal")
                    file.delete()
                }
            } catch (e: Exception) {
                when (e) {
                    is ImageUploadError -> throw e
                    else -> throw ImageUploadError.UnknownError(e)
                }
            }
        }
    }
}