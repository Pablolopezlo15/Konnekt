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
import coil.request.ImageRequest
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import pl.konnekt.R
import pl.konnekt.utils.CoilConfig
import android.util.Log

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
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            contentPadding = PaddingValues(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(posts) { post ->
                val postImageUrl = "${AppConfig.BASE_URL}${post.imageUrl}"
                Log.d("SavedPostsScreen", "Loading post image: $postImageUrl")
                
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable {
                            selectedPost = post
                        }
                ) {
                    var isLoading by remember { mutableStateOf(true) }
                    var imageLoadError by remember { mutableStateOf(false) }

                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(postImageUrl)
                            .crossfade(true)
                            .allowHardware(false)
                            .allowRgb565(true)
                            .memoryCacheKey(postImageUrl)
                            .diskCacheKey(postImageUrl)
                            .error(R.drawable.default_post_image)
                            .placeholder(R.drawable.default_post_image)
                            .build(),
                        imageLoader = CoilConfig.getImageLoader(context),
                        contentDescription = "Post image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        onLoading = { isLoading = true },
                        onSuccess = { isLoading = false },
                        onError = {
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
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
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