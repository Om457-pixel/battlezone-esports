package com.esports.tournament.ui.screens.leaderboard

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esports.tournament.data.model.LeaderboardEntry
import com.esports.tournament.ui.components.GlassCard
import com.esports.tournament.ui.components.GradientBackground
import com.esports.tournament.ui.screens.profile.getRankGradient
import com.esports.tournament.ui.theme.*

@Composable
fun LeaderboardScreen(viewModel: LeaderboardViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val games = listOf("All", "Free Fire MAX", "PUBG Mobile", "COD Mobile")
    var selectedGame by remember { mutableStateOf("All") }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(NeonPurple.copy(0.2f), Color.Transparent)))
                    .padding(16.dp)
            ) {
                Column {
                    Text(text = "🏆 Leaderboard", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                    Text(text = "Top players this season", color = Color(0xFF888899), fontSize = 13.sp)
                }
            }

            // Game filter
            ScrollableTabRow(
                selectedTabIndex = games.indexOf(selectedGame),
                containerColor = Color.Transparent,
                contentColor = NeonPurple,
                edgePadding = 16.dp
            ) {
                games.forEachIndexed { _, game ->
                    Tab(
                        selected = selectedGame == game,
                        onClick = {
                            selectedGame = game
                            viewModel.loadLeaderboard(if (game == "All") null else game)
                        },
                        text = {
                            Text(
                                text = game,
                                color = if (selectedGame == game) NeonPurple else Color(0xFF888899),
                                fontWeight = if (selectedGame == game) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
            }

            // Top 3 podium
            if (uiState.entries.size >= 3) {
                TopThreePodium(entries = uiState.entries.take(3))
            }

            // Full list
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(uiState.entries.drop(3)) { index, entry ->
                    LeaderboardRow(
                        entry = entry,
                        rank = index + 4,
                        isCurrentUser = entry.uid == uiState.currentUserId,
                        animationDelay = index * 50
                    )
                }
            }
        }
    }
}

@Composable
private fun TopThreePodium(entries: List<LeaderboardEntry>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // 2nd place
        PodiumItem(entry = entries[1], rank = 2, height = 100.dp, color = SilverColor)
        // 1st place
        PodiumItem(entry = entries[0], rank = 1, height = 130.dp, color = GoldColor)
        // 3rd place
        PodiumItem(entry = entries[2], rank = 3, height = 80.dp, color = BronzeColor)
    }
}

@Composable
private fun PodiumItem(
    entry: LeaderboardEntry,
    rank: Int,
    height: androidx.compose.ui.unit.Dp,
    color: Color
) {
    val infiniteTransition = rememberInfiniteTransition(label = "podium_$rank")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = if (rank == 1) 1.05f else 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "podium_scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(scale)
    ) {
        // Crown for 1st
        if (rank == 1) Text(text = "👑", fontSize = 20.sp)

        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(color, color.copy(0.5f)))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = entry.username.take(1).uppercase(),
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(text = entry.username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp, maxLines = 1)
        Text(text = "${entry.stats.rankPoints} pts", color = color, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)

        Spacer(Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height)
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(Brush.verticalGradient(listOf(color.copy(0.4f), color.copy(0.1f)))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "#$rank",
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp
            )
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    rank: Int,
    isCurrentUser: Boolean,
    animationDelay: Int
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn()
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderGradient = if (isCurrentUser)
                listOf(NeonPurple.copy(0.8f), NeonBlue.copy(0.8f))
            else listOf(Color(0xFF2A2A3A), Color.Transparent)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank number
                Text(
                    text = "#$rank",
                    color = if (rank <= 10) NeonOrange else Color(0xFF888899),
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.width(36.dp)
                )

                // Avatar
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(getRankGradient(entry.rankTier))),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = entry.username.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = entry.username, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (isCurrentUser) {
                            Spacer(Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonPurple.copy(0.3f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(text = "YOU", color = NeonPurple, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(text = "Lv.${entry.level} • ${entry.rankTier}", color = Color(0xFF888899), fontSize = 11.sp)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "${entry.stats.rankPoints}", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "pts", color = Color(0xFF888899), fontSize = 10.sp)
                }
            }
        }
    }
}
