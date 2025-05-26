package pl.konnekt.ui.components

import android.content.Context
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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

    NavigationBar(
        // Puedes ajustar el color del contenedor si deseas
        // containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
            //label = { Text("Home") }, // Puedes descomentar si quieres etiquetas
            selected = currentRoute == Screen.Home.route,
            onClick = {
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.Home.route) { inclusive = true }
                }
            },
            // Ajustes para un look más moderno
            alwaysShowLabel = false, // Oculta las etiquetas si no están seleccionadas
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.padding(horizontal = 4.dp) // Ajusta el padding
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            //label = { Text("Search") },
            selected = currentRoute == Screen.Search.route,
            onClick = {
                navController.navigate(Screen.Search.route) {
                    popUpTo(Screen.Search.route) { inclusive = true }
                }
            },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        NavigationBarItem(
            icon = { Icon(Icons.Filled.AddCircle, contentDescription = "Create Post") },
            //label = { Text("Create") },
            selected = currentRoute == Screen.CreatePost.route,
            onClick = { navController.navigate(Screen.CreatePost.route) },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        NavigationBarItem(
            icon = { Icon(Icons.Default.Favorite, contentDescription = "Notificaciones") },
            //label = { Text("Notificaciones") },
            selected = currentRoute == Screen.Notifications.route,
            onClick = {
                navController.navigate("notifications")
            },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        
        NavigationBarItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "Profile") },
            //label = { Text("Profile") },
            selected = currentRoute == Screen.Profile.route,
            onClick = {
                currentUserId?.let { userId ->
                    navController.navigate(Screen.Profile.createRoute(userId)) {
                        launchSingleTop = true
                        popUpTo(Screen.Home.route)
                    }
                }
            },
            alwaysShowLabel = false,
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                indicatorColor = MaterialTheme.colorScheme.surface
            ),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
