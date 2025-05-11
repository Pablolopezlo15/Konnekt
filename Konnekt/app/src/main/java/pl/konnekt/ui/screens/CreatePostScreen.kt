package pl.konnekt.ui.screens

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.konnekt.ui.components.ImagePicker
import pl.konnekt.viewmodel.PostViewModel

@Composable
fun CreatePostScreen(
    onPostCreated: () -> Unit,
    viewModel: PostViewModel = viewModel()
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var caption by remember { mutableStateOf("") }
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ImagePicker { uri ->
            selectedImageUri = uri
        }

        if (selectedImageUri != null) {
            OutlinedTextField(
                value = caption,
                onValueChange = { caption = it },
                label = { Text("Caption") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    selectedImageUri?.let { uri ->
                        viewModel.createPost(context, uri, caption)
                        onPostCreated()
                    }
                },
                enabled = !isLoading && caption.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Upload Post")
                }
            }
        }
    }
}