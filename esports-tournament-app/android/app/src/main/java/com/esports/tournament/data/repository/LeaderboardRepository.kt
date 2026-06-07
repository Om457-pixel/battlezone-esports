package com.esports.tournament.data.repository

import com.esports.tournament.data.api.ApiService
import com.esports.tournament.data.model.LeaderboardEntry
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeaderboardRepository @Inject constructor(private val api: ApiService) {

    suspend fun getLeaderboard(game: String? = null): Result<List<LeaderboardEntry>> = runCatching {
        val response = api.getLeaderboard(game)
        if (response.isSuccessful) response.body()!!.leaderboard
        else throw Exception(response.message())
    }
}
