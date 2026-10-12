package com.example.chess.data.repository

import com.example.chess.data.local.BotProgressEntity
import com.example.chess.data.model.BotProfile
import com.example.chess.domain.model.NodeState
import com.example.chess.domain.model.RoadmapNodeUiState

// Pure, dependency-free so it's directly unit-testable without a Dao/DataStore fake.
// Catalog order IS unlock order: the first bot in `catalog` with no recorded progress is
// "Current"; everything strictly after it stays "Locked" until its own turn comes. A bot
// that already has a progress row always renders Completed, regardless of position.
fun deriveNodeStates(
    catalog: List<BotProfile>,
    progress: Map<String, BotProgressEntity>,
): List<RoadmapNodeUiState> {
    var unlockedNext = true
    return catalog.map { bot ->
        val recorded = progress[bot.id]
        val state = when {
            recorded != null -> NodeState.Completed(recorded.starRating)
            unlockedNext -> NodeState.Current
            else -> NodeState.Locked
        }
        if (recorded == null) unlockedNext = false
        RoadmapNodeUiState(bot, state)
    }
}
