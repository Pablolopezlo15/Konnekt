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
    object CreatePost : Screen("create_post")
    object Notifications : Screen("notifications")
    object Post : Screen("post/{postId}") {
        fun createRoute(postId: String) = "post/$postId"
    }
    object Comments : Screen("comments/{postId}") {
        fun createRoute(postId: String) = "comments/$postId"
    }
    object SavedPosts : Screen("saved_posts/{userId}") {
        fun createRoute(userId: String) = "saved_posts/$userId"
    }
}
