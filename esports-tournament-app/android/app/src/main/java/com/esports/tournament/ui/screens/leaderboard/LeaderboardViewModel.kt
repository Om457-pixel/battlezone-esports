package com.esports.tournament.ui.screens.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.tournament.data.model.LeaderboardEntry
import com.esports.tournament.data.repository.LeaderboardRepository
import com.esports.tournament.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LeaderboardUiState(
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUserId: String = "",
    val myRank: Int = 0,
    val isLoading: Boolean = false
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        loadLeaderboard(null)
        loadCurrentUser()
    }

    private fun loadCurrentUser() {
        viewModelScope.launch {
            userRepository.getCurrentUser().onSuccess { user ->
                _uiState.update { it.copy(currentUserId = user.uid) }
            }
        }
    }

    fun loadLeaderboard(game: String?) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            leaderboardRepository.getLeaderboard(game).onSuccess { entries ->
                _uiState.update { it.copy(entries = entries, isLoading = false) }
            }
        }
    }
}
