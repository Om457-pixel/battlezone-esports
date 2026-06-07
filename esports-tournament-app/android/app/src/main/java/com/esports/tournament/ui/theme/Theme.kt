package com.esports.tournament.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Neon Gaming Color Palette
val NeonPurple = Color(0xFF9B59FF)
val NeonBlue = Color(0xFF4A9EFF)
val NeonCyan = Color(0xFF00E5FF)
val NeonPink = Color(0xFFFF4081)
val NeonGreen = Color(0xFF00FF88)
val NeonOrange = Color(0xFFFF6B35)

val DarkBackground = Color(0xFF0A0A0F)
val DarkSurface = Color(0xFF12121A)
val DarkCard = Color(0xFF1A1A28)
val DarkCardElevated = Color(0xFF1E1E30)
val GlassWhite = Color(0x1AFFFFFF)
val GlassBorder = Color(0x33FFFFFF)

val GradientPurpleBlue = listOf(NeonPurple, NeonBlue)
val GradientCyanBlue = listOf(NeonCyan, NeonBlue)
val GradientPurplePink = listOf(NeonPurple, NeonPink)
val GradientGold = listOf(Color(0xFFFFD700), Color(0xFFFFA500))

// Rank tier colors
val BronzeColor = Color(0xFFCD7F32)
val SilverColor = Color(0xFFC0C0C0)
val GoldColor = Color(0xFFFFD700)
val PlatinumColor = Color(0xFF00FFFF)
val DiamondColor = Color(0xFF00BFFF)
val MasterColor = Color(0xFF9B59FF)
val LegendColor = Color(0xFFFF4500)

private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    secondary = NeonBlue,
    tertiary = NeonCyan,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkCard,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    onSurfaceVariant = Color(0xFFB0B0C8),
    outline = GlassBorder,
    error = NeonPink
)

@Composable
fun EsportsTournamentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = EsportsTypography,
        content = content
    )
}
