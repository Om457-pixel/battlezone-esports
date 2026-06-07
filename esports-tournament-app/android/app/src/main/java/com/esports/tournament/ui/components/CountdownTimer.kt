package com.esports.tournament.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esports.tournament.ui.theme.NeonCyan
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.temporal.ChronoUnit

@Composable
fun CountdownTimer(
    targetTimeIso: String,
    modifier: Modifier = Modifier,
    color: Color = NeonCyan
) {
    var timeLeft by remember { mutableStateOf(calculateTimeLeft(targetTimeIso)) }

    LaunchedEffect(targetTimeIso) {
        while (timeLeft.totalSeconds > 0) {
            delay(1000L)
            timeLeft = calculateTimeLeft(targetTimeIso)
        }
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (timeLeft.days > 0) {
            TimeUnit(value = timeLeft.days, label = "D", color = color)
            TimeSeparator(color)
        }
        TimeUnit(value = timeLeft.hours, label = "H", color = color)
        TimeSeparator(color)
        TimeUnit(value = timeLeft.minutes, label = "M", color = color)
        TimeSeparator(color)
        TimeUnit(value = timeLeft.seconds, label = "S", color = color)
    }
}

@Composable
private fun TimeUnit(value: Long, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimatedContent(
            targetState = value,
            transitionSpec = {
                slideInVertically { it } togetherWith slideOutVertically { -it }
            },
            label = "time_$label"
        ) { v ->
            Text(
                text = v.toString().padStart(2, '0'),
                color = color,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
        Text(text = label, color = color.copy(alpha = 0.6f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun TimeSeparator(color: Color) {
    Text(text = ":", color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
}

data class TimeLeft(val days: Long, val hours: Long, val minutes: Long, val seconds: Long, val totalSeconds: Long)

fun calculateTimeLeft(targetTimeIso: String): TimeLeft {
    return try {
        val target = Instant.parse(targetTimeIso)
        val now = Instant.now()
        val totalSeconds = ChronoUnit.SECONDS.between(now, target).coerceAtLeast(0)
        TimeLeft(
            days = totalSeconds / 86400,
            hours = (totalSeconds % 86400) / 3600,
            minutes = (totalSeconds % 3600) / 60,
            seconds = totalSeconds % 60,
            totalSeconds = totalSeconds
        )
    } catch (e: Exception) {
        TimeLeft(0, 0, 0, 0, 0)
    }
}
