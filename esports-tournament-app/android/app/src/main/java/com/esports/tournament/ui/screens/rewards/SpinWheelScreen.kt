package com.esports.tournament.ui.screens.rewards

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esports.tournament.ui.components.GradientBackground
import com.esports.tournament.ui.components.NeonButton
import com.esports.tournament.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

val SPIN_SEGMENTS = listOf(
    Pair("₹5", NeonBlue),
    Pair("₹10", NeonPurple),
    Pair("₹25", NeonCyan),
    Pair("₹50", NeonOrange),
    Pair("₹100", NeonGreen),
    Pair("₹250", NeonPink),
    Pair("₹500", GoldColor),
    Pair("₹10", NeonBlue)
)

@Composable
fun SpinWheelScreen(
    onBack: () -> Unit,
    viewModel: SpinWheelViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var isSpinning by remember { mutableStateOf(false) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var wonPrize by remember { mutableStateOf<String?>(null) }

    val animatedRotation by animateFloatAsState(
        targetValue = rotation,
        animationSpec = tween(
            durationMillis = 4000,
            easing = FastOutSlowInEasing
        ),
        finishedListener = {
            isSpinning = false
            // Calculate winning segment
            val normalizedAngle = rotation % 360
            val segmentAngle = 360f / SPIN_SEGMENTS.size
            val winIndex = ((360 - normalizedAngle) / segmentAngle).toInt() % SPIN_SEGMENTS.size
            wonPrize = SPIN_SEGMENTS[winIndex].first
        },
        label = "spin_rotation"
    )

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(text = "🎰 Spin & Win", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            }

            Spacer(Modifier.height(24.dp))

            Text(text = "Spin the wheel to win prizes!", color = Color(0xFF888899), fontSize = 14.sp)
            Spacer(Modifier.height(8.dp))

            if (!uiState.spinAvailable) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NeonOrange.copy(0.2f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(text = "Next spin available tomorrow", color = NeonOrange, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Pointer
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color.White)
            )

            // Wheel
            Box(
                modifier = Modifier
                    .size(300.dp)
                    .rotate(animatedRotation)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawSpinWheel(SPIN_SEGMENTS)
                }
            }

            Spacer(Modifier.height(32.dp))

            // Won prize display
            wonPrize?.let { prize ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.horizontalGradient(GradientGold))
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = "🎉 You won $prize!",
                        color = Color.Black,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            NeonButton(
                text = if (isSpinning) "SPINNING..." else "SPIN NOW",
                onClick = {
                    if (!isSpinning && uiState.spinAvailable) {
                        isSpinning = true
                        wonPrize = null
                        rotation += 1440f + (0..360).random()
                        viewModel.spin()
                    }
                },
                enabled = !isSpinning && uiState.spinAvailable,
                gradientColors = GradientPurpleBlue,
                modifier = Modifier.padding(horizontal = 32.dp).fillMaxWidth()
            )
        }
    }
}

private fun DrawScope.drawSpinWheel(segments: List<Pair<String, Color>>) {
    val segmentAngle = 360f / segments.size
    val radius = size.minDimension / 2
    val center = Offset(size.width / 2, size.height / 2)

    segments.forEachIndexed { index, (label, color) ->
        val startAngle = index * segmentAngle - 90f
        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = segmentAngle,
            useCenter = true,
            topLeft = Offset(center.x - radius, center.y - radius),
            size = Size(radius * 2, radius * 2)
        )
        // Divider lines
        val lineAngle = Math.toRadians((startAngle + segmentAngle / 2).toDouble())
        drawLine(
            color = Color.Black.copy(0.3f),
            start = center,
            end = Offset(
                center.x + (radius * cos(Math.toRadians(startAngle.toDouble()))).toFloat(),
                center.y + (radius * sin(Math.toRadians(startAngle.toDouble()))).toFloat()
            ),
            strokeWidth = 2f
        )
        // Label
        val textAngle = Math.toRadians((startAngle + segmentAngle / 2).toDouble())
        val textRadius = radius * 0.65f
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                this.color = android.graphics.Color.WHITE
                textSize = 32f
                textAlign = android.graphics.Paint.Align.CENTER
                isFakeBoldText = true
            }
            drawText(
                label,
                center.x + (textRadius * cos(textAngle)).toFloat(),
                center.y + (textRadius * sin(textAngle)).toFloat() + 10f,
                paint
            )
        }
    }

    // Center circle
    drawCircle(color = Color(0xFF1A1A28), radius = radius * 0.15f, center = center)
    drawCircle(color = NeonPurple, radius = radius * 0.12f, center = center)
}
