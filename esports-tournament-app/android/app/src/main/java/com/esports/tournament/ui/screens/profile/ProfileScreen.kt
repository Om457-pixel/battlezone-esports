package com.esports.tournament.ui.screens.profile

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.esports.tournament.data.model.User
import com.esports.tournament.ui.components.GlassCard
import com.esports.tournament.ui.components.GradientBackground
import com.esports.tournament.ui.components.NeonGradientCard
import com.esports.tournament.ui.theme.*

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val user = uiState.user ?: return

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header with back button
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(text = "Profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            // Avatar + name section
            ProfileHeader(user = user)

            Spacer(Modifier.height(16.dp))

            // XP Progress bar
            XpProgressBar(user = user)

            Spacer(Modifier.height(16.dp))

            // Stats grid
            StatsGrid(user = user)

            Spacer(Modifier.height(16.dp))

            // Achievements
            AchievementsSection(achievements = uiState.achievements)

            Spacer(Modifier.height(16.dp))

            // Referral card
            ReferralCard(referralCode = user.referralCode)

            Spacer(Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ProfileHeader(user: User) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        // Background gradient
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
                .background(Brush.verticalGradient(listOf(NeonPurple.copy(0.3f), Color.Transparent)))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(20.dp))

            // Avatar with rank ring
            Box(contentAlignment = Alignment.Center) {
                // Rank ring
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(getRankGradient(user.rankTier))
                        )
                )
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(DarkBackground)
                ) {
                    if (user.avatar != null) {
                        AsyncImage(
                            model = user.avatar,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp).align(Alignment.Center)
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(text = user.username, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)

            Spacer(Modifier.height(4.dp))

            // Rank badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.horizontalGradient(getRankGradient(user.rankTier)))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${getRankEmoji(user.rankTier)} ${user.rankTier}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun XpProgressBar(user: User) {
    GlassCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Level ${user.level}", color = NeonPurple, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(text = "${user.xp} / ${user.xpToNextLevel} XP", color = Color(0xFF888899), fontSize = 12.sp)
            }
            Spacer(Modifier.height(8.dp))
            val progress = user.xp.toFloat() / user.xpToNextLevel.toFloat()
            val animatedProgress by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(1000, easing = EaseOutCubic),
                label = "xp_progress"
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color(0xFF2A2A3A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .height(8.dp)
                        .background(Brush.horizontalGradient(GradientPurpleBlue))
                )
            }
        }
    }
}

@Composable
private fun StatsGrid(user: User) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "📊 Statistics", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Matches", user.stats.matchesPlayed.toString(), NeonBlue, Modifier.weight(1f))
            StatCard("Wins", user.stats.matchesWon.toString(), NeonGreen, Modifier.weight(1f))
            StatCard("Kills", user.stats.totalKills.toString(), NeonOrange, Modifier.weight(1f))
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Win Rate", "${if (user.stats.matchesPlayed > 0) (user.stats.matchesWon * 100 / user.stats.matchesPlayed) else 0}%", NeonPurple, Modifier.weight(1f))
            StatCard("Earnings", "₹${user.totalEarnings.toInt()}", NeonGreen, Modifier.weight(1f))
            StatCard("Rank Pts", user.stats.rankPoints.toString(), NeonCyan, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    NeonGradientCard(
        modifier = modifier,
        gradientColors = listOf(color, color.copy(0.5f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, color = color, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
            Text(text = label, color = Color(0xFF888899), fontSize = 11.sp)
        }
    }
}

@Composable
private fun AchievementsSection(achievements: List<com.esports.tournament.data.model.Achievement>) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(text = "🏅 Achievements", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        achievements.chunked(3).forEach { row ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { ach ->
                    AchievementBadge(achievement = ach, modifier = Modifier.weight(1f))
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun AchievementBadge(achievement: com.esports.tournament.data.model.Achievement, modifier: Modifier = Modifier) {
    GlassCard(
        modifier = modifier,
        borderGradient = if (achievement.earned)
            listOf(NeonOrange.copy(0.6f), NeonPurple.copy(0.6f))
        else listOf(Color(0xFF333344), Color.Transparent)
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = achievement.icon,
                fontSize = 24.sp,
                color = if (achievement.earned) Color.White else Color(0xFF444455)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = achievement.title,
                color = if (achievement.earned) Color.White else Color(0xFF555566),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ReferralCard(referralCode: String) {
    NeonGradientCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        gradientColors = GradientCyanBlue
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎁", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(text = "Refer & Earn ₹50", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
            Spacer(Modifier.height(8.dp))
            Text(text = "Share your code and earn ₹50 for each friend who joins!", color = Color(0xFFB0B0C8), fontSize = 12.sp)
            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A1A28))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = referralCode, color = NeonCyan, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, letterSpacing = 2.sp)
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = NeonCyan, modifier = Modifier.size(20.dp))
            }
        }
    }
}

fun getRankGradient(tier: String): List<Color> = when (tier) {
    "Bronze" -> listOf(BronzeColor, Color(0xFF8B4513))
    "Silver" -> listOf(SilverColor, Color(0xFF808080))
    "Gold" -> listOf(GoldColor, NeonOrange)
    "Platinum" -> listOf(PlatinumColor, NeonBlue)
    "Diamond" -> listOf(DiamondColor, NeonPurple)
    "Master" -> listOf(MasterColor, NeonPink)
    "Legend" -> listOf(LegendColor, NeonOrange)
    else -> listOf(BronzeColor, Color(0xFF8B4513))
}

fun getRankEmoji(tier: String): String = when (tier) {
    "Bronze" -> "🥉"
    "Silver" -> "🥈"
    "Gold" -> "🥇"
    "Platinum" -> "💎"
    "Diamond" -> "💠"
    "Master" -> "👑"
    "Legend" -> "🔥"
    else -> "🥉"
}
