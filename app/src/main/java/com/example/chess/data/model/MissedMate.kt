package com.example.chess.data.model

data class MissedMate(
    val move: String,
    val mateInBefore: Int,
    val player: Player,
    val moveObject: Move
)
