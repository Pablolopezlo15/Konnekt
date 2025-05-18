package pl.konnekt.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.konnekt.models.FriendRequest
import pl.konnekt.network.KonnektApi

class NotificationsViewModel : ViewModel() {
    private val _receivedRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val receivedRequests: StateFlow<List<FriendRequest>> = _receivedRequests.asStateFlow()

    private val _sentRequests = MutableStateFlow<List<FriendRequest>>(emptyList())
    val sentRequests: StateFlow<List<FriendRequest>> = _sentRequests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun loadReceivedRequests(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val requests = KonnektApi.retrofitService.getReceivedFollowRequests(userId)
                _receivedRequests.value = requests
            } catch (e: Exception) {
                _error.value = "Error loading received requests: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadSentRequests(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val requests = KonnektApi.retrofitService.getSentFollowRequests(userId)
                _sentRequests.value = requests
            } catch (e: Exception) {
                _error.value = "Error loading sent requests: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun acceptRequest(userId: String, requestId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedUser = KonnektApi.retrofitService.acceptFollowRequest(userId, requestId)
                _receivedRequests.value = _receivedRequests.value.filter { it.id != requestId }
            } catch (e: Exception) {
                _error.value = "Error accepting request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun rejectRequest(userId: String, requestId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val updatedUser = KonnektApi.retrofitService.rejectFollowRequest(userId, requestId)
                _receivedRequests.value = _receivedRequests.value.filter { it.id != requestId }
            } catch (e: Exception) {
                _error.value = "Error rejecting request: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}