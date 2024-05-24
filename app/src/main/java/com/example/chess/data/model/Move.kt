package com.example.chess.data.model

data class Move(
    val from: Spot,
    val to: Spot,
    val player: Player,
    val pieceMoved: ChessPiece?,
    var pieceKilled: ChessPiece? = null,
    var isCastlingMove: Boolean = false,
    var isEnPassant: Boolean = false
)
