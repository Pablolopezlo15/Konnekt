package pl.konnekt.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import pl.konnekt.models.User
import pl.konnekt.viewmodel.UserSearchViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.draw.clip

@Composable
fun UserSearchScreen(
    viewModel: UserSearchViewModel,
    onUserClick: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                viewModel.searchUsers(it)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Buscar...") },
            singleLine = true,
            leadingIcon = { // Add this block for the leading icon
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else {
            LazyColumn {
                items(searchResults) { user ->
                    UserCard(
                        user = user,
                        onClick = { onUserClick(user.id) }  // Navigate to profile using user.id
                    )
                }
            }
        }
    }
}

@Composable
private fun UserCard(
    user: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.profileImageUrl,
                contentDescription = "Profile picture",
                modifier = Modifier
                    .size(48.dp)
                    // Add a circular shape to the image
                    .clip(CircleShape)
                    .padding(end = 16.dp)
            )

            // Display only the username
            Text(
                text = user.username,
                style = MaterialTheme.typography.titleMedium
            )

        }
    }
}