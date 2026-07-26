package com.example.chess.domain.model

data class PositionEval(
    val score: Int,
    val bestMove: String,
    val mateIn: Int,
    val principalVariation: List<String>,  // full best line
    val depth: Int,
    val secondBestScore: Int? = null,  // MultiPV line 2 eval, same perspective as score
    val secondBestMove: String? = null,  // MultiPV line 2 best move
)
