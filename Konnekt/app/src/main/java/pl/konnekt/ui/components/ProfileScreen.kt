package pl.konnekt.ui.components

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import pl.konnekt.models.User
import pl.konnekt.models.createPabloUser
import pl.konnekt.ui.theme.KonnektTheme

@Composable
fun ProfileScreen(navController: NavController, user: User, onLogout: () -> Unit = {}) {
    var userName by remember { mutableStateOf(TextFieldValue(user.username)) }
    var userEmail by remember { mutableStateOf(TextFieldValue(user.email)) }
    var userPhone by remember { mutableStateOf(TextFieldValue(user.phone.toString())) }
    val context = LocalContext.current

    KonnektTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Profile Header (Image and Username)

            Row {
                AsyncImage(
                    model = user.profileImageUrl,
                    contentDescription = "Profile picture of ${user.username}",
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)

                )
                Column {
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp),
                        text = user.username,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Posts", count = "12")
                        StatItem(label = "Followers", count = user.followers?.size.toString())
                        StatItem(label = "Following", count = user.following?.size.toString())
                    }
                }
            }


            // Stats Row (Posts, Followers, Following)


            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                        .edit()
                        .remove("token")
                        .apply()
                    onLogout()
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}

@Composable
fun StatItem(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleMedium.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview
@Composable
fun PreviewProfileScreen() {
    val sampleUser = createPabloUser()
    ProfileScreen(navController = NavController(LocalContext.current), user = sampleUser)
}