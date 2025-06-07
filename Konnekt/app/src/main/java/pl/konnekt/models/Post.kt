package pl.konnekt.models

import android.util.Log
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName
import pl.konnekt.config.AppConfig
import pl.konnekt.utils.CoilConfig
import java.text.SimpleDateFormat
import java.util.Locale
import pl.konnekt.R
import coil.request.ImageRequest
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import java.io.Serializable
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.ZoneId
import java.time.LocalDateTime
import java.time.format.DateTimeParseException

data class Post(
    @SerializedName("id") val id: String,
    @SerializedName("author_id") val authorId: String,
    @SerializedName("author_username") val authorUsername: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("caption") val caption: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("likes_count") var likesCount: Int,
    @SerializedName("comments_count") val commentsCount: Int,
    @SerializedName("is_liked") var isLiked: Boolean = false,
    @SerializedName("is_saved") var isSaved: Boolean = false
) : Serializable

@Composable
fun PostItem(
    post: Post,
    onLikeClick: (String, Boolean, (Boolean, Boolean, Int) -> Unit) -> Unit = { _, _, _ -> },
    onCommentClick: (String) -> Unit = {},
    onSaveClick: (String, Boolean, (Boolean, Boolean) -> Unit) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val imageLoader = remember { CoilConfig.getImageLoader(context) }
    var imageLoadError by remember { mutableStateOf(false) }
    val defaultImage = painterResource(id = R.drawable.default_post_image)

    // Local state for immediate feedback
    var isLikedLocal by remember(post.id) { mutableStateOf(post.isLiked) }
    var likesCountLocal by remember(post.id) { mutableStateOf(post.likesCount) }
    var isProcessingLike by remember { mutableStateOf(false) }

    // Sync with global state when post changes
    LaunchedEffect(post.id, post.isLiked, post.likesCount) {
        if (isLikedLocal != post.isLiked || likesCountLocal != post.likesCount) {
            isLikedLocal = post.isLiked
            likesCountLocal = post.likesCount
        }
    }

    // Format timestamp with correct timezone
    val formattedDate = remember(post.timestamp) {
        try {
            // Parse as UTC and convert to local timezone
            val localDateTime = if (post.timestamp.endsWith("Z") || post.timestamp.contains("+")) {
                // Already has timezone info
                ZonedDateTime.parse(post.timestamp).withZoneSameInstant(ZoneId.systemDefault())
            } else {
                // Assume UTC and add Z
                val utcDateTime = LocalDateTime.parse(post.timestamp, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                utcDateTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneId.systemDefault())
            }
            
            val now = ZonedDateTime.now()
            val duration = java.time.Duration.between(localDateTime, now)
            
            when {
                duration.toMinutes() < 1 -> "Ahora"
                duration.toMinutes() < 60 -> "${duration.toMinutes()}m"
                duration.toHours() < 24 -> "${duration.toHours()}h"
                duration.toDays() < 7 -> "${duration.toDays()}d"
                else -> localDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"))
            }
        } catch (e: DateTimeParseException) {
            Log.e("PostItem", "Error parsing timestamp: ${post.timestamp}", e)
            // Fallback to original SimpleDateFormat but with UTC timezone
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
                    timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
                val date = inputFormat.parse(post.timestamp)
                outputFormat.format(date)
            } catch (e: Exception) {
                post.timestamp
            }
        } catch (e: Exception) {
            Log.e("PostItem", "Error formatting timestamp: ${post.timestamp}", e)
            post.timestamp
        }
    }

    // Handle like action
    val onLikeAction = {
        if (!isProcessingLike) {
            isProcessingLike = true
            val newIsLiked = !isLikedLocal
            Log.d("PostItem", "Before like: isLiked=$isLikedLocal, likesCount=$likesCountLocal")
            isLikedLocal = newIsLiked
            likesCountLocal += if (newIsLiked) 1 else -1
            onLikeClick(post.id, newIsLiked) { success, serverIsLiked, serverLikesCount ->
                if (!success) {
                    // Revert state on failure
                    isLikedLocal = !newIsLiked
                    likesCountLocal = serverLikesCount
                } else {
                    // Sync with server state
                    isLikedLocal = serverIsLiked
                    likesCountLocal = serverLikesCount
                }
                isProcessingLike = false
                Log.d("PostItem", "After like: success=$success, isLiked=$isLikedLocal, likesCount=$likesCountLocal")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        // User header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            AsyncImage(
                model = "${AppConfig.BASE_URL}/uploads/${post.authorId}.jpg",
                contentDescription = "Profile picture of ${post.authorUsername}",
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
            )
            Text(
                text = post.authorUsername,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Post image
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = { _ -> onLikeAction() }
                    )
                }
        ) {
            var isLoading by remember { mutableStateOf(true) }

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(if (!imageLoadError) "${AppConfig.BASE_URL}${post.imageUrl}" else null)
                    .crossfade(true)
                    .build(),
                contentDescription = "Post image by ${post.authorUsername}",
                imageLoader = imageLoader,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = defaultImage,
                onLoading = { isLoading = true },
                onSuccess = { isLoading = false },
                onError = { 
                    Log.e("PostItem", "Error loading image: ${it.result.throwable.message}")
                    imageLoadError = true
                    isLoading = false
                }
            )
            
            if (isLoading && !imageLoadError) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        // Caption
        Text(
            text = post.caption,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 14.sp
        )

        // Local state for save status
        var isSavedLocal by remember(post.id) { mutableStateOf(post.isSaved) }
        var isProcessingSave by remember { mutableStateOf(false) }

        // Sync with global state when post changes
        LaunchedEffect(post.id, post.isSaved) {
            if (isSavedLocal != post.isSaved) {
                isSavedLocal = post.isSaved
            }
        }

        // Actions row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { 
                    if (!isProcessingLike) {
                        onLikeAction()
                    }
                }
            ) {
                Icon(
                    imageVector = if (isLikedLocal) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isLikedLocal) "Unlike" else "Like",
                    tint = if (isLikedLocal) Color(0xFFE91E63) else Color.Gray,
                    modifier = Modifier.animateContentSize()
                )
            }
            IconButton(onClick = { onCommentClick(post.id) }) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Comment"
                )
            }
            IconButton(
                onClick = { 
                    if (!isProcessingSave) {
                        isProcessingSave = true
                        val newIsSaved = !isSavedLocal
                        isSavedLocal = newIsSaved
                        onSaveClick(post.id, newIsSaved) { success, serverIsSaved ->
                            if (!success) {
                                isSavedLocal = !newIsSaved
                            } else {
                                isSavedLocal = serverIsSaved
                            }
                            isProcessingSave = false
                        }
                    }
                }
            ) {
                Icon(
                    imageVector = if (isSavedLocal) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = if (isSavedLocal) "Unsave" else "Save",
                    tint = if (isSavedLocal) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier.animateContentSize()
                )
            }
        }

        // Likes and comments count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "$likesCountLocal likes",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "${post.commentsCount} comments",
                fontSize = 14.sp
            )
        }

        // Timestamp
        Text(
            text = formattedDate,
            modifier = Modifier.padding(horizontal = 8.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}