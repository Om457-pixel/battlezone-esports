package com.esports.tournament.data.api

import com.esports.tournament.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("auth/verify-token")
    suspend fun verifyToken(@Body body: Map<String, String>): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(): Response<Map<String, String>>

    @GET("auth/me")
    suspend fun getMe(): Response<User>

    @GET("auth/check-username")
    suspend fun checkUsername(@Query("username") username: String): Response<Map<String, Any>>

    // Matches
    @GET("matches/")
    suspend fun getMatches(
        @Query("status") status: String,
        @Query("game") game: String? = null,
        @Query("limit") limit: Int = 20,
        @Query("skip") skip: Int = 0
    ): Response<MatchListResponse>

    @GET("matches/{matchId}")
    suspend fun getMatch(@Path("matchId") matchId: String): Response<Match>

    @POST("matches/{matchId}/join")
    suspend fun joinMatch(@Path("matchId") matchId: String, @Body body: Map<String, String>): Response<Map<String, Any>>

    @POST("matches/{matchId}/result")
    suspend fun submitResult(@Path("matchId") matchId: String, @Body body: Map<String, Any>): Response<Map<String, Any>>

    @GET("matches/{matchId}/players")
    suspend fun getPlayers(@Path("matchId") matchId: String): Response<PlayersResponse>

    @GET("matches/featured")
    suspend fun getFeaturedMatches(): Response<MatchListResponse>

    // Users
    @GET("users/profile")
    suspend fun getProfile(): Response<User>

    @PUT("users/profile")
    suspend fun updateProfile(@Body body: Map<String, Any>): Response<Map<String, Any>>

    @GET("users/{username}")
    suspend fun getUserProfile(@Path("username") username: String): Response<User>

    @GET("users/match-history")
    suspend fun getMatchHistory(@Query("limit") limit: Int = 10): Response<MatchHistoryResponse>

    @POST("users/report")
    suspend fun reportUser(@Body body: Map<String, String>): Response<Map<String, Any>>

    // Wallet
    @GET("wallet/balance")
    suspend fun getBalance(): Response<WalletBalance>

    @POST("wallet/deposit/create-order")
    suspend fun createDepositOrder(@Body body: Map<String, Int>): Response<DepositOrder>

    @POST("wallet/deposit/verify")
    suspend fun verifyDeposit(@Body body: Map<String, Any>): Response<Map<String, Any>>

    @POST("wallet/withdraw")
    suspend fun withdraw(@Body body: Map<String, Any>): Response<Map<String, Any>>

    @GET("wallet/transactions")
    suspend fun getTransactions(@Query("limit") limit: Int = 20): Response<TransactionListResponse>

    // Leaderboard
    @GET("leaderboard/")
    suspend fun getLeaderboard(@Query("game") game: String? = null): Response<LeaderboardResponse>

    @GET("leaderboard/my-rank")
    suspend fun getMyRank(): Response<Map<String, Any>>

    // Rewards
    @POST("rewards/daily-claim")
    suspend fun claimDailyReward(): Response<DailyClaimResponse>

    @POST("rewards/spin")
    suspend fun spin(): Response<SpinResult>

    @GET("rewards/achievements")
    suspend fun getAchievements(): Response<AchievementsResponse>

    @GET("rewards/daily-schedule")
    suspend fun getDailySchedule(): Response<DailySchedule>

    // Chat
    @GET("chat/{matchId}/messages")
    suspend fun getChatMessages(@Path("matchId") matchId: String): Response<ChatMessagesResponse>

    // Notifications
    @GET("notifications/")
    suspend fun getNotifications(): Response<NotificationsResponse>

    @POST("notifications/mark-read")
    suspend fun markNotificationsRead(@Body body: Map<String, List<String>>): Response<Map<String, Any>>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): Response<Map<String, Int>>
}

// Response wrappers
data class AuthResponse(val accessToken: String = "", val refreshToken: String = "", val user: User = User(), val isNewUser: Boolean = false)
data class MatchListResponse(val matches: List<Match> = emptyList(), val count: Int = 0)
data class PlayersResponse(val players: List<JoinedPlayer> = emptyList())
data class MatchHistoryResponse(val history: List<Map<String, Any>> = emptyList())
data class TransactionListResponse(val transactions: List<Transaction> = emptyList())
data class LeaderboardResponse(val leaderboard: List<LeaderboardEntry> = emptyList())
data class AchievementsResponse(val achievements: List<Achievement> = emptyList())
data class DailyClaimResponse(val success: Boolean = false, val amount: Int = 0, val streak: Int = 0)
data class ChatMessagesResponse(val messages: List<ChatMessage> = emptyList())
data class NotificationsResponse(val notifications: List<Notification> = emptyList())
