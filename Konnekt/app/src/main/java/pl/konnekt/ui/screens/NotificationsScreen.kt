package pl.konnekt.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pl.konnekt.config.AppConfig
import pl.konnekt.viewmodel.NotificationsViewModel
import pl.konnekt.models.FriendRequest
import pl.konnekt.utils.CoilConfig
import pl.konnekt.R

@Composable
fun NotificationsScreen(
    currentUserId: String?,
    viewModel: NotificationsViewModel = viewModel()
) {
    val receivedRequests by viewModel.receivedRequests.collectAsState()
    val sentRequests by viewModel.sentRequests.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(currentUserId) {
        currentUserId?.let { userId ->
            viewModel.loadReceivedRequests(userId)
            viewModel.loadSentRequests(userId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Solicitudes de seguimiento",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            error?.let { errorMessage ->
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            LazyColumn {
                if (receivedRequests.isNotEmpty()) {
                    Log.d("NotificationsScreen", "Received Requests: $receivedRequests")
                    item {
                        Text(
                            text = "Recibidas",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(
                        items = receivedRequests,
                        key = { it.id }
                    ) { request ->
                        Log.d("NotificationsScreen", "Request: $request")
                        RequestItem(
                            request = request,
                            onAccept = { 
                                currentUserId?.let { userId ->
                                    viewModel.acceptRequest(userId, request.id)
                                }
                            },
                            onReject = {
                                currentUserId?.let { userId ->
                                    viewModel.rejectRequest(userId, request.id)
                                }
                            }
                        )
                    }
                }

                if (sentRequests.isNotEmpty()) {
                    item {
                        Text(
                            text = "Enviadas",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(
                        items = sentRequests,
                        key = { it.id }
                    ) { request ->
                        Log.d("NotificationsScreen", "Request: $request")
                        RequestItem(
                            request = request,
                            isPending = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RequestItem(
    request: FriendRequest,
    onAccept: (() -> Unit)? = null,
    onReject: (() -> Unit)? = null,
    isPending: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                var isLoading by remember { mutableStateOf(true) }
                var imageLoadError by remember { mutableStateOf(false) }
                var imageUrl = AppConfig.BASE_URL + (if (isPending) request.receiverProfileImage else request.senderProfileImage ?: "")
                val context = LocalContext.current
                
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
                    imageLoader = CoilConfig.getImageLoader(context), // Ahora context está definido
                    contentDescription = "Profile picture",
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    onLoading = { isLoading = true },
                    onSuccess = { 
                        isLoading = false
                        Log.d("NotificationsScreen", "Profile image loaded successfully")
                    },
                    onError = { 
                        Log.e("NotificationsScreen", "Error loading profile image: ${it.result.throwable}")
                        imageLoadError = true
                        isLoading = false
                    }
                )
                
                if (isLoading && !imageLoadError) {
                    Box(
                        modifier = Modifier
                            .size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                Column {
                    Text(
                        text = if (isPending) request.receiverUsername ?: "Usuario desconocido" else request.senderUsername ?: "Usuario desconocido",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isPending) "Solicitud pendiente" else "Nueva solicitud",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = request.getFormattedDate(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (!isPending) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { onAccept?.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Aceptar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onReject?.invoke() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Rechazar",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = "Pendiente",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}