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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import pl.konnekt.config.AppConfig
import pl.konnekt.utils.CoilConfig
import java.text.SimpleDateFormat
import java.util.Locale

data class Post(
    @SerializedName("id") val id: String,
    @SerializedName("author_id") val authorId: String,
    @SerializedName("author_username") val authorUsername: String,
    @SerializedName("image_url") val imageUrl: String,
    @SerializedName("caption") val caption: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("likes_count") var likesCount: Int,
    @SerializedName("comments_count") val commentsCount: Int,
    @SerializedName("is_liked") var isLiked: Boolean = false
): Serializable

@Composable
fun PostItem(
    post: Post,
    onLikeClick: (String) -> Unit = {},
    onCommentClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val imageLoader = remember { CoilConfig.getImageLoader(context) }
    
    // Track post state with both id and isLiked to ensure updates
    var currentPost by remember(post.id, post.isLiked) { mutableStateOf(post) }
    
    // Update local state when post changes
    LaunchedEffect(post.id, post.isLiked, post.likesCount) {
        if (currentPost.isLiked != post.isLiked || currentPost.likesCount != post.likesCount) {
            currentPost = post
        }
    }

    // Format timestamp
    val formattedDate = remember(currentPost.timestamp) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(currentPost.timestamp)
            outputFormat.format(date)
        } catch (e: Exception) {
            currentPost.timestamp
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
                model = "${AppConfig.BASE_URL}${currentPost.imageUrl}",  // Changed from post to currentPost
                contentDescription = "Profile picture of ${currentPost.authorUsername}",  // Changed from post to currentPost
                imageLoader = imageLoader,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color.Gray, CircleShape)
            )
            Text(
                text = currentPost.authorUsername,  // Changed from post to currentPost
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Post image
        AsyncImage(
            model = "${AppConfig.BASE_URL}${currentPost.imageUrl}",  // Changed from post to currentPost
            contentDescription = "Post image by ${currentPost.authorUsername}",  // Changed from post to currentPost
            imageLoader = imageLoader,
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            contentScale = ContentScale.Crop,
            onError = { 
                Log.e("PostItem", "Error loading image: ${it.result.throwable.message}")
                Log.e("PostItem", "URL: ${AppConfig.BASE_URL}${post.imageUrl}")
            },
            onLoading = { Log.d("PostItem", "Loading image...") },
            onSuccess = { Log.d("PostItem", "Image loaded successfully") }
        )

        // Caption
        Text(
            text = currentPost.caption,  // Changed from post to currentPost
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            fontSize = 14.sp
        )

        // Actions row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { 
                    Log.d("PostItem", "Like clicked - Before: isLiked=${currentPost.isLiked}")
                    // Update local state immediately for better UX
                    currentPost = currentPost.copy(
                        isLiked = !currentPost.isLiked,
                        likesCount = currentPost.likesCount + if (!currentPost.isLiked) 1 else -1
                    )
                    onLikeClick(currentPost.id)
                }
            ) {
                Icon(
                    imageVector = if (currentPost.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (currentPost.isLiked) "Unlike" else "Like",
                    tint = if (currentPost.isLiked) Color(0xFFE91E63) else Color.Gray,
                    modifier = Modifier.animateContentSize()
                )
            }
            IconButton(onClick = { onCommentClick(currentPost.id) }) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Comment"
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
                text = "${currentPost.likesCount} likes",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "${currentPost.commentsCount} comments",
                fontSize = 14.sp
            )
        }

        // Update the timestamp display
        Text(
            text = formattedDate,
            modifier = Modifier.padding(horizontal = 8.dp),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
