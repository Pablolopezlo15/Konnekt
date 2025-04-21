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

    fun loadUserProfile(userId: String) {
        viewModelScope.launch {
            try {
                val user = KonnektApi.retrofitService.getUserProfile(userId)
                _userProfile.value = user
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
}