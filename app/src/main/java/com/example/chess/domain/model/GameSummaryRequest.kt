package com.example.chess.domain.model

import com.example.chess.data.model.AnalysisResult

// Everything the AI game-summary prompt needs, pulled straight from an already-computed
// AnalysisResult — mirrors MoveExplanationRequest.fromAnalysisResult's convention.
data class GameSummaryRequest(
    val pgn: String,
    val totalMoves: Int,
    val whiteAccuracy: Double,
    val blackAccuracy: Double,
    val whiteCategoryCounts: Map<String, Int>,
    val blackCategoryCounts: Map<String, Int>,
    val distinctTactics: List<String>,
    val missedMateCount: Int,
    val criticalMoveIndex: Int,
    val criticalMoveNumber: Int,
    val criticalMovePlayerColor: String,
    val criticalMoveSan: String,
    val criticalMoveDetail: String,
    val criticalMoveBestContinuation: List<String>,
    val criticalMoveEvalLost: Int,
    val criticalMoveGamePhase: String,
    val longestStreakPlayer: String,
    val longestStreakLength: Int,
    val openingName: String?,
    val openingEco: String?,
    val playerElo: Int,
) {
    companion object {
        fun fromAnalysisResult(
            result: AnalysisResult,
            pgn: String,
            playerElo: Int = 1200,
        ): GameSummaryRequest? {
            val summary = result.gameSummary ?: return null
            val criticalIndex = result.criticalMoveIndex
            val criticalInsight = result.insights.getOrNull(criticalIndex)

            return GameSummaryRequest(
                pgn = pgn,
                totalMoves = result.insights.size,
                whiteAccuracy = summary.whiteAccuracy,
                blackAccuracy = summary.blackAccuracy,
                whiteCategoryCounts = result.whiteCategoryCounts,
                blackCategoryCounts = result.blackCategoryCounts,
                distinctTactics = result.tactics.flatten().distinct(),
                missedMateCount = result.missedMates.count { it != null },
                criticalMoveIndex = criticalIndex,
                criticalMoveNumber = criticalIndex + 1,
                criticalMovePlayerColor = if (criticalIndex % 2 == 0) "White" else "Black",
                criticalMoveSan = criticalInsight?.move ?: summary.biggestMistakeMove,
                criticalMoveDetail = summary.criticalMoveDescription,
                criticalMoveBestContinuation = criticalInsight?.bestContinuation ?: emptyList(),
                criticalMoveEvalLost = criticalInsight?.evalLost ?: 0,
                criticalMoveGamePhase = criticalInsight?.gamePhase?.name ?: "MIDDLEGAME",
                longestStreakPlayer = summary.longestWinningStreak.first,
                longestStreakLength = summary.longestWinningStreak.second,
                openingName = summary.openingName,
                openingEco = summary.openingEco,
                playerElo = playerElo,
            )
        }
    }
}
