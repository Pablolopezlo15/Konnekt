package pl.konnekt.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Auth : Screen("auth")
    object Profile : Screen("profile")
    object UserList : Screen("userList")
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }
}