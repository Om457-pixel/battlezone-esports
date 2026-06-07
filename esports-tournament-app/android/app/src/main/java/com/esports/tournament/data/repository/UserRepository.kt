package com.esports.tournament.data.repository

import com.esports.tournament.data.api.ApiService
import com.esports.tournament.data.local.TokenStore
import com.esports.tournament.data.model.User
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore
) {
    suspend fun getCurrentUser(): Result<User> = runCatching {
        val response = api.getProfile()
        if (response.isSuccessful) response.body()!!
        else throw Exception(response.message())
    }

    suspend fun getUserProfile(username: String): Result<User> = runCatching {
        val response = api.getUserProfile(username)
        if (response.isSuccessful) response.body()!!
        else throw Exception(response.message())
    }

    suspend fun updateProfile(bio: String? = null, avatar: String? = null): Result<Unit> = runCatching {
        val body = mutableMapOf<String, Any>()
        bio?.let { body["bio"] = it }
        avatar?.let { body["avatar"] = it }
        val response = api.updateProfile(body)
        if (!response.isSuccessful) throw Exception(response.message())
    }
}
