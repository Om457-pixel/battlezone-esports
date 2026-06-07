package com.esports.tournament.ui.screens.rewards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.esports.tournament.data.repository.RewardsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SpinUiState(val spinAvailable: Boolean = false, val lastPrize: String? = null)

@HiltViewModel
class SpinWheelViewModel @Inject constructor(
    private val rewardsRepository: RewardsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SpinUiState())
    val uiState: StateFlow<SpinUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            rewardsRepository.getDailySchedule().onSuccess { schedule ->
                _uiState.update { it.copy(spinAvailable = true) } // simplified
            }
        }
    }

    fun spin() {
        viewModelScope.launch {
            rewardsRepository.spin().onSuccess { result ->
                _uiState.update { it.copy(spinAvailable = false, lastPrize = result.prize.label) }
            }
        }
    }
}
