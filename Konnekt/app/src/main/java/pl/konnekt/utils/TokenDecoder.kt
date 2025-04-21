package pl.konnekt.utils

import com.auth0.android.jwt.JWT

object TokenDecoder {
    fun getUserIdFromToken(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("user_id").asString()
        } catch (e: Exception) {
            null
        }
    }

    fun getUsername(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("username").asString()
        } catch (e: Exception) {
            null
        }
    }

    fun getEmail(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("email").asString()
        } catch (e: Exception) {
            null
        }
    }

    fun getProfileImageUrl(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("profile_image_url").asString()
        } catch (e: Exception) {
            null
        }
    }

    fun getPhone(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("phone").asString()
        } catch (e: Exception) {
            null
        }
    }

    fun getBirthDate(token: String): String? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("birth_date").asString()
        } catch (e: Exception) {
            null
        }
    }

    fun getFollowers(token: String): List<String>? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("followers").asList(String::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun getFollowing(token: String): List<String>? {
        return try {
            val jwt = JWT(token)
            jwt.getClaim("following").asList(String::class.java)
        } catch (e: Exception) {
            null
        }
    }
}