package pl.konnekt.ui.components

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import pl.konnekt.navigation.Screen
import pl.konnekt.utils.TokenDecoder

@Composable
fun BottomBar(navController: NavController) {
    val context = LocalContext.current
    val token = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
        .getString("token", "") ?: ""
    val currentUserId = TokenDecoder.getUserIdFromToken(token)

    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            label = { Text("Home") },
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            label = { Text("Search") },
            selected = currentRoute == Screen.Search.route,
            onClick = {
                navController.navigate(Screen.Search.route) {
                    popUpTo(Screen.Search.route) { inclusive = true }
                }
            }
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            label = { Text("Profile") },
            selected = currentRoute?.startsWith("profile") == true,
            onClick = {
                currentUserId?.let { userId ->
                    navController.navigate(Screen.Profile.createRoute(userId)) {
                        launchSingleTop = true
                        popUpTo(Screen.Home.route)
                    }
                }
            }
        )
    }
}
