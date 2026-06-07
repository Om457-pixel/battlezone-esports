package com.esports.tournament.data.model

import com.google.gson.annotations.SerializedName

data class User(
    val uid: String = "",
    val username: String = "",
    val phone: String? = null,
    val email: String? = null,
    val avatar: String? = null,
    val bio: String = "",
    val level: Int = 1,
    val xp: Int = 0,
    @SerializedName("xp_to_next_level") val xpToNextLevel: Int = 100,
    @SerializedName("wallet_balance") val walletBalance: Double = 0.0,
    @SerializedName("bonus_balance") val bonusBalance: Double = 0.0,
    @SerializedName("total_earnings") val totalEarnings: Double = 0.0,
    @SerializedName("referral_code") val referralCode: String = "",
    val stats: UserStats = UserStats(),
    @SerializedName("rank_tier") val rankTier: String = "Bronze",
    @SerializedName("trust_score") val trustScore: Int = 100,
    val achievements: List<String> = emptyList(),
    val badges: List<String> = emptyList(),
    val streak: Int = 0,
    @SerializedName("spin_available") val spinAvailable: Boolean = false,
    @SerializedName("is_banned") val isBanned: Boolean = false
)

data class UserStats(
    @SerializedName("matches_played") val matchesPlayed: Int = 0,
    @SerializedName("matches_won") val matchesWon: Int = 0,
    @SerializedName("total_kills") val totalKills: Int = 0,
    @SerializedName("win_rate") val winRate: Double = 0.0,
    @SerializedName("rank_points") val rankPoints: Int = 0
)

data class Match(
    @SerializedName("match_id") val matchId: String = "",
    val title: String = "",
    val game: String = "",
    val mode: String = "Squad",
    val map: String = "Bermuda",
    @SerializedName("entry_fee") val entryFee: Double = 0.0,
    @SerializedName("prize_pool") val prizePool: Double = 0.0,
    @SerializedName("prize_distribution") val prizeDistribution: List<PrizeDistribution> = emptyList(),
    @SerializedName("max_players") val maxPlayers: Int = 100,
    @SerializedName("current_players") val currentPlayers: Int = 0,
    @SerializedName("start_time") val startTime: String = "",
    val status: String = "upcoming",
    @SerializedName("room_id") val roomId: String? = null,
    @SerializedName("room_password") val roomPassword: String? = null,
    @SerializedName("banner_url") val bannerUrl: String? = null,
    val rules: String = "",
    @SerializedName("is_featured") val isFeatured: Boolean = false
)

data class PrizeDistribution(
    val rank: Int = 1,
    val prize: Double = 0.0
)

data class JoinedPlayer(
    @SerializedName("match_id") val matchId: String = "",
    @SerializedName("user_id") val userId: String = "",
    @SerializedName("in_game_name") val inGameName: String = "",
    @SerializedName("team_name") val teamName: String? = null,
    val slot: Int = 0,
    val kills: Int = 0,
    val rank: Int? = null,
    @SerializedName("prize_won") val prizeWon: Double = 0.0
)

data class Transaction(
    @SerializedName("tx_id") val txId: String = "",
    @SerializedName("user_id") val userId: String = "",
    val type: String = "",
    val amount: Double = 0.0,
    val description: String = "",
    val status: String = "completed",
    @SerializedName("created_at") val createdAt: String = ""
)

data class LeaderboardEntry(
    val uid: String = "",
    val username: String = "",
    val avatar: String? = null,
    val level: Int = 1,
    @SerializedName("rank_tier") val rankTier: String = "Bronze",
    val stats: UserStats = UserStats(),
    @SerializedName("total_earnings") val totalEarnings: Double = 0.0,
    val rank: Int = 0
)

data class Achievement(
    val id: String = "",
    val title: String = "",
    val desc: String = "",
    val xp: Int = 0,
    val icon: String = "",
    val earned: Boolean = false
)

data class DailySchedule(
    val rewards: List<Int> = emptyList(),
    @SerializedName("current_streak") val currentStreak: Int = 0,
    @SerializedName("claimed_today") val claimedToday: Boolean = false,
    @SerializedName("current_day") val currentDay: Int = 0
)

data class SpinResult(
    val success: Boolean = false,
    val prize: SpinPrize = SpinPrize()
)

data class SpinPrize(
    val label: String = "",
    val amount: Double = 0.0
)

data class WalletBalance(
    @SerializedName("wallet_balance") val walletBalance: Double = 0.0,
    @SerializedName("bonus_balance") val bonusBalance: Double = 0.0
)

data class DepositOrder(
    @SerializedName("order_id") val orderId: String = "",
    val amount: Int = 0,
    val currency: String = "INR"
)

data class ChatMessage(
    @SerializedName("match_id") val matchId: String = "",
    @SerializedName("user_id") val userId: String = "",
    val username: String = "",
    val avatar: String? = null,
    val level: Int = 1,
    val message: String = "",
    val timestamp: String = ""
)

data class Notification(
    @SerializedName("notif_id") val notifId: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val read: Boolean = false,
    @SerializedName("created_at") val createdAt: String = ""
)
