package pl.konnekt.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pl.konnekt.models.User
import pl.konnekt.network.KonnektApi
import pl.konnekt.network.KonnektApiService

class UserRepository(
    private val api: KonnektApiService = KonnektApi.retrofitService
) {
    suspend fun getAllUsers(): List<User> {
        return api.getAllUsers()
    }

    suspend fun getUserProfile(userId: String): User {
        return KonnektApi.retrofitService.getUserProfile(userId)
    }

    fun searchUsers(query: String): Flow<List<User>> = flow {
        try {
            val users = KonnektApi.retrofitService.searchUsers(query)
            emit(users)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }
}

