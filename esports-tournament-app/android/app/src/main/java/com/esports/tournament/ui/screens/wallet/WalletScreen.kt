package com.esports.tournament.ui.screens.wallet

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.esports.tournament.data.model.Transaction
import com.esports.tournament.ui.components.GlassCard
import com.esports.tournament.ui.components.GradientBackground
import com.esports.tournament.ui.components.NeonButton
import com.esports.tournament.ui.theme.*

@Composable
fun WalletScreen(
    onBack: () -> Unit,
    viewModel: WalletViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }

    GradientBackground(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Text(text = "Wallet", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }

            item {
                // Balance card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Brush.linearGradient(GradientPurpleBlue))
                        .padding(24.dp)
                ) {
                    Column {
                        Text(text = "Total Balance", color = Color.White.copy(0.8f), fontSize = 14.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "₹${String.format("%.2f", uiState.walletBalance)}",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 36.sp
                        )
                        Spacer(Modifier.height(8.dp))
                        Row {
                            Text(text = "Bonus: ₹${String.format("%.2f", uiState.bonusBalance)}", color = Color.White.copy(0.7f), fontSize = 12.sp)
                        }
                    }
                }
            }

            item {
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    NeonButton(
                        text = "➕ Add Money",
                        onClick = { showDepositDialog = true },
                        modifier = Modifier.weight(1f),
                        gradientColors = listOf(NeonGreen, NeonBlue)
                    )
                    NeonButton(
                        text = "💸 Withdraw",
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier.weight(1f),
                        gradientColors = listOf(NeonOrange, NeonPink)
                    )
                }
            }

            item {
                // Quick add amounts
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(text = "Quick Add", color = Color(0xFF888899), fontSize = 13.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(50, 100, 200, 500).forEach { amount ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(1.dp, NeonGreen.copy(0.4f), RoundedCornerShape(8.dp))
                                    .background(NeonGreen.copy(0.1f))
                                    .clickable { viewModel.initiateDeposit(amount) }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(text = "₹$amount", color = NeonGreen, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Transaction History",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            items(uiState.transactions) { tx ->
                TransactionRow(transaction = tx)
            }
        }
    }

    if (showDepositDialog) {
        DepositDialog(
            onDismiss = { showDepositDialog = false },
            onDeposit = { amount ->
                viewModel.initiateDeposit(amount)
                showDepositDialog = false
            }
        )
    }

    if (showWithdrawDialog) {
        WithdrawDialog(
            balance = uiState.walletBalance,
            onDismiss = { showWithdrawDialog = false },
            onWithdraw = { amount, upiId ->
                viewModel.withdraw(amount, upiId)
                showWithdrawDialog = false
            }
        )
    }
}

@Composable
private fun TransactionRow(transaction: Transaction) {
    val isCredit = transaction.amount > 0
    val color = if (isCredit) NeonGreen else NeonPink
    val icon = when (transaction.type) {
        "deposit" -> "💳"
        "withdrawal" -> "🏦"
        "entry_fee" -> "🎮"
        "prize_won" -> "🏆"
        "referral_bonus" -> "🎁"
        "daily_reward" -> "🔥"
        "spin_reward" -> "🎰"
        else -> "💰"
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = transaction.description, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Text(text = transaction.createdAt.take(10), color = Color(0xFF888899), fontSize = 11.sp)
            }
            Text(
                text = "${if (isCredit) "+" else ""}₹${Math.abs(transaction.amount).toInt()}",
                color = color,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun DepositDialog(onDismiss: () -> Unit, onDeposit: (Int) -> Unit) {
    var amount by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = { Text("Add Money", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() } },
                label = { Text("Amount (₹)", color = Color(0xFF888899)) },
                prefix = { Text("₹", color = NeonGreen) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonGreen,
                    unfocusedBorderColor = Color(0xFF333344),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            NeonButton(
                text = "PROCEED",
                onClick = { amount.toIntOrNull()?.let { onDeposit(it) } },
                gradientColors = listOf(NeonGreen, NeonBlue),
                enabled = amount.toIntOrNull()?.let { it >= 10 } ?: false
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF888899)) } }
    )
}

@Composable
private fun WithdrawDialog(balance: Double, onDismiss: () -> Unit, onWithdraw: (Double, String) -> Unit) {
    var amount by remember { mutableStateOf("") }
    var upiId by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkCard,
        title = { Text("Withdraw Money", color = Color.White, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = "Available: ₹${balance.toInt()}", color = NeonGreen, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() } },
                    label = { Text("Amount (min ₹100)", color = Color(0xFF888899)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonOrange, unfocusedBorderColor = Color(0xFF333344), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = upiId,
                    onValueChange = { upiId = it },
                    label = { Text("UPI ID", color = Color(0xFF888899)) },
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonOrange, unfocusedBorderColor = Color(0xFF333344), focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            NeonButton(
                text = "WITHDRAW",
                onClick = { amount.toDoubleOrNull()?.let { onWithdraw(it, upiId) } },
                gradientColors = listOf(NeonOrange, NeonPink),
                enabled = (amount.toDoubleOrNull() ?: 0.0) >= 100 && upiId.isNotBlank()
            )
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = Color(0xFF888899)) } }
    )
}
