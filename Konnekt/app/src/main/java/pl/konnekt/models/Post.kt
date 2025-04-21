package pl.konnekt.models

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import java.io.Serializable


data class Post(
    val id: String,
    val imageUrl: String,
    val description: String,
    val user: User,
    val createdAt: String

): Serializable

@Composable
fun PostItem(post: Post) {
    Column(
        modifier = Modifier
            .fillMaxWidth()

    ) {
        topPart(post.user)
        AsyncImage(
            model = post.imageUrl,
            contentDescription = "Imagen de ${post.user.username}",
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp)
        )
        Text(text = post.description, modifier = Modifier.padding(start = 8.dp), fontSize = 12.sp)
    }
}

@Composable
fun topPart(user: User) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(8.dp)
    ) {
        AsyncImage(
            model = user.profileImageUrl,
            contentDescription = "Imagen de ${user.username}",
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .border(1.dp, Color.Gray, CircleShape)
        )
        Text(
            text = user.username,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
