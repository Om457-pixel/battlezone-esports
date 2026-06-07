package com.esports.tournament.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.esports.tournament.ui.theme.*

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderGradient: List<Color> = listOf(GlassBorder, Color.Transparent),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0x26FFFFFF),
                        Color(0x0DFFFFFF)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(borderGradient),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

@Composable
fun NeonGradientCard(
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = GradientPurpleBlue,
    cornerRadius: Dp = 16.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = gradientColors.map { it.copy(alpha = 0.15f) }
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.linearGradient(gradientColors.map { it.copy(alpha = 0.5f) }),
                shape = RoundedCornerShape(cornerRadius)
            ),
        content = content
    )
}

@Composable
fun GradientBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.background(
            Brush.radialGradient(
                colors = listOf(
                    Color(0xFF1A0A2E),
                    Color(0xFF0A0A1A),
                    DarkBackground
                ),
                radius = 1200f
            )
        ),
        content = content
    )
}
