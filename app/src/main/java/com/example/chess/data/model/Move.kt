package com.example.chess.data.model

data class Move(
    val from: Spot,
    val to: Spot,
    val player: Player,
    val pieceMoved: ChessPiece?,
    var pieceKilled: ChessPiece? = null,
    var isCastlingMove: Boolean = false,
    var promotionPiece: ChessPiece? = null,
    var isEnPassant: Boolean = false,
    // Set only by GamePlay.makeMove(), never by simulateMove(). Lets undo restore
    // Board.enPassantTarget after a real committed move, while leaving it untouched
    // when undoing a speculative simulateMove() used for legality scanning.
    var previousEnPassantTarget: Position? = null,
    var enPassantTargetCaptured: Boolean = false
)
