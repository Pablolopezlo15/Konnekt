package pl.konnekt.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.konnekt.models.User
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.app.DatePickerDialog
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pl.konnekt.config.AppConfig
import pl.konnekt.utils.ImageUploader
import androidx.compose.material.icons.filled.DateRange
import coil.imageLoader
import coil.request.CachePolicy
import pl.konnekt.R
import pl.konnekt.utils.CoilConfig

@Composable
fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (Map<String, String>) -> Unit
) {
    val context = LocalContext.current
    val calendar = remember { Calendar.getInstance() }
    val scope = rememberCoroutineScope()
    
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone ?: "") }
    var birthDate by remember { mutableStateOf(user.birthDate ?: "") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var isPrivateAccount by remember { mutableStateOf(user.private_account) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    var emailError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var birthDateError by remember { mutableStateOf<String?>(null) }

    fun validateBirthDate(): Boolean {
        return if (birthDate.isNotEmpty()) {
            try {
                val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                formatter.isLenient = false
                formatter.parse(birthDate)
                birthDateError = null
                true
            } catch (e: Exception) {
                birthDateError = "Formato inválido (dd/mm/yyyy)"
                false
            }
        } else {
            birthDateError = null
            true
        }
    }

    user.birthDate?.let {
        try {
            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            calendar.time = formatter.parse(it) ?: Date()
        } catch (e: Exception) {
            calendar.time = Date()
        }
    }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                birthDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    .format(calendar.time)
                validateBirthDate()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    fun validateEmail(): Boolean {
        return if (email.isEmpty()) {
            emailError = "El email no puede estar vacío"
            false
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Email inválido"
            false
        } else {
            emailError = null
            true
        }
    }

    fun validatePhone(): Boolean {
        return if (phone.isNotEmpty() && !phone.matches(Regex("^[0-9]{9}$"))) {
            phoneError = "Teléfono inválido (9 dígitos)"
            false
        } else {
            phoneError = null
            true
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            Log.d("EditProfileDialog", "Imagen seleccionada: $it")
            // La llamada a AsyncImage se ha movido fuera de este lambda
        }
    }

    AlertDialog(
        onDismissRequest = {
            Log.d("EditProfileDialog", "Diálogo cerrado por el usuario")
            onDismiss()
        },
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Editar Perfil")
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .clickable { imagePickerLauncher.launch("image/*") }
                ) {
                    if (selectedImageUri != null) {
                        Log.d("EditProfileDialog", "Cargando imagen seleccionada: $selectedImageUri")
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedImageUri)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.DISABLED)  
                                .memoryCachePolicy(CachePolicy.DISABLED)  
                                .build(),
                            contentDescription = "Selected profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        val imageUrl = "${AppConfig.BASE_URL}${user.profileImageUrl}"
                        Log.d("EditProfileDialog", "Cargando imagen de perfil actual: $imageUrl")
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .crossfade(true)
                                .diskCachePolicy(CachePolicy.DISABLED)
                                .memoryCachePolicy(CachePolicy.DISABLED)
                                .build(),
                            contentDescription = "Current profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            onError = {
                                Log.e("EditProfileDialog", "Error al cargar la imagen de perfil", it.result.throwable)
                            }
                        )
                    }
                    
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = "Change photo",
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                            .padding(4.dp)
                    )
                }

                OutlinedTextField(
                    value = email,
                    onValueChange = { 
                        email = it
                        validateEmail()
                    },
                    label = { Text("Email") },
                    isError = emailError != null,
                    supportingText = { emailError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { 
                        phone = it
                        validatePhone()
                    },
                    label = { Text("Teléfono") },
                    isError = phoneError != null,
                    supportingText = { phoneError?.let { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { },
                    label = { Text("Fecha de nacimiento") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Select date"
                            )
                        }
                    },
                    isError = birthDateError != null,
                    supportingText = { birthDateError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Switch(
                        checked = isPrivateAccount,
                        onCheckedChange = { 
                            isPrivateAccount = it
                            Log.d("EditProfileDialog", "Private account changed to: $it")
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cuenta Privada")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validateEmail() && validatePhone() && validateBirthDate()) {
                        scope.launch {
                            isLoading = true
                            val updates = mutableMapOf<String, String>()
                            if (email != user.email) updates["email"] = email
                            if (phone != user.phone) updates["phone"] = phone
                            if (birthDate != user.birthDate) updates["birth_date"] = birthDate
                            if (isPrivateAccount != user.private_account) {
                                updates["private_account"] = isPrivateAccount.toString().lowercase()
                            }
                            
                            try {
                                selectedImageUri?.let { uri ->
                                    Log.d("EditProfileDialog", "Iniciando carga de imagen de perfil")
                                    isLoading = true
                                    val imageUrl = ImageUploader.uploadImageProfile(context, uri, user.id)
                                    Log.d("EditProfileDialog", "Imagen subida exitosamente: $imageUrl")
                                    updates["profile_image_url"] = imageUrl
                                    context.imageLoader.memoryCache?.clear()
                                    context.imageLoader.diskCache?.clear()
                                    Log.d("EditProfileDialog", "Cache de imágenes limpiado")
                                }
                                Log.d("EditProfileDialog", "Guardando actualizaciones: $updates")
                                onSave(updates)
                                onDismiss()
                            } catch (e: Exception) {
                                Log.e("EditProfileDialog", "Error al guardar el perfil", e)
                                when (e) {
                                    is ImageUploader.ImageUploadError.NetworkError -> {
                                        Log.e("EditProfileDialog", "Error de red al subir imagen", e)
                                        errorMessage = "Error de conexión. Por favor, inténtalo de nuevo."
                                    }
                                    is ImageUploader.ImageUploadError.ServerError -> {
                                        Log.e("EditProfileDialog", "Error del servidor al subir imagen", e)
                                        errorMessage = "Error en el servidor. Por favor, inténtalo más tarde."
                                    }
                                    else -> {
                                        Log.e("EditProfileDialog", "Error desconocido al subir imagen", e)
                                        errorMessage = "Error al actualizar la foto de perfil. Por favor, inténtalo de nuevo."
                                    }
                                }
                            } finally {
                                Log.d("EditProfileDialog", "Finalizando proceso de actualización")
                                isLoading = false
                                onDismiss()
                            }
                        }
                    }
                },
                enabled = !isLoading && emailError == null && phoneError == null && birthDateError == null
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Guardar")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    )
}