package pl.konnekt.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.konnekt.models.User
import pl.konnekt.network.KonnektApi
import pl.konnekt.repository.UserRepository
class UserViewModel : ViewModel() {
    private val _userProfile = MutableStateFlow<User?>(null)
    val userProfile = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _followRequestStatus = MutableStateFlow<String?>(null)
    val followRequestStatus = _followRequestStatus.asStateFlow()

    private val _canViewPosts = MutableStateFlow(true)
    val canViewPosts = _canViewPosts.asStateFlow()

    fun checkFollowRequestStatus(userId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                val request = KonnektApi.retrofitService.checkFollowRequest(userId, currentUserId)
                _followRequestStatus.value = if (request != null && request.status == "pending") "pending" else null
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error checking follow request: ${e.message}")
            }
        }
    }

    fun followUser(userId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedUser = KonnektApi.retrofitService.followUser(userId, currentUserId)
                
                if (updatedUser.private_account) {
                    _followRequestStatus.value = "pending"
                    _error.value = "Solicitud de seguimiento enviada"
                } else {
                    _followRequestStatus.value = "following"
                    _userProfile.value = updatedUser
                }
            } catch (e: retrofit2.HttpException) {
                Log.e("UserViewModel", "HTTP error code: ${e.code()}")
                Log.e("UserViewModel", "HTTP error body: ${e.response()?.errorBody()?.string()}")
                _error.value = "Error al seguir al usuario: ${e.message}"
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error following user", e)
                _error.value = "Error al seguir al usuario: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun unfollowUser(userId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedUser = KonnektApi.retrofitService.unfollowUser(userId, currentUserId)
                _userProfile.value = updatedUser
                _followRequestStatus.value = null
            } catch (e: Exception) {
                _error.value = "Error al dejar de seguir: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserProfile(userId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = KonnektApi.retrofitService.getUserProfile(userId)
                _userProfile.value = user
                _canViewPosts.value = !user.private_account || user.followers.contains(currentUserId)
                checkFollowRequestStatus(userId, currentUserId)
                Log.d("UserViewModel", "User profile loaded: $user")
            } catch (e: Exception) {
                _error.value = "Error al cargar el perfil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(userId: String, updates: Map<String, String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                Log.d("UserViewModel", "Sending updates: $updates")
                
                if (updates.isEmpty()) {
                    Log.d("UserViewModel", "No changes detected, skipping update.")
                    _error.value = "No hay cambios para actualizar."
                    return@launch
                }
                
                val response = KonnektApi.retrofitService.updateProfile(userId, updates)
                _userProfile.value = response
                _canViewPosts.value = !response.private_account || response.followers.contains(userId)
                Log.d("UserViewModel", "Profile updated successfully: $response")
            } catch (e: retrofit2.HttpException) {
                if (e.code() == 304) {
                    Log.d("UserViewModel", "Profile not modified, no changes needed.")
                    _error.value = "El perfil no fue modificado, no se necesitan cambios."
                } else if (e.code() == 500) {
                    Log.e("UserViewModel", "HTTP error code: ${e.code()}")
                    Log.e("UserViewModel", "HTTP error body: ${e.response()?.errorBody()?.string()}")
                    _error.value = "Error en el servidor al actualizar la imagen. Por favor, inténtalo de nuevo más tarde."
                } else {
                    _error.value = "Error al actualizar el perfil: ${e.message}"
                }
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error updating profile: ${e.message}")
                _error.value = "Error al actualizar el perfil: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}