package pl.konnekt.viewmodel

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

    fun followUser(userId: String, currentUserId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedUser = KonnektApi.retrofitService.followUser(userId, currentUserId)
                _userProfile.value = updatedUser
            } catch (e: Exception) {
                _error.value = e.message
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
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val user = KonnektApi.retrofitService.getUserProfile(userId)
                _userProfile.value = user
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}