package com.esports.tournament.ui.screens.match

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.esports.tournament.ui.components.*
import com.esports.tournament.ui.theme.*

@Composable
fun MatchDetailScreen(
    matchId: String,
    onBack: () -> Unit,
    viewModel: MatchDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showJoinDialog by remember { mutableStateOf(false) }
    var showChatPanel by remember { mutableStateOf(false) }

    LaunchedEffect(matchId) { viewModel.loadMatch(matchId) }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonPurple)
            }
            return@GradientBackground
        }

        val match = uiState.match ?: return@GradientBackground

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { Text(match.title, color = Color.White, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { showChatPanel = !showChatPanel }) {
                            Icon(Icons.Default.Chat, contentDescription = "Chat", tint = NeonCyan)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                )
            },
            bottomBar = {
                if (match.status == "upcoming" && !uiState.hasJoined) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface)
                            .padding(16.dp)
                    ) {
                        NeonButton(
                            text = if (match.entryFee == 0.0) "JOIN FREE" else "JOIN FOR ₹${match.entryFee.toInt()}",
                            onClick = { showJoinDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            gradientColors = if (match.entryFee == 0.0) listOf(NeonGreen, NeonBlue) else GradientPurpleBlue
                        )
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Banner
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(16.dp))
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
                                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xAA000000))))
                        )
                        // Prize pool overlay
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                        ) {
                            Text(text = "PRIZE POOL", color = Color(0xFFB0B0C8), fontSize = 11.sp)
                            Text(
                                text = "₹${match.prizePool.toInt()}",
                                color = NeonOrange,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            )
                        }
                    }
                }

                // Stats grid
                item {
                    NeonGradientCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "Match Details", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                DetailStat("Game", match.game)
                                DetailStat("Mode", match.mode)
                                DetailStat("Map", match.map)
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                DetailStat("Entry", if (match.entryFee == 0.0) "FREE" else "₹${match.entryFee.toInt()}", NeonGreen)
                                DetailStat("Players", "${match.currentPlayers}/${match.maxPlayers}", NeonBlue)
                                DetailStat("Slots Left", "${match.maxPlayers - match.currentPlayers}", NeonOrange)
                            }
                        }
                    }
                }

                // Countdown
                if (match.status == "upcoming") {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "Match Starts In", color = Color(0xFF888899), fontSize = 12.sp)
                                Spacer(Modifier.height(8.dp))
                                CountdownTimer(
                                    targetTimeIso = match.startTime,
                                    color = NeonCyan
                                )
                            }
                        }
                    }
                }

                // Room credentials (revealed 15 min before)
                if (uiState.hasJoined && match.roomId != null) {
                    item {
                        NeonGradientCard(
                            modifier = Modifier.fillMaxWidth(),
                            gradientColors = GradientGold
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = NeonOrange, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text(text = "Room Credentials", color = NeonOrange, fontWeight = FontWeight.Bold)
                                }
                                Spacer(Modifier.height(12.dp))
                                CredentialRow("Room ID", match.roomId)
                                Spacer(Modifier.height(8.dp))
                                CredentialRow("Password", match.roomPassword ?: "")
                            }
                        }
                    }
                }

                // Prize distribution
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "🏆 Prize Distribution", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(Modifier.height(12.dp))
                            match.prizeDistribution.forEachIndexed { index, prize ->
                                PrizeRow(rank = prize.rank, amount = prize.prize, index = index)
                                if (index < match.prizeDistribution.size - 1) {
                                    Divider(color = Color(0xFF2A2A3A), modifier = Modifier.padding(vertical = 6.dp))
                                }
                            }
                        }
                    }
                }

                // Players list
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = "👥 Registered Players (${uiState.players.size})", color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            uiState.players.take(10).forEach { player ->
                                PlayerRow(player = player)
                                Spacer(Modifier.height(6.dp))
                            }
                        }
                    }
                }

                // Rules
                if (match.rules.isNotEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(text = "📋 Rules", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(Modifier.height(8.dp))
                                Text(text = match.rules, color = Color(0xFFB0B0C8), fontSize = 13.sp, lineHeight = 20.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showJoinDialog) {
        JoinMatchDialog(
            match = match,
            onDismiss = { showJoinDialog = false },
            onJoin = { inGameName ->
                viewModel.joinMatch(matchId, inGameName)
                showJoinDialog = false
            }
        )
    }
}

@Composable
private fun DetailStat(label: String, value: String, color: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = label, color = Color(0xFF888899), fontSize = 11.sp)
    }
}

@Composable
private fun CredentialRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF1A1A28))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xFF888899), fontSize = 13.sp)
        Text(text = value, color = NeonOrange, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
private fun PrizeRow(rank: Int, amount: Double, index: Int) {
    val (emoji, color) = when (rank) {
        1 -> Pair("🥇", GoldColor)
        2 -> Pair("🥈", SilverColor)
        3 -> Pair("🥉", BronzeColor)
        else -> Pair("#$rank", Color(0xFF888899))
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 18.sp)
        Text(text = "Rank $rank", color = color, fontWeight = FontWeight.SemiBold)
        Text(text = "₹${amount.toInt()}", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun PlayerRow(player: com.esports.tournament.data.model.JoinedPlayer) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(GradientPurpleBlue)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = player.inGameName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }
        Spacer(Modifier.width(10.dp))
        Text(text = player.inGameName, color = Color.White, fontSize = 13.sp)
        Spacer(Modifier.weight(1f))
        Text(text = "Slot #${player.slot}", color = Color(0xFF888899), fontSize = 11.sp)
    }
}

@Composable
private fun JoinMatchDialog(
    match: com.esports.tournament.data.model.Match,
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var inGameName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = { Text("Join ${match.title}", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column {
                Text(
                    text = "Entry Fee: ${if (match.entryFee == 0.0) "FREE" else "₹${match.entryFee.toInt()}"}",
                    color = NeonGreen, fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = inGameName,
                    onValueChange = { inGameName = it },
                    label = { Text("Your In-Game Name", color = Color(0xFF888899)) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPurple,
                        unfocusedBorderColor = Color(0xFF333344),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            NeonButton(
                text = "CONFIRM JOIN",
                onClick = { if (inGameName.isNotBlank()) onJoin(inGameName) },
                enabled = inGameName.isNotBlank()
            )
        },
        dismissButton = {
            OutlineNeonButton(text = "Cancel", onClick = onDismiss)
        }
    )
}
