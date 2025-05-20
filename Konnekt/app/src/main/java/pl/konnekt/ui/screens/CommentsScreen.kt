package pl.konnekt.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.konnekt.config.AppConfig
import pl.konnekt.models.Comment
import pl.konnekt.ui.components.LocalNavController
import pl.konnekt.viewmodel.PostViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentsScreen(postId: String) {
    val postViewModel: PostViewModel = viewModel()
    val comments by postViewModel.comments.collectAsState()
    var postImageUrl by remember { mutableStateOf("") }
    val context = LocalContext.current
    val navController = LocalNavController.current
    var commentText by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState()
    val isGeneratingComment by postViewModel.isGeneratingComment.collectAsState()

    LaunchedEffect(postId) {
        postViewModel.getComments(postId, context)
        val post = postViewModel.getPost(postId, context)
        post?.let {
            postImageUrl = it.imageUrl
        }
    }

    ModalBottomSheet(
        onDismissRequest = { navController.popBackStack() },
        sheetState = sheetState,
        modifier = Modifier
            .fillMaxWidth()
            .height(600.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background.copy(alpha = 0.9f))
                .padding(16.dp)
        ) {
            // Header y campo de entrada
            Text(
                text = "Comentarios",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Campo de entrada de comentarios movido arriba
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    placeholder = { Text("Escribe...") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (commentText.isNotBlank()) {
                                postViewModel.postComment(postId, commentText, context) { success, newComment ->
                                    if (success) {
                                        commentText = ""
                                    }
                                }
                            }
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                
                // Botón para generar comentario con IA
                IconButton(
                    onClick = {
                        postViewModel.generateAIComment(
                            "${AppConfig.BASE_URL}$postImageUrl",
                            context
                        ) { generatedComment ->
                            commentText = generatedComment
                        }
                    },
                    enabled = !isGeneratingComment
                ) {
                    if (isGeneratingComment) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Generar comentario con IA",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                IconButton(
                    onClick = {
                        if (commentText.isNotBlank()) {
                            postViewModel.postComment(postId, commentText, context) { success, newComment ->
                                if (success) {
                                    commentText = ""
                                }
                            }
                        }
                    },
                    enabled = commentText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Publicar comentario",
                        tint = if (commentText.isNotBlank()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Lista de comentarios
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(comments) { comment ->
                    CommentItem(comment = comment)
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    val formattedDate = remember(comment.timestamp) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = inputFormat.parse(comment.timestamp)
            outputFormat.format(date)
        } catch (e: Exception) {
            comment.timestamp
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = comment.authorUsername,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = comment.content,
            fontSize = 14.sp,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}