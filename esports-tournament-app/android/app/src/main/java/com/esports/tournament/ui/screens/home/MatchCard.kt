package com.esports.tournament.ui.screens.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.esports.tournament.data.model.Match
import com.esports.tournament.ui.components.CountdownTimer
import com.esports.tournament.ui.components.GlassCard
import com.esports.tournament.ui.theme.*

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    // Pulsing glow for live matches
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkCard)
            .clickable {
                isPressed = true
                onClick()
            }
    ) {
        // Banner image
        if (match.bannerUrl != null) {
            AsyncImage(
                model = match.bannerUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(120.dp),
                contentScale = ContentScale.Crop
            )
            // Gradient overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, DarkCard),
                            startY = 40f
                        )
                    )
            )
        }

        Column(modifier = Modifier.padding(16.dp)) {
            if (match.bannerUrl != null) Spacer(Modifier.height(80.dp))

            // Status badge + game tag
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(status = match.status, glowAlpha = glowAlpha)
                GameTag(game = match.game)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = match.title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                maxLines = 1
            )

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Map, contentDescription = null, tint = NeonCyan.copy(0.7f), modifier = Modifier.size(12.dp))
                Spacer(Modifier.width(4.dp))
                Text(text = "${match.mode} • ${match.map}", color = Color(0xFFB0B0C8), fontSize = 12.sp)
            }

            Spacer(Modifier.height(12.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatChip(label = "Entry", value = if (match.entryFee == 0.0) "FREE" else "₹${match.entryFee.toInt()}", color = NeonGreen)
                StatChip(label = "Prize", value = "₹${match.prizePool.toInt()}", color = NeonOrange)
                StatChip(label = "Players", value = "${match.currentPlayers}/${match.maxPlayers}", color = NeonBlue)
            }

            Spacer(Modifier.height(12.dp))

            // Progress bar for slots
            val progress = match.currentPlayers.toFloat() / match.maxPlayers.toFloat()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF2A2A3A))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .height(4.dp)
                        .background(Brush.horizontalGradient(GradientPurpleBlue))
                )
            }

            Spacer(Modifier.height(12.dp))

            // Bottom row: countdown + join button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (match.status == "upcoming") {
                    Column {
                        Text(text = "Starts in", color = Color(0xFF888899), fontSize = 10.sp)
                        CountdownTimer(targetTimeIso = match.startTime)
                    }
                } else if (match.status == "live") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(NeonGreen.copy(alpha = glowAlpha), shape = RoundedCornerShape(4.dp))
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(text = "LIVE NOW", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Brush.horizontalGradient(GradientPurpleBlue))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (match.status == "live") "VIEW" else "JOIN",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(status: String, glowAlpha: Float) {
    val (color, label) = when (status) {
        "live" -> Pair(NeonGreen, "🔴 LIVE")
        "upcoming" -> Pair(NeonBlue, "⏰ UPCOMING")
        "completed" -> Pair(Color(0xFF888899), "✅ ENDED")
        else -> Pair(Color.Gray, status.uppercase())
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = if (status == "live") glowAlpha * 0.3f else 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun GameTag(game: String) {
    val color = when {
        game.contains("Free Fire") -> NeonOrange
        game.contains("PUBG") -> NeonBlue
        game.contains("Call of Duty") -> NeonGreen
        else -> NeonPurple
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = game.take(10), color = color, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, color = color, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(text = label, color = Color(0xFF888899), fontSize = 10.sp)
    }
}
