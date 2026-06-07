package com.esports.tournament.data.repository

import com.esports.tournament.data.api.ApiService
import com.esports.tournament.data.api.DailyClaimResponse
import com.esports.tournament.data.model.Achievement
import com.esports.tournament.data.model.DailySchedule
import com.esports.tournament.data.model.SpinResult
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardsRepository @Inject constructor(private val api: ApiService) {

    suspend fun getDailySchedule(): Result<DailySchedule> = runCatching {
        val response = api.getDailySchedule()
        if (response.isSuccessful) response.body()!!
        else throw Exception(response.message())
    }

    suspend fun claimDailyReward(): Result<DailyClaimResponse> = runCatching {
        val response = api.claimDailyReward()
        if (response.isSuccessful) response.body()!!
        else throw Exception(response.message())
    }

    suspend fun spin(): Result<SpinResult> = runCatching {
        val response = api.spin()
        if (response.isSuccessful) response.body()!!
        else throw Exception(response.message())
    }

    suspend fun getAchievements(): Result<List<Achievement>> = runCatching {
        val response = api.getAchievements()
        if (response.isSuccessful) response.body()!!.achievements
        else throw Exception(response.message())
    }
}
