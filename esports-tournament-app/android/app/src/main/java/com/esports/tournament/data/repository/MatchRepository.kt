package com.esports.tournament.data.repository

import com.esports.tournament.data.api.ApiService
import com.esports.tournament.data.model.JoinedPlayer
import com.esports.tournament.data.model.Match
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchRepository @Inject constructor(private val api: ApiService) {

    suspend fun getMatches(status: String, game: String? = null): Result<List<Match>> = runCatching {
        val response = api.getMatches(status, game)
        if (response.isSuccessful) response.body()!!.matches
        else throw Exception(response.message())
    }

    suspend fun getMatch(matchId: String): Result<Match> = runCatching {
        val response = api.getMatch(matchId)
        if (response.isSuccessful) response.body()!!
        else throw Exception(response.message())
    }

    suspend fun getFeaturedMatches(): Result<List<Match>> = runCatching {
        val response = api.getFeaturedMatches()
        if (response.isSuccessful) response.body()!!.matches
        else throw Exception(response.message())
    }

    suspend fun joinMatch(matchId: String, inGameName: String): Result<Unit> = runCatching {
        val response = api.joinMatch(matchId, mapOf("in_game_name" to inGameName))
        if (!response.isSuccessful) throw Exception(response.message())
    }

    suspend fun getPlayers(matchId: String): Result<List<JoinedPlayer>> = runCatching {
        val response = api.getPlayers(matchId)
        if (response.isSuccessful) response.body()!!.players
        else throw Exception(response.message())
    }

    suspend fun hasJoined(matchId: String): Result<Boolean> = runCatching {
        // Check from local cache or players list
        false
    }
}
