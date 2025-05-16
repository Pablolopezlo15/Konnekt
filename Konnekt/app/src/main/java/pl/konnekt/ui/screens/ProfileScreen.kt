package pl.konnekt.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import pl.konnekt.models.User
import pl.konnekt.navigation.Screen
import pl.konnekt.ui.theme.KonnektTheme
import pl.konnekt.utils.TokenDecoder
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.konnekt.viewmodel.UserViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import coil.request.ImageRequest
import pl.konnekt.config.AppConfig
import pl.konnekt.viewmodel.PostViewModel

@Composable
fun ProfileScreen(
    navController: NavController, 
    user: User, 
    onLogout: () -> Unit = {},
    currentUserId: String? = null,
    viewModel: UserViewModel = viewModel(),
    viewModelPost: PostViewModel = viewModel()
) {
    val isCurrentUser = currentUserId == user.id
    val context = LocalContext.current
    val updatedUser by viewModel.userProfile.collectAsState()
    val displayUser = updatedUser ?: user
    val isFollowing = displayUser.followers.contains(currentUserId)
    val isLoading by viewModel.isLoading.collectAsState()

    // Load posts when the screen is first displayed
    LaunchedEffect(user.id) {
        viewModelPost.getUserPosts(user.id, context)
    }

    val posts by viewModelPost.posts.collectAsState()

    KonnektTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row {
                AsyncImage(
                    model = displayUser.profileImageUrl,
                    contentDescription = "Profile picture of ${displayUser.username}",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )
                Column {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        text = displayUser.username,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Posts", count = posts.size.toString())
                        StatItem(label = "Followers", count = displayUser.followers.size.toString())
                        StatItem(label = "Following", count = displayUser.following.size.toString())
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isCurrentUser) {
                            Button(
                                onClick = { 
                                    currentUserId?.let { cuid ->
                                        if (isFollowing) {
                                            viewModel.unfollowUser(displayUser.id, cuid)
                                        } else {
                                            viewModel.followUser(displayUser.id, cuid)
                                        }
                                    }
                                },
                                enabled = !isLoading,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFollowing) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        Color.Transparent,
                                    contentColor = if (isFollowing) 
                                        MaterialTheme.colorScheme.onError 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isFollowing) 
                                        MaterialTheme.colorScheme.error 
                                    else 
                                        MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)  // Altura fija para ambos botones
            
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = if (isFollowing) 
                                            MaterialTheme.colorScheme.onError 
                                        else 
                                            MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Text(if (isFollowing) "Siguiendo" else "Seguir")
                                }
                            }

                            Button(
                                onClick = { 
                                    navController.navigate(Screen.Chat.createRoute(user.id))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp),  // Misma altura que el botón Seguir
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Mensaje")
                            }
                        } else {
                            Button(
                                onClick = {
                                    context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                                        .edit()
                                        .remove("token")
                                        .apply()
                                    onLogout()
                                    navController.navigate("auth") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    
                            ) {
                                Text("Cerrar Sesión")
                            }
                        }
                    }
                }
            }

            // Grid de posts con mejor visualización
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
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                //navController.navigate(Screen.PostDetail.createRoute(post.id))
                            }
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data("${AppConfig.BASE_URL}${post.imageUrl}")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Post image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
