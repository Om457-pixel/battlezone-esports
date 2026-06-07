package com.esports.tournament.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.tournament.data.model.Achievement
import com.esports.tournament.data.model.User
import com.esports.tournament.data.repository.RewardsRepository
import com.esports.tournament.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val achievements: List<Achievement> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val rewardsRepository: RewardsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getCurrentUser().onSuccess { user ->
                _uiState.update { it.copy(user = user, isLoading = false) }
            }
            rewardsRepository.getAchievements().onSuccess { achievements ->
                _uiState.update { it.copy(achievements = achievements) }
            }
        }
    }
}
