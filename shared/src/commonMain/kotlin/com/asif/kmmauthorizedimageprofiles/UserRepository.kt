package com.asif.kmmauthorizedimageprofiles

open class UserRepository(private val api: ApiService) {

    suspend fun register(email: String, password: String): TokenResponse? {
        return api.register(email, password)
    }

    suspend fun login(email: String, password: String): TokenResponse? {
        return api.login(email, password)
    }

    suspend fun getProfile(userId: String, token: String): ProfileResponse? {
        return api.getProfile(userId, token)
    }

    suspend fun updateAvatar(userId: String, avatar: String, token: String): AvatarResponse? {
        return api.updateAvatar(userId, avatar, token)
    }
}
