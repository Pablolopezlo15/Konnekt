package pl.konnekt

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import pl.konnekt.models.RegisterResponse
import pl.konnekt.models.User
import pl.konnekt.models.createPabloUser
import pl.konnekt.navigation.AppNavigation
import pl.konnekt.navigation.Screen
import pl.konnekt.ui.components.AuthComponent
import pl.konnekt.ui.components.BottomBar
import pl.konnekt.ui.components.ChatScreen
import pl.konnekt.ui.components.LocalNavController
import pl.konnekt.ui.components.MainScreen
import pl.konnekt.ui.components.MyTopBar
import pl.konnekt.ui.components.ProfileScreen
import pl.konnekt.ui.components.UserListScreen
import pl.konnekt.ui.theme.KonnektTheme
import pl.konnekt.utils.TokenDecoder
import pl.konnekt.viewmodel.ChatViewModel
import pl.konnekt.viewmodel.UserListViewModel
import pl.konnekt.viewmodel.UserViewModel
import pl.konnekt.viewmodels.AuthViewModel

class MainActivity : ComponentActivity() {
    var token: String = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        // Obtener el token guardado
        val sharedPreferences = getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        token = sharedPreferences.getString("token", "") ?: ""

        setContent {
            KonnektTheme {
                var isLoggedIn by remember { mutableStateOf(token.isNotBlank()) }
                val navController = rememberNavController()

                if (isLoggedIn) {
                    Log.d("MainActivity", "User is logged in, token length: ${token.length}")
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            val currentRoute by navController.currentBackStackEntryAsState()
                            if (currentRoute?.destination?.route?.startsWith("chat/") == true) {

                            } else {
                                MyTopBar(
                                    title = "Konnekt",
                                    onBackClick = { navController.popBackStack() },
                                    onChatsClick = { navController.navigate(Screen.UserList.route) }  // Actualizado
                                )
                            }
                        },
                        bottomBar = { BottomBar(navController) }
                    ) { innerPadding ->
                        CompositionLocalProvider(LocalNavController provides navController) {
                            AppNavigation(
                                navController = navController,
                                modifier = Modifier.padding(innerPadding),
                                token = token,
                                onLogout = {
                                    isLoggedIn = false
                                    token = ""
                                }
                            )
                        }
                    }
                } else {
                    Log.d("MainActivity", "Showing auth screen")
                    AuthScreen(
                        onLoginSuccess = { accessToken ->
                            token = accessToken
                            // Guardar el token
                            sharedPreferences.edit().putString("token", accessToken).apply()
                            isLoggedIn = true
                            navController.navigate("home") {
                                popUpTo(0) { inclusive = true }
                            }
                        },
                        onRegisterSuccess = { accessToken ->
                            token = accessToken
                            sharedPreferences.edit().putString("token", accessToken).apply()
                            isLoggedIn = true
                            navController.navigate(Screen.Home.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NavigationGraph(
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
                animationSpec = tween(300)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            )
        }
    ) {
        composable(Screen.Home.route) {
            MainScreen(modifier = Modifier.padding())
        }
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = { /* ... */ },
                onRegisterSuccess = { /* ... */ }
            )
        }
        composable(
            route = Screen.Profile.route + "/{userId}",
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
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
            UserListScreen(
                viewModel = userListViewModel,
                navController = navController
            )
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("userId") { type = NavType.StringType })
        ) { backStackEntry ->
            val userId = backStackEntry.arguments?.getString("userId") ?: return@composable
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

@Composable
fun AuthScreen(
    onLoginSuccess: (String) -> Unit,
    onRegisterSuccess: (String) -> Unit
) {
    val authViewModel = remember { AuthViewModel() }
    val isLoading by authViewModel.isLoading.collectAsState()
    val error by authViewModel.error.collectAsState()
    val navController = rememberNavController()

    AuthComponent(
        viewModel = authViewModel,
        onLoginSuccess = { loginResponse ->
            loginResponse.access_token?.let { token ->
                onLoginSuccess(token)
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            }
        },
        onRegisterSuccess = { registerResponse ->
            registerResponse.access_token.let { token ->
                onRegisterSuccess(token)
                navController.navigate("home") {
                    popUpTo(0) { inclusive = true }
                }
            }
        },
        isLoading = isLoading,
        error = error
    )
}