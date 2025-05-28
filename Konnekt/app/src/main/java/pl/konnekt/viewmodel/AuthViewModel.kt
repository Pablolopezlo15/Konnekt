package pl.konnekt.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.konnekt.models.LoginResponse
import pl.konnekt.models.RegisterResponse
import pl.konnekt.models.UserCreate
import pl.konnekt.network.KonnektApi

class AuthViewModel : ViewModel() {
    val loginState = MutableStateFlow<LoginResponse?>(null)
    val registerState = MutableStateFlow<RegisterResponse?>(null)
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun setError(message: String) {
        _error.value = message
    }

    private fun handleApiError(error: Exception): String {
        println("Error message: ${error}")
        return when (error) {
            is retrofit2.HttpException -> {
                try {
                    val errorBody = error.response()?.errorBody()?.string()
                    println("Error body: ${errorBody}")
                    if (errorBody != null) {
                        // Using proper escape sequences for the regex pattern
                        val detailPattern = """"detail":\s*"([^"]+)""""
                        val matchResult = Regex(detailPattern).find(errorBody)
                        val detail = matchResult?.groupValues?.get(1)
                        
                        when {
                            detail?.contains("Username already registered") == true -> 
                                "Ya existe un usuario con ese nombre"
                            detail?.contains("Email already registered") == true -> 
                                "Este correo electrónico ya está registrado"
                            detail?.contains("Phone number already registered") == true -> 
                                "Este número de teléfono ya está registrado"
                            detail?.contains("Invalid username format") == true -> 
                                "El nombre de usuario solo puede contener letras, números y guiones bajos"
                            detail?.contains("Invalid email format") == true -> 
                                "El formato del correo electrónico no es válido"
                            detail?.contains("Invalid phone format") == true -> 
                                "El formato del número de teléfono no es válido"
                            detail?.contains("User must be at least 13 years old") == true -> 
                                "Debes tener al menos 13 años para registrarte"
                            detail?.contains("Incorrect username or password") == true -> 
                                "Usuario o contraseña incorrectos"
                            detail != null -> detail
                            else -> "Ha ocurrido un error. Por favor, inténtalo de nuevo"
                        }
                    } else {
                        "Ha ocurrido un error. Por favor, inténtalo de nuevo"
                    }
                } catch (e: Exception) {
                    "Ha ocurrido un error. Por favor, inténtalo de nuevo"
                }
            }
            else -> "Ha ocurrido un error. Por favor, inténtalo de nuevo"
        }
    }

    fun login(username: String, password: String, onSuccess: (LoginResponse) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val loginResponse = KonnektApi.retrofitService.login(
                    mapOf(
                        "username" to username,
                        "password" to password
                    )
                )
                loginState.value = loginResponse
                onSuccess(loginResponse)
            } catch (e: Exception) {
                _error.value = handleApiError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun register(
        username: String,
        password: String,
        email: String,
        phone: String? = null,
        birthDate: String? = null,
        onSuccess: (RegisterResponse) -> Unit,
        showToast: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val userResponse = KonnektApi.retrofitService.register(
                    UserCreate(
                        username = username,
                        password = password,
                        email = email,
                        phone = phone,
                        birthDate = birthDate
                    )
                )
                registerState.value = userResponse
                onSuccess(userResponse)
                showToast("¡Registro exitoso! Ya puedes iniciar sesión")
            } catch (e: Exception) {
                _error.value = handleApiError(e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}