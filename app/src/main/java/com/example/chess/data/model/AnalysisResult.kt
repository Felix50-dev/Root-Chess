package com.example.chess.data.model

import com.example.chess.domain.model.GameSummary
import com.example.chess.domain.model.MoveInsight
data class AnalysisResult(
    val evaluations: List<Int>,
    val whiteAccuracy: Double,
    val blackAccuracy: Double,
    val whiteCategoryCounts: Map<String, Int>,
    val blackCategoryCounts: Map<String, Int>,
    val moveCategories: List<String>,
    val bestMoves: List<String>,
    val captureOpportunity: List<CaptureOpportunity?>,
    val missedMates: List<MissedMate?>,
    val tactics: List<List<String>>,
    val losses: List<Int>,
    val insights: List<MoveInsight>,
    val gameSummary: GameSummary?,
    val criticalMoveIndex: Int,
)
