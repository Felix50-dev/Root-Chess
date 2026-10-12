package com.example.chess.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.data.repository.RoadmapProgressRepository
import com.example.chess.domain.model.RoadmapNodeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class RoadmapViewModel @Inject constructor(
    repository: RoadmapProgressRepository,
) : ViewModel() {

    val roadmapState: StateFlow<List<RoadmapNodeUiState>> = repository.observeRoadmapState()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val playerRating: StateFlow<Int> = repository.observeRating()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RoadmapProgressRepository.DEFAULT_RATING)
}
