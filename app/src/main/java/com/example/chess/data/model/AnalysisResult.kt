package com.example.chess.data.model

data class AnalysisResult(
    val evaluations: List<Int>,
    val whiteAccuracy: Double,
    val blackAccuracy: Double,
    val whiteCategoryCounts: Map<String, Int>,
    val blackCategoryCounts: Map<String, Int>,
    val moveCategories: List<String>,
    val bestMoves: List<String>,
    val captureOpportunity: MutableList<CaptureOpportunity?>,
    val missedMates: MutableList<MissedMate?>,
    val tactics: MutableList<List<String>>
)
