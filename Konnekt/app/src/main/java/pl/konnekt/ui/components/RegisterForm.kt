import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp


@Composable
fun RegisterForm(
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
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = username,
            onValueChange = onUsernameChange,
            label = { Text("Usuario") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = onConfirmPasswordChange,
            label = { Text("Confirmar Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Teléfono (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = birthDate,
            onValueChange = onBirthDateChange,
            label = { Text("Fecha de nacimiento (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = enabled
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRegisterClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.hsl(39f, 1f, 0.5f),
                contentColor = Color.White
            ),
            enabled = enabled && 
                     username.isNotBlank() && 
                     email.isNotBlank() && 
                     password.isNotBlank() && 
                     confirmPassword.isNotBlank() &&
                     password == confirmPassword
        ) {
            Text("Registrarse")
        }
    }
}