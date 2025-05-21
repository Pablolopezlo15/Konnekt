package pl.konnekt.ui.components

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.konnekt.models.Post
import pl.konnekt.models.PostItem
import pl.konnekt.models.User
import pl.konnekt.models.createPabloUser
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.ResistanceConfig
import androidx.compose.material.Text
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import pl.konnekt.navigation.Screen
import androidx.lifecycle.viewmodel.compose.viewModel
import pl.konnekt.viewmodel.PostViewModel
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(modifier: Modifier = Modifier) {
    val navController = LocalNavController.current
    val swipeableState = rememberSwipeableState(initialValue = 0)
    val anchors = mapOf(0f to 0, -300f to 1)
    val postViewModel: PostViewModel = viewModel()
    val posts by postViewModel.posts.collectAsState()
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        // Forzar recarga de posts
        postViewModel.clearPosts()
        postViewModel.getAllPosts(context)
    }
    
    // Añadir un efecto para recargar cuando se vuelve a la pantalla
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                postViewModel.clearPosts()
                postViewModel.getAllPosts(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    Box(
        modifier = modifier
            .fillMaxSize()
            .swipeable(
                state = swipeableState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.15f)},
                orientation = Orientation.Horizontal,
                resistance = ResistanceConfig(0.3f)
            )
    ) {
        LaunchedEffect(swipeableState.currentValue) {
            if (swipeableState.currentValue == 1) {
                navController.navigate(Screen.UserList.route)
                swipeableState.snapTo(0)
            }
        }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 8.dp)
            ) {
                items(posts) { post ->
                    PostItem(
                        post = post,
                        onLikeClick = { postId, isLiked, callback ->
                            postViewModel.likePost(postId, context, isLiked, callback)
                        },
                        onCommentClick = { postId -> 
                            navController.navigate("post/$postId/comments")
                        },
                        onSaveClick = { postId, isSaved, callback ->
                            postViewModel.savePost(postId, context, callback)
                        }
                    )
                }
            }
        }
    }
}
