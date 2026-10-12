package com.example.chess.domain.model

import com.example.chess.data.model.BotProfile

sealed interface NodeState {
    data object Locked : NodeState
    data object Current : NodeState
    data class Completed(val stars: Int) : NodeState
}

data class RoadmapNodeUiState(val bot: BotProfile, val state: NodeState)
