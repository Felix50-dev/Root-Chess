package com.example.chess.data.model

data class Feedback(
    val move: String,
    val type: String,
    val explanation: String,
    val bestMove: String? = null
)
