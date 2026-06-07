package com.esports.tournament.ui.screens.match

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.tournament.data.model.JoinedPlayer
import com.esports.tournament.data.model.Match
import com.esports.tournament.data.repository.MatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchDetailUiState(
    val match: Match? = null,
    val players: List<JoinedPlayer> = emptyList(),
    val hasJoined: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
    val joinSuccess: Boolean = false
)

@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    private val matchRepository: MatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    fun loadMatch(matchId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            matchRepository.getMatch(matchId).onSuccess { match ->
                _uiState.update { it.copy(match = match, isLoading = false) }
            }
            matchRepository.getPlayers(matchId).onSuccess { players ->
                _uiState.update { it.copy(players = players) }
            }
            matchRepository.hasJoined(matchId).onSuccess { joined ->
                _uiState.update { it.copy(hasJoined = joined) }
            }
        }
    }

    fun joinMatch(matchId: String, inGameName: String) {
        viewModelScope.launch {
            matchRepository.joinMatch(matchId, inGameName).onSuccess {
                _uiState.update { it.copy(hasJoined = true, joinSuccess = true) }
                loadMatch(matchId)
            }.onFailure { e ->
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
