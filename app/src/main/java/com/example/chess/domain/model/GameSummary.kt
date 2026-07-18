package com.example.chess.domain.model

data class GameSummary(
    val whiteAccuracy: Double,
    val blackAccuracy: Double,
    val criticalMoveIndex: Int,
    val criticalMoveDescription: String,
    val longestWinningStreak: Pair<String, Int>,  // player name, move count
    val biggestMistakeMove: String,
    val gameNarrative: String,                    // 2-3 sentence story
    val weaknessHint: String,                     // one actionable tip
)
