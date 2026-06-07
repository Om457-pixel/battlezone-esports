package com.esports.tournament.ui.screens.auth

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esports.tournament.ui.components.GradientBackground
import com.esports.tournament.ui.components.NeonButton
import com.esports.tournament.ui.theme.*

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var phoneNumber by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }

    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) onAuthSuccess()
    }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(60.dp))

            // Logo / Title
            AnimatedLogo()

            Spacer(Modifier.height(48.dp))

            AnimatedContent(
                targetState = uiState.step,
                transitionSpec = {
                    slideInHorizontally { it } + fadeIn() togetherWith
                    slideOutHorizontally { -it } + fadeOut()
                },
                label = "auth_step"
            ) { step ->
                when (step) {
                    AuthStep.PHONE -> PhoneStep(
                        phoneNumber = phoneNumber,
                        onPhoneChange = { phoneNumber = it },
                        onSendOtp = { viewModel.sendOtp(phoneNumber) },
                        isLoading = uiState.isLoading,
                        error = uiState.error
                    )
                    AuthStep.OTP -> OtpStep(
                        otp = otp,
                        onOtpChange = { otp = it },
                        onVerify = { viewModel.verifyOtp(otp) },
                        onResend = { viewModel.sendOtp(phoneNumber) },
                        isLoading = uiState.isLoading,
                        error = uiState.error
                    )
                    AuthStep.USERNAME -> UsernameStep(
                        username = username,
                        referralCode = referralCode,
                        onUsernameChange = { username = it },
                        onReferralChange = { referralCode = it },
                        onComplete = { viewModel.setUsername(username, referralCode) },
                        isLoading = uiState.isLoading,
                        error = uiState.error
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedLogo() {
    val infiniteTransition = rememberInfiniteTransition(label = "logo")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1500), RepeatMode.Reverse),
        label = "logo_glow"
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "⚡", fontSize = 64.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = "BATTLEZONE",
            color = Color.White,
            fontWeight = FontWeight.Black,
            fontSize = 32.sp,
            letterSpacing = 4.sp
        )
        Text(
            text = "ESPORTS TOURNAMENT",
            color = NeonPurple.copy(alpha = glowAlpha),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            letterSpacing = 3.sp
        )
    }
}

@Composable
private fun PhoneStep(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Enter your phone number", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        Text(text = "We'll send you a verification code", color = Color(0xFF888899), fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { if (it.length <= 10) onPhoneChange(it) },
            label = { Text("Phone Number", color = Color(0xFF888899)) },
            prefix = { Text("+91 ", color = NeonPurple, fontWeight = FontWeight.Bold) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = Color(0xFF333344),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(text = it, color = NeonPink, fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))

        NeonButton(
            text = if (isLoading) "Sending..." else "SEND OTP",
            onClick = onSendOtp,
            enabled = phoneNumber.length == 10 && !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun OtpStep(
    otp: String,
    onOtpChange: (String) -> Unit,
    onVerify: () -> Unit,
    onResend: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Enter OTP", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        Text(text = "6-digit code sent to your phone", color = Color(0xFF888899), fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))

        // OTP boxes
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(6) { index ->
                val char = otp.getOrNull(index)?.toString() ?: ""
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (char.isNotEmpty()) NeonPurple.copy(0.2f) else Color(0xFF1A1A28))
                        .border(
                            1.dp,
                            if (char.isNotEmpty()) NeonPurple else Color(0xFF333344),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = char, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }

        // Hidden text field for input
        OutlinedTextField(
            value = otp,
            onValueChange = { if (it.length <= 6) onOtpChange(it) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            modifier = Modifier.size(1.dp),
            colors = OutlinedTextFieldDefaults.colors()
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(text = it, color = NeonPink, fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))

        NeonButton(
            text = if (isLoading) "Verifying..." else "VERIFY OTP",
            onClick = onVerify,
            enabled = otp.length == 6 && !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onResend) {
            Text(text = "Resend OTP", color = NeonCyan, fontSize = 14.sp)
        }
    }
}

@Composable
private fun UsernameStep(
    username: String,
    referralCode: String,
    onUsernameChange: (String) -> Unit,
    onReferralChange: (String) -> Unit,
    onComplete: () -> Unit,
    isLoading: Boolean,
    error: String?
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = "Create your profile", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))
        Text(text = "Choose a unique gaming username", color = Color(0xFF888899), fontSize = 14.sp)
        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = username,
            onValueChange = { if (it.length <= 20) onUsernameChange(it) },
            label = { Text("Username", color = Color(0xFF888899)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                unfocusedBorderColor = Color(0xFF333344),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = { Text("${username.length}/20", color = Color(0xFF888899)) }
        )

        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = referralCode,
            onValueChange = { onReferralChange(it.uppercase()) },
            label = { Text("Referral Code (optional)", color = Color(0xFF888899)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = Color(0xFF333344),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(text = it, color = NeonPink, fontSize = 12.sp)
        }

        Spacer(Modifier.height(24.dp))

        NeonButton(
            text = if (isLoading) "Creating..." else "START PLAYING",
            onClick = onComplete,
            enabled = username.length >= 3 && !isLoading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
