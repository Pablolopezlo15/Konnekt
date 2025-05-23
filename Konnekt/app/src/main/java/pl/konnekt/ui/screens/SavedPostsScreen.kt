package pl.konnekt.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import pl.konnekt.config.AppConfig
import pl.konnekt.models.Post
import pl.konnekt.models.PostItem
import pl.konnekt.viewmodel.PostViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import pl.konnekt.ui.components.LocalNavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedPostsScreen(
    userId: String,
    viewModel: PostViewModel = viewModel(),
    onPostClick: (Post) -> Unit
) {
    val context = LocalContext.current
    val navController = LocalNavController.current
    val posts by viewModel.savedPosts.collectAsState()
    var selectedPost by remember { mutableStateOf<Post?>(null) }

    LaunchedEffect(Unit) {
        viewModel.getSavedPosts(context, userId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Text(
            text = "Posts Guardados",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth(),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(posts) { post ->
                Surface(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp)),
                    tonalElevation = 2.dp,
                    shadowElevation = 2.dp
                ) {
                    AsyncImage(
                        model = "${AppConfig.BASE_URL}${post.imageUrl}",
                        contentDescription = "Post image",
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { selectedPost = post },
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }

    selectedPost?.let { post ->
        BasicAlertDialog(
            onDismissRequest = { selectedPost = null },
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
            content = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                ) {
                    PostItem(
                        post = post,
                        onLikeClick = { postId, isLiked, callback ->
                            viewModel.likePost(postId, context, isLiked, callback)
                        },
                        onCommentClick = { postId -> 
                            navController.navigate("post/$postId/comments")
                        },
                        onSaveClick = { postId, isSaved, callback ->
                            viewModel.savePost(postId, context, callback)
                        }
                    )
                }
            }
        )
    }
}