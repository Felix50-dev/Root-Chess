package com.example.chess.data.model

data class AnalysisResult(
    val evaluations: List<Int>,
    val whiteAccuracy: Double,
    val blackAccuracy: Double,
    val whiteCategoryCounts: Map<String, Int>,
    val blackCategoryCounts: Map<String, Int>
)
