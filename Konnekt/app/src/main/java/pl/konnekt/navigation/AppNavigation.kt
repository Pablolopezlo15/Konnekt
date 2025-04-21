package pl.konnekt.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import pl.konnekt.AuthScreen
import pl.konnekt.models.User
import pl.konnekt.ui.components.*
import pl.konnekt.utils.TokenDecoder
import pl.konnekt.viewmodel.ChatViewModel
import pl.konnekt.viewmodel.UserListViewModel
import pl.konnekt.viewmodel.UserViewModel
import pl.konnekt.viewmodels.AuthViewModel

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    token: String,
    onLogout: () -> Unit
) {
    val userListViewModel = remember { UserListViewModel() }

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(200)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(200)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(200)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(200)
            )
        }
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
        
        composable(Screen.Profile.route) {
            val userId = TokenDecoder.getUserIdFromToken(token)
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
                onLogout = {
                    onLogout()
                    navController.navigate(Screen.Auth.route) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.UserList.route) {
            LaunchedEffect(Unit) {
                userListViewModel.loadUsers()
            }
            UserListScreen(
                viewModel = userListViewModel,
                navController = navController
            )
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val recipientId = backStackEntry.arguments?.getString("userId") ?: return@composable
            val chatViewModel = remember(recipientId) { 
                ChatViewModel(
                    currentUserId = TokenDecoder.getUserIdFromToken(token) ?: "",
                    recipientId = recipientId
                )
            }
            val user = userListViewModel.users.collectAsState().value.find { it.id == recipientId }
            ChatScreen(
                viewModel = chatViewModel,
                recipientId = recipientId,
                userId = TokenDecoder.getUserIdFromToken(token) ?: "",
                recipientName = user?.username ?: "Usuario",
                navController = navController
            )
        }
    }
}