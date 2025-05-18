package pl.konnekt.utils

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import pl.konnekt.network.KonnektApi
import java.io.File
import java.io.FileOutputStream

object ImageUploader {
    suspend fun uploadImage(context: Context, imageUri: Uri): String {
        return withContext(Dispatchers.IO) {
            val file = createTempFileFromUri(context, imageUri)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("image", file.name, requestBody)
            
            try {
                val response = KonnektApi.retrofitService.uploadImage(part)
                response.imageUrl
            } finally {
                file.delete()
            }
        }
    }

    suspend fun uploadImageProfile(context: Context, imageUri: Uri, idUser: String): String {
        return withContext(Dispatchers.IO) {
            val file = createTempFileFromUriProfile(context, imageUri, idUser)
            val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
            // Use user ID in the form data name for server-side identification
            val part = MultipartBody.Part.createFormData("profile_image", "profile_${idUser}.jpg", requestBody)
            
            try {
                val response = KonnektApi.retrofitService.uploadImage(part)
                response.imageUrl
            } finally {
                file.delete()
            }
        }
    }

    fun createTempFileFromUriProfile(context: Context, uri: Uri, idUser: String): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        // Create temp file with user ID in name
        val tempFile = File.createTempFile("profile_${idUser}_", ".jpg", context.cacheDir)
        
        FileOutputStream(tempFile).use { outputStream ->
            inputStream?.use { input ->
                input.copyTo(outputStream)
            }
        }
        
        return tempFile
    }
    fun createTempFileFromUri(context: Context, uri: Uri): File {
        val inputStream = context.contentResolver.openInputStream(uri)
        val tempFile = File.createTempFile("upload_", ".jpg", context.cacheDir)
        
        FileOutputStream(tempFile).use { outputStream ->
            inputStream?.use { input ->
                input.copyTo(outputStream)
            }
        }
        
        return tempFile
    }
}