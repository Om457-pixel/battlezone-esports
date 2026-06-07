package com.esports.tournament.data.repository

import com.esports.tournament.data.api.ApiService
import com.esports.tournament.data.model.DepositOrder
import com.esports.tournament.data.model.Transaction
import com.esports.tournament.data.model.WalletBalance
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalletRepository @Inject constructor(private val api: ApiService) {

    suspend fun getBalance(): Result<WalletBalance> = runCatching {
        val response = api.getBalance()
        if (response.isSuccessful) response.body()!!
        else throw Exception(response.message())
    }

    suspend fun createDepositOrder(amount: Int): Result<DepositOrder> = runCatching {
        val response = api.createDepositOrder(mapOf("amount" to amount))
        if (response.isSuccessful) response.body()!!
        else throw Exception(response.message())
    }

    suspend fun verifyDeposit(orderId: String, paymentId: String, signature: String, amount: Int): Result<Unit> = runCatching {
        val response = api.verifyDeposit(mapOf(
            "order_id" to orderId,
            "payment_id" to paymentId,
            "signature" to signature,
            "amount" to amount
        ))
        if (!response.isSuccessful) throw Exception(response.message())
    }

    suspend fun withdraw(amount: Double, upiId: String): Result<Unit> = runCatching {
        val response = api.withdraw(mapOf("amount" to amount, "upi_id" to upiId))
        if (!response.isSuccessful) throw Exception(response.message())
    }

    suspend fun getTransactions(): Result<List<Transaction>> = runCatching {
        val response = api.getTransactions()
        if (response.isSuccessful) response.body()!!.transactions
        else throw Exception(response.message())
    }
}
