package com.example.chess.data.model

data class CaptureOpportunity(
    val target: ChessPiece,
    val attacker: ChessPiece,
    val isProfitable: Boolean
)