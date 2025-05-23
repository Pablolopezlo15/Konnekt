package pl.konnekt.ui.components

import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pl.konnekt.R
import pl.konnekt.config.AppConfig
import pl.konnekt.models.User
import pl.konnekt.navigation.Screen
import pl.konnekt.ui.theme.KonnektTheme
import pl.konnekt.utils.CoilConfig
import pl.konnekt.viewmodel.PostViewModel
import pl.konnekt.viewmodel.UserViewModel
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.imageLoader

@Composable
fun ProfileScreen(
    navController: NavController,
    user: User,
    onLogout: () -> Unit = {},
    currentUserId: String? = null,
    viewModel: UserViewModel = viewModel(),
    viewModelPost: PostViewModel = viewModel()
) {
    val showEditDialog = remember { mutableStateOf(false) }
    val isCurrentUser = currentUserId == user.id
    val context = LocalContext.current
    val updatedUser by viewModel.userProfile.collectAsState()
    val displayUser = updatedUser ?: user
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val isFollowing = displayUser.followers.contains(currentUserId)
    val canViewPosts = isCurrentUser || isFollowing || !displayUser.private_account

    // Load user profile and posts
    LaunchedEffect(user.id, showEditDialog.value) {
        if (currentUserId != null) {
            viewModel.loadUserProfile(user.id, currentUserId)
            viewModelPost.getUserPosts(user.id, context)
        }
        // Limpiar caché de Coil para descartar problemas de caché
        context.imageLoader.memoryCache?.clear()
        context.imageLoader.diskCache?.clear()
    }

    // Efecto para recargar el perfil después de editar
    LaunchedEffect(showEditDialog.value) {
        if (!showEditDialog.value && currentUserId != null) {
            // Solo recargamos cuando el diálogo se cierra
            viewModel.loadUserProfile(user.id, currentUserId)
            viewModelPost.getUserPosts(user.id, context)
            // Limpiar caché de Coil
            context.imageLoader.memoryCache?.clear()
            context.imageLoader.diskCache?.clear()
        }
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
            // Error message display
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            if (isLoading && updatedUser == null) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            } else {
                val imageUrl = if (!displayUser.profileImageUrl.isNullOrEmpty() && displayUser.profileImageUrl != "") {
                    "${AppConfig.BASE_URL}${displayUser.profileImageUrl}"
                } else {
                    null
                }
                Log.d("ProfileScreen", "Display User: $displayUser")
                Log.d("ProfileScreen", "Formatted Image URL: $imageUrl")

                Row {
                    if (imageUrl != null) {
                        var isLoading by remember { mutableStateOf(true) }
                        var imageLoadError by remember { mutableStateOf(false) }

                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(imageUrl)
                                .crossfade(true)
                                .allowHardware(false)
                                .allowRgb565(true)
                                .memoryCacheKey(imageUrl)
                                .diskCacheKey(imageUrl)
                                .error(R.drawable.default_profile_image)
                                .placeholder(R.drawable.default_profile_image)
                                .build(),
                            imageLoader = CoilConfig.getImageLoader(context),
                            contentDescription = "Profile picture of ${displayUser.username}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = isCurrentUser) {
                                    if (isCurrentUser) {
                                        showEditDialog.value = true
                                    }
                                },
                            onLoading = { isLoading = true },
                            onSuccess = {
                                isLoading = false
                                Log.d("ProfileScreen", "Profile image loaded successfully: $imageUrl")
                            },
                            onError = {
                                Log.e("ProfileScreen", "Error loading profile image: ${it.result.throwable}")
                                imageLoadError = true
                                isLoading = false
                            }
                        )

                        if (isLoading && !imageLoadError) {
                            Box(
                                modifier = Modifier.size(90.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.default_profile_image),
                            contentDescription = "Profile picture of ${displayUser.username}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(enabled = isCurrentUser) {
                                    if (isCurrentUser) {
                                        showEditDialog.value = true
                                    }
                                }
                        )
                    }
                    Column {
                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp),
                            text = displayUser.username,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(label = "Posts", count = posts.size.toString())
                            StatItem(label = "Seguidores", count = displayUser.followers.size.toString())
                            StatItem(label = "Seguidos", count = displayUser.following.size.toString())
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!isCurrentUser) {
                                val followRequestStatus by viewModel.followRequestStatus.collectAsState()
                                val buttonText = when {
                                    isFollowing -> "Siguiendo"
                                    followRequestStatus == "pending" -> "Solicitado"
                                    displayUser.private_account -> "Solicitar"
                                    else -> "Seguir"
                                }

                                val buttonEnabled = !isLoading && followRequestStatus != "pending"

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
                                    enabled = buttonEnabled,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = when {
                                            isFollowing -> MaterialTheme.colorScheme.error
                                            followRequestStatus == "pending" -> MaterialTheme.colorScheme.surfaceVariant
                                            else -> MaterialTheme.colorScheme.primary
                                        }
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                ) {
                                    Row(
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        if (followRequestStatus == "pending") {
                                            Icon(
                                                imageVector = Icons.Default.Schedule,
                                                contentDescription = "Pending",
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                        }
                                        Text(
                                            text = buttonText,
                                            maxLines = 1,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                Button(
                                    onClick = {
                                        navController.navigate(Screen.Chat.createRoute(user.id))
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary
                                    )
                                ) {
                                    Text(
                                        text = "Mensaje",
                                        maxLines = 1,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            } else {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Button(
                                        onClick = { showEditDialog.value = true },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Text("Editar Perfil")
                                    }

                                    Button(
                                        onClick = {
                                            navController.navigate("saved_posts/${user.id}")
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.secondary
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Text("Posts Guardados")
                                    }

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
                                            .weight(1f)
                                            .height(40.dp)
                                    ) {
                                        Text("Cerrar Sesión")
                                    }
                                }
                            }
                        }
                    }
                }

                if (canViewPosts) {
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
                            Log.d("ProfileScreen", "Loading post image: $postImageUrl")
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
                                    imageLoader = CoilConfig.getImageLoader(context), // Usar ImageLoader personalizado
                                    contentDescription = "Post image",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    onLoading = { isLoading = true },
                                    onSuccess = {
                                        isLoading = false
                                        Log.d("ProfileScreen", "Post image loaded successfully: $postImageUrl")
                                    },
                                    onError = {
                                        Log.e("ProfileScreen", "Error loading post image: ${it.result.throwable}")
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
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Private Account",
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Esta cuenta es privada",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sigue a este usuario para ver sus fotos",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        if (showEditDialog.value && isCurrentUser) {
            EditProfileDialog(
                user = displayUser,
                onDismiss = { showEditDialog.value = false },
                onSave = { updatedData ->
                    Log.d("EditProfileDialog", "Saving updated data: $updatedData")
                    viewModel.updateProfile(user.id, updatedData)
                    showEditDialog.value = false
                }
            )
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
