package com.esports.tournament.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.tournament.data.api.ApiService
import com.esports.tournament.data.local.TokenStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject

enum class AuthStep { PHONE, OTP, USERNAME }

data class AuthUiState(
    val step: AuthStep = AuthStep.PHONE,
    val isLoading: Boolean = false,
    val isAuthenticated: Boolean = false,
    val isNewUser: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val api: ApiService,
    private val tokenStore: TokenStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val auth = FirebaseAuth.getInstance()
    private var verificationId: String? = null
    private var idToken: String? = null

    init {
        // Check if already logged in
        viewModelScope.launch {
            val token = tokenStore.getAccessToken()
            if (token != null) {
                _uiState.update { it.copy(isAuthenticated = true) }
            }
        }
    }

    fun sendOtp(phoneNumber: String) {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber("+91$phoneNumber")
            .setTimeout(60L, TimeUnit.SECONDS)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithCredential(credential)
                }
                override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                override fun onCodeSent(vId: String, token: PhoneAuthProvider.ForceResendingToken) {
                    verificationId = vId
                    _uiState.update { it.copy(isLoading = false, step = AuthStep.OTP) }
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyOtp(otp: String) {
        val vId = verificationId ?: return
        _uiState.update { it.copy(isLoading = true, error = null) }
        val credential = PhoneAuthProvider.getCredential(vId, otp)
        signInWithCredential(credential)
    }

    private fun signInWithCredential(credential: PhoneAuthCredential) {
        viewModelScope.launch {
            try {
                val result = auth.signInWithCredential(credential).await()
                idToken = result.user?.getIdToken(false)?.await()?.token
                val response = api.verifyToken(mapOf("id_token" to idToken!!))
                if (response.isSuccessful) {
                    val body = response.body()!!
                    if (body.isNewUser) {
                        _uiState.update { it.copy(isLoading = false, step = AuthStep.USERNAME, isNewUser = true) }
                    } else {
                        tokenStore.saveTokens(body.accessToken, body.refreshToken)
                        _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Authentication failed") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Invalid OTP") }
            }
        }
    }

    fun setUsername(username: String, referralCode: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val body = mutableMapOf("id_token" to (idToken ?: ""), "username" to username)
                if (referralCode.isNotBlank()) body["referral_code"] = referralCode
                val response = api.verifyToken(body)
                if (response.isSuccessful) {
                    val authResp = response.body()!!
                    tokenStore.saveTokens(authResp.accessToken, authResp.refreshToken)
                    _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Username taken or invalid") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}
