package pl.konnekt.ui.components

import android.R.attr.fontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import pl.konnekt.R
import pl.konnekt.models.LoginResponse
import pl.konnekt.models.UserResponse
import pl.konnekt.viewmodels.AuthViewModel

@OptIn(ExperimentalPagerApi::class)
@Composable
fun AuthComponent(
    viewModel: AuthViewModel,
    onLoginSuccess: (LoginResponse) -> Unit,
    onRegisterSuccess: (UserResponse) -> Unit,
    isLoading: Boolean,
    error: String?
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }

    val pagerState = rememberPagerState(initialPage = 0)
    val tabs = listOf("Login", "Registro")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            )
    ) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.Center)
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Error Message
        error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Tabs
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.clip(RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    text = { Text(title, style = MaterialTheme.typography.labelLarge) },
                    selected = pagerState.currentPage == index,
                    onClick = { /* Handled by HorizontalPager */ }
                )
            }
        }

        // Loading Indicator
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.padding(16.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Forms
        HorizontalPager(
            count = tabs.size,
            state = pagerState,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            when (page) {
                0 -> ModernLoginForm(
                    id = "", // Not used in your original, kept for compatibility
                    username = username,
                    password = password,
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it },
                    onLoginClick = {
                        viewModel.login(username, password, onLoginSuccess)
                    },
                    enabled = !isLoading
                )
                1 -> ModernRegisterForm(
                    username = username,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    phone = phone,
                    birthDate = birthDate,
                    onUsernameChange = { username = it },
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onConfirmPasswordChange = { confirmPassword = it },
                    onPhoneChange = { phone = it },
                    onBirthDateChange = { birthDate = it },
                    onRegisterClick = {
                        if (password == confirmPassword) {
                            viewModel.register(
                                username = username,
                                password = password,
                                email = email,
                                phone = phone.takeIf { it.isNotBlank() },
                                birthDate = birthDate.takeIf { it.isNotBlank() },
                                onSuccess = onRegisterSuccess
                            )
                        } else {
                            viewModel.setError("Las contraseñas no coinciden")
                        }
                    },
                    enabled = !isLoading
                )
            }
        }
    }
}
}

@Composable
fun ModernLoginForm(
    id: String,
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Sign in to continue",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person), // Add your person icon
                    contentDescription = "Username",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            placeholder = { Text("Username") },
            singleLine = true,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock), // Add your lock icon
                    contentDescription = "Password",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            placeholder = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 32.dp),
            enabled = enabled && username.isNotBlank() && password.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign In",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
fun ModernRegisterForm(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    phone: String,
    birthDate: String,
    onUsernameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onBirthDateChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    enabled: Boolean
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontSize = 32.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Join us today",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_person),
                    contentDescription = "Username",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            placeholder = { Text("Username") },
            singleLine = true,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_email), // Add your email icon
                    contentDescription = "Email",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            placeholder = { Text("Email") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Password",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            placeholder = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_lock),
                    contentDescription = "Confirm Password",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            placeholder = { Text("Confirm Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_phone), // Add your phone icon
                    contentDescription = "Phone",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            placeholder = { Text("Phone (optional)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        OutlinedTextField(
            value = birthDate,
            onValueChange = onBirthDateChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            leadingIcon = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_calendar), // Add your calendar icon
                    contentDescription = "Birth Date",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            placeholder = { Text("Birth Date (optional)") },
            singleLine = true,
            enabled = enabled,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                cursorColor = MaterialTheme.colorScheme.primary
            )
        )

        Button(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(top = 32.dp),
            enabled = enabled && username.isNotBlank() && email.isNotBlank() && password.isNotBlank() && confirmPassword.isNotBlank(),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            contentPadding = PaddingValues(0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Sign Up",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                )
            }
        }
    }
}