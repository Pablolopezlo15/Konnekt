package pl.konnekt.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Auth : Screen("auth")
    object UserList : Screen("userList")
    object Search : Screen("search")
    object Chat : Screen("chat/{userId}") {
        fun createRoute(userId: String) = "chat/$userId"
    }
    object Profile : Screen("profile/{userId}") {
        fun createRoute(userId: String) = "profile/$userId"
    }
    object UploadImage : Screen("uploadImage")
}