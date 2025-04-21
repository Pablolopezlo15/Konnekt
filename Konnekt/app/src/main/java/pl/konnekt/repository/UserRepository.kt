package pl.konnekt.repository

import pl.konnekt.models.User
import pl.konnekt.network.KonnektApi

class UserRepository {
    suspend fun getUserProfile(userId: String): User {
        return KonnektApi.retrofitService.getUserProfile(userId)
    }
}