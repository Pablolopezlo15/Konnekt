package pl.konnekt.utils

object TokenManager {
    private var token: String? = null

    fun setToken(newToken: String) {
        token = newToken
    }

    fun getToken(): String? {
        return token
    }

    fun clearToken() {
        token = null
    }
}