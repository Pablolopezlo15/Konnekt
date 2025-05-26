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
                                // Mantener políticas de caché deshabilitadas para la imagen seleccionada
                                .diskCachePolicy(CachePolicy.DISABLED)
                                .memoryCachePolicy(CachePolicy.DISABLED)
                                // Añadir placeholder y error drawable para consistencia y UX
                                .placeholder(R.drawable.default_profile_image)
                                .error(R.drawable.default_profile_image)
                                .build(),
                            contentDescription = "Selected profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            // Usar el ImageLoader personalizado
                            imageLoader = CoilConfig.getImageLoader(context)
                        )
                    } else {
                        val imageUrl = "${AppConfig.BASE_URL}${user.profileImageUrl}"
                        Log.d("EditProfileDialog", "Cargando imagen de perfil actual: $imageUrl")
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageUrl)
                                .crossfade(true)
                                // Eliminar políticas de caché deshabilitadas para usar las por defecto (habilitadas)
                                // .diskCachePolicy(CachePolicy.DISABLED)
                                // .memoryCachePolicy(CachePolicy.DISABLED)
                                // Añadir configuraciones de ProfileScreen para consistencia
                                .allowHardware(false)
                                .allowRgb565(true)
                                .memoryCacheKey(imageUrl)
                                .diskCacheKey(imageUrl)
                                .error(R.drawable.default_profile_image)
                                .placeholder(R.drawable.default_profile_image)
                                .build(),
                            contentDescription = "Current profile picture",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            // Usar el ImageLoader personalizado
                            imageLoader = CoilConfig.getImageLoader(context)
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
        // Añadir el confirmButton requerido
        confirmButton = {
            Button(
                onClick = {
                    if (validateEmail() && validatePhone() && validateBirthDate()) {
                        isLoading = true
                        errorMessage = null
                        scope.launch {
                            val updatedFields = mutableMapOf<String, String>()
                            if (email != user.email) updatedFields["email"] = email
                            if (phone != user.phone) updatedFields["phone"] = phone
                            if (birthDate != user.birthDate) updatedFields["birthDate"] = birthDate
                            if (isPrivateAccount != user.private_account) updatedFields["private_account"] = isPrivateAccount.toString()

                            selectedImageUri?.let {\ uri ->
                                withContext(Dispatchers.IO) {
                                    try {
                                        val inputStream = context.contentResolver.openInputStream(uri)
                                        if (inputStream != null) {
                                            val fileExtension = context.contentResolver.getType(uri)?.substringAfterLast('/') ?: "jpg"
                                            val uploadResult = ImageUploader.uploadImage(inputStream, fileExtension)
                                            if (uploadResult != null) {
                                                updatedFields["profileImageUrl"] = uploadResult.imageUrl // Asume que la respuesta tiene un campo imageUrl
                                                Log.d("EditProfileDialog", "Imagen subida con éxito: ${uploadResult.imageUrl}")
                                            } else {
                                                errorMessage = "Error al subir la imagen."
                                                Log.e("EditProfileDialog", "Error al subir la imagen: Resultado nulo")
                                            }
                                        } else {
                                            errorMessage = "No se pudo abrir el stream de la imagen."
                                            Log.e("EditProfileDialog", "No se pudo abrir el stream de la imagen.")
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Error al subir la imagen: ${e.message}"
                                        Log.e("EditProfileDialog", "Error al subir la imagen", e)
                                    }
                                }
                            }

                            if (updatedFields.isNotEmpty() && errorMessage == null) {
                                onSave(updatedFields)
                            } else if (errorMessage == null) {
                                // No hay campos para actualizar y no hay error de imagen
                                onDismiss()
                            }
                            isLoading = false
                        }
                    } else {
                        // Validation failed, error messages are already set
                    }
                },
                enabled = !isLoading // Deshabilitar el botón mientras se carga
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text("Guardar")
                }
            }
        }
    )
}