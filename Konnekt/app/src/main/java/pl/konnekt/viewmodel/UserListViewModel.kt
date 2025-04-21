package pl.konnekt.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pl.konnekt.models.User
import pl.konnekt.models.UserListResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import pl.konnekt.models.createPabloUser
import pl.konnekt.network.KonnektApi

class UserListViewModel : ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users = _users.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val userList = KonnektApi.retrofitService.getAllUsers()
                _users.value = userList
            } catch (e: Exception) {
                _error.value = "Error loading users: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}