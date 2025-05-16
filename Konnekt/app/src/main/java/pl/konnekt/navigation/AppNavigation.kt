package pl.konnekt.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pl.konnekt.AuthScreen
import pl.konnekt.models.User
import pl.konnekt.ui.components.*
import pl.konnekt.ui.screens.CommentsScreen
import pl.konnekt.ui.screens.CreatePostScreen
import pl.konnekt.utils.TokenDecoder
import pl.konnekt.viewmodel.ChatViewModel
import pl.konnekt.viewmodel.UserListViewModel
import pl.konnekt.viewmodel.UserViewModel
import pl.konnekt.viewmodels.AuthViewModel
import pl.konnekt.ui.screens.UserSearchScreen
import pl.konnekt.viewmodel.UserSearchViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    token: String,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(Screen.Home.route) {
            MainScreen(modifier = Modifier)
        }

        composable(Screen.Auth.route) {
            val authViewModel = remember { AuthViewModel() }
            AuthScreen(
                onLoginSuccess = { accessToken ->
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Profile.route,
            arguments = listOf(
                navArgument("userId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId")
            val userViewModel = remember { UserViewModel() }

            LaunchedEffect(userId) {
                userId?.let { id ->
                    userViewModel.loadUserProfile(id)
                }
            }

            val userProfile by userViewModel.userProfile.collectAsState()

            ProfileScreen(
                navController = navController,
                user = userProfile ?: User(
                    id = userId ?: "",
                    username = "Loading...",
                    email = "",
                    phone = null,
                    profileImageUrl = null,
                    followers = emptyList(),
                    following = emptyList()
                ),
                onLogout = onLogout,
                currentUserId = TokenDecoder.getUserIdFromToken(token)
            )
        }

        composable(Screen.UserList.route) {
            val userListViewModel = remember { UserListViewModel() }
            UserListScreen(
                viewModel = userListViewModel,
                navController = navController
            )
        }

        composable(Screen.Search.route) {
            val searchViewModel = remember { UserSearchViewModel() }
            UserSearchScreen(
                viewModel = searchViewModel,
                onUserClick = { userId ->
                    navController.navigate("profile/$userId")
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipientId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val context = LocalContext.current
            val chatViewModel = remember(recipientId) {
                ChatViewModel(
                    currentUserId = TokenDecoder.getUserIdFromToken(token) ?: "",
                    recipientId = recipientId
                )
            }
            val userViewModel = remember { UserViewModel() }

            LaunchedEffect(recipientId) {
                userViewModel.loadUserProfile(recipientId)
            }

            val recipientProfile by userViewModel.userProfile.collectAsState()

            ChatScreen(
                viewModel = chatViewModel,
                recipientId = recipientId,
                userId = TokenDecoder.getUserIdFromToken(token) ?: "",
                recipientName = recipientProfile?.username ?: "Loading...",
                navController = navController
            )
        }

        composable(Screen.CreatePost.route) {
            CreatePostScreen(
                onPostCreated = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "post/{postId}/comments",
            arguments = listOf(navArgument("postId") { type = NavType.StringType }),
            enterTransition = { slideInHorizontally(tween(300)) { it } },
            exitTransition = { slideOutHorizontally(tween(300)) { it } }
        ) { backStackEntry ->
            val postId = backStackEntry.arguments?.getString("postId") ?: ""
            CommentsScreen(postId = postId)
        }
    }
}
