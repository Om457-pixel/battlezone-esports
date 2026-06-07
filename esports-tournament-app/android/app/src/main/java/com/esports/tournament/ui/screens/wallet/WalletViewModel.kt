package com.esports.tournament.ui.screens.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.tournament.data.model.Transaction
import com.esports.tournament.data.repository.WalletRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WalletUiState(
    val walletBalance: Double = 0.0,
    val bonusBalance: Double = 0.0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class WalletViewModel @Inject constructor(
    private val walletRepository: WalletRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(WalletUiState())
    val uiState: StateFlow<WalletUiState> = _uiState.asStateFlow()

    init {
        loadWallet()
    }

    private fun loadWallet() {
        viewModelScope.launch {
            walletRepository.getBalance().onSuccess { balance ->
                _uiState.update { it.copy(walletBalance = balance.walletBalance, bonusBalance = balance.bonusBalance) }
            }
            walletRepository.getTransactions().onSuccess { txs ->
                _uiState.update { it.copy(transactions = txs) }
            }
        }
    }

    fun initiateDeposit(amount: Int) {
        viewModelScope.launch {
            walletRepository.createDepositOrder(amount).onSuccess { order ->
                // Trigger Razorpay SDK with order.orderId
                // After payment success, call verifyDeposit
            }
        }
    }

    fun withdraw(amount: Double, upiId: String) {
        viewModelScope.launch {
            walletRepository.withdraw(amount, upiId).onSuccess {
                loadWallet()
            }
        }
    }
}
