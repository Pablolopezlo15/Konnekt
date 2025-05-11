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
import pl.konnekt.models.UserResponse
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
                _error.value = "Error: ${e.localizedMessage}"
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
        onSuccess: (RegisterResponse) -> Unit
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
            } catch (e: Exception) {
                _error.value = "Error: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}