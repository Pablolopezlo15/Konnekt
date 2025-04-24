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

    private fun createTempFileFromUri(context: Context, uri: Uri): File {
        val stream = context.contentResolver.openInputStream(uri)
        val file = File.createTempFile("upload", ".jpg", context.cacheDir)
        FileOutputStream(file).use { output ->
            stream?.copyTo(output)
        }
        return file
    }
}