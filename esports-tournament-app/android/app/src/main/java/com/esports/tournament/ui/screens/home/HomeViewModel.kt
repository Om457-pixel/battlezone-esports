package com.esports.tournament.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.tournament.data.model.Match
import com.esports.tournament.data.repository.MatchRepository
import com.esports.tournament.data.repository.RewardsRepository
import com.esports.tournament.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val username: String = "",
    val avatarUrl: String? = null,
    val walletBalance: Double = 0.0,
    val streak: Int = 0,
    val canClaimDaily: Boolean = false,
    val unreadNotifications: Int = 0,
    val featuredMatches: List<Match> = emptyList(),
    val matches: List<Match> = emptyList(),
    val selectedGame: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val userRepository: UserRepository,
    private val rewardsRepository: RewardsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadUserData()
        loadFeaturedMatches()
        loadMatches("upcoming")
    }

    private fun loadUserData() {
        viewModelScope.launch {
            userRepository.getCurrentUser().onSuccess { user ->
                _uiState.update {
                    it.copy(
                        username = user.username,
                        avatarUrl = user.avatar,
                        walletBalance = user.walletBalance,
                        streak = user.streak
                    )
                }
            }
            rewardsRepository.getDailySchedule().onSuccess { schedule ->
                _uiState.update { it.copy(canClaimDaily = !schedule.claimedToday) }
            }
        }
    }

    private fun loadFeaturedMatches() {
        viewModelScope.launch {
            matchRepository.getFeaturedMatches().onSuccess { matches ->
                _uiState.update { it.copy(featuredMatches = matches) }
            }
        }
    }

    fun loadMatches(status: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            matchRepository.getMatches(status, _uiState.value.selectedGame).onSuccess { matches ->
                _uiState.update { it.copy(matches = matches, isLoading = false) }
            }.onFailure {
                _uiState.update { s -> s.copy(isLoading = false, error = it.message) }
            }
        }
    }

    fun filterByGame(game: String?) {
        _uiState.update { it.copy(selectedGame = game) }
        loadMatches("upcoming")
    }

    fun claimDailyReward() {
        viewModelScope.launch {
            rewardsRepository.claimDailyReward().onSuccess { result ->
                _uiState.update {
                    it.copy(
                        canClaimDaily = false,
                        walletBalance = it.walletBalance + result.amount
                    )
                }
            }
        }
    }
}
