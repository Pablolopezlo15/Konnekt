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

@Composable
fun ProfileScreen(
    navController: NavController, 
    user: User, 
    onLogout: () -> Unit = {},
    currentUserId: String? = null,
    viewModel: UserViewModel = viewModel()
) {
    val isCurrentUser = currentUserId == user.id
    val context = LocalContext.current
    val updatedUser by viewModel.userProfile.collectAsState()
    val displayUser = updatedUser ?: user
    val isFollowing = displayUser.followers.contains(currentUserId)
    val isLoading by viewModel.isLoading.collectAsState()

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
                        StatItem(label = "Posts", count = "0")
                        StatItem(label = "Followers", count = displayUser.followers.size.toString())
                        StatItem(label = "Following", count = displayUser.following.size.toString())
                    }

                    if (!isCurrentUser) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
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
                                    Text(if (isFollowing) "Unfollow" else "Follow")
                                }
                            }

                            Button(
                                onClick = { 
                                    navController.navigate(Screen.Chat.createRoute(user.id))
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Send Message")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isCurrentUser) {
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
                        .padding(vertical = 16.dp)
                ) {
                    Text("Cerrar Sesión")
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