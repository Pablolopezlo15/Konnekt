package pl.konnekt.models

import android.util.Log
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
    @SerializedName("likes_count") val likesCount: Int,
    @SerializedName("comments_count") val commentsCount: Int,
    @SerializedName("is_liked") val isLiked: Boolean = false
): Serializable

@Composable
fun PostItem(
    post: Post,
    onLikeClick: (String) -> Unit = {},
    onCommentClick: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val imageLoader = remember { CoilConfig.getImageLoader(context) }
    
    // Format timestamp
    val formattedDate = remember(post.timestamp) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(post.timestamp)
            outputFormat.format(date)
        } catch (e: Exception) {
            post.timestamp
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
                model = "${AppConfig.BASE_URL}${post.imageUrl}",
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
        AsyncImage(
            model = "${AppConfig.BASE_URL}${post.imageUrl}",
            contentDescription = "Post image by ${post.authorUsername}",
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
            text = post.caption,
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
            // Update the like button
            IconButton(onClick = { onLikeClick(post.id) }) {
                Icon(
                    imageVector = if (post.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (post.isLiked) Color.Red else LocalContentColor.current
                )
            }
            IconButton(onClick = { onCommentClick(post.id) }) {
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
                text = "${post.likesCount} likes",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = "${post.commentsCount} comments",
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
