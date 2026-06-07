package com.esports.tournament.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.pager.*
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
import com.esports.tournament.data.model.Match
import com.esports.tournament.ui.components.GlassCard
import com.esports.tournament.ui.components.GradientBackground
import com.esports.tournament.ui.theme.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    onMatchClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val tabs = listOf("Live", "Upcoming", "Completed")
    var selectedTab by remember { mutableIntStateOf(1) }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar
            HomeTopBar(
                username = uiState.username,
                walletBalance = uiState.walletBalance,
                avatarUrl = uiState.avatarUrl,
                notificationCount = uiState.unreadNotifications,
                onProfileClick = onProfileClick,
                onWalletClick = onWalletClick
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // Banner carousel
                item {
                    BannerCarousel(
                        matches = uiState.featuredMatches,
                        onMatchClick = onMatchClick
                    )
                }

                // Daily reward strip
                item {
                    DailyRewardStrip(
                        streak = uiState.streak,
                        canClaim = uiState.canClaimDaily,
                        onClaim = { viewModel.claimDailyReward() }
                    )
                }

                // Game filter chips
                item {
                    GameFilterRow(
                        selectedGame = uiState.selectedGame,
                        onGameSelected = { viewModel.filterByGame(it) }
                    )
                }

                // Tab row
                item {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        contentColor = NeonPurple,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = NeonPurple
                            )
                        }
                    ) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = {
                                    selectedTab = index
                                    viewModel.loadMatches(tabs[index].lowercase())
                                },
                                text = {
                                    Text(
                                        text = title,
                                        color = if (selectedTab == index) NeonPurple else Color(0xFF888899),
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            )
                        }
                    }
                }

                // Match list
                if (uiState.isLoading) {
                    items(3) { MatchCardSkeleton() }
                } else if (uiState.matches.isEmpty()) {
                    item { EmptyMatchesView() }
                } else {
                    items(uiState.matches, key = { it.matchId }) { match ->
                        MatchCard(
                            match = match,
                            onClick = { onMatchClick(match.matchId) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    username: String,
    walletBalance: Double,
    avatarUrl: String?,
    notificationCount: Int,
    onProfileClick: () -> Unit,
    onWalletClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable(onClick = onProfileClick)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(GradientPurpleBlue))
            ) {
                if (avatarUrl != null) {
                    AsyncImage(model = avatarUrl, contentDescription = null,
                        modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } else {
                    Icon(Icons.Default.Person, contentDescription = null,
                        tint = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(text = "Welcome back,", color = Color(0xFF888899), fontSize = 11.sp)
                Text(text = username, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Wallet chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Brush.horizontalGradient(listOf(NeonGreen.copy(0.2f), NeonBlue.copy(0.2f))))
                    .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(20.dp))
                    .clickable(onClick = onWalletClick)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "₹", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(text = "%.0f".format(walletBalance), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Notification bell
            Box {
                IconButton(onClick = {}) {
                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                }
                if (notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(NeonPink, CircleShape)
                            .align(Alignment.TopEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = notificationCount.toString(), color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BannerCarousel(matches: List<Match>, onMatchClick: (String) -> Unit) {
    if (matches.isEmpty()) return

    val pagerState = rememberPagerState { matches.size }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3500)
            val next = (pagerState.currentPage + 1) % matches.size
            pagerState.animateScrollToPage(next)
        }
    }

    Column(modifier = Modifier.padding(vertical = 12.dp)) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp
        ) { page ->
            BannerItem(match = matches[page], onClick = { onMatchClick(matches[page].matchId) })
        }

        Spacer(Modifier.height(8.dp))

        // Dot indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(matches.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .size(if (isSelected) 20.dp else 6.dp, 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(if (isSelected) NeonPurple else Color(0xFF444455))
                )
            }
        }
    }
}

@Composable
private fun BannerItem(match: Match, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        AsyncImage(
            model = match.bannerUrl ?: "https://picsum.photos/800/400",
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color(0xCC000000)),
                        startY = 60f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(text = match.title, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrizeBadge(text = "Prize: ₹${match.prizePool.toInt()}")
                PrizeBadge(text = if (match.entryFee == 0.0) "FREE" else "Entry: ₹${match.entryFee.toInt()}")
            }
        }
    }
}

@Composable
private fun PrizeBadge(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NeonPurple.copy(alpha = 0.8f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = text, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DailyRewardStrip(streak: Int, canClaim: Boolean, onClaim: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "reward_glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "reward_alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(NeonOrange.copy(0.15f), NeonPurple.copy(0.15f))
                )
            )
            .border(1.dp, if (canClaim) NeonOrange.copy(glowAlpha) else Color(0xFF333344), RoundedCornerShape(12.dp))
            .clickable(enabled = canClaim, onClick = onClaim)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔥", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(text = "Day $streak Streak", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = if (canClaim) "Tap to claim daily reward!" else "Come back tomorrow", color = Color(0xFF888899), fontSize = 11.sp)
                }
            }
            if (canClaim) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(GradientGold))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "CLAIM", color = Color.Black, fontWeight = FontWeight.ExtraBold, fontSize = 12.sp)
                }
            } else {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = NeonGreen, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
private fun GameFilterRow(selectedGame: String?, onGameSelected: (String?) -> Unit) {
    val games = listOf("All", "Free Fire MAX", "PUBG Mobile", "COD Mobile", "BGMI")
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(games) { game ->
            val isSelected = (game == "All" && selectedGame == null) || game == selectedGame
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (isSelected) Brush.horizontalGradient(GradientPurpleBlue)
                        else Brush.horizontalGradient(listOf(DarkCard, DarkCard))
                    )
                    .border(
                        1.dp,
                        if (isSelected) Color.Transparent else Color(0xFF333344),
                        RoundedCornerShape(20.dp)
                    )
                    .clickable { onGameSelected(if (game == "All") null else game) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    text = game,
                    color = if (isSelected) Color.White else Color(0xFF888899),
                    fontSize = 12.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun MatchCardSkeleton() {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.7f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "skeleton_alpha"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard.copy(alpha = alpha))
    )
}

@Composable
private fun EmptyMatchesView() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "🎮", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(text = "No matches found", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(text = "Check back soon for new tournaments", color = Color(0xFF888899), fontSize = 14.sp)
    }
}
