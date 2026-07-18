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
    // Set only by GamePlay.makeMove(), never by simulateMove(). Marks that this Move
    // was produced by a real commit rather than a speculative legality-scan simulation,
    // so undo knows it's safe to restore the fields below (simulateMove() never touches
    // enPassantTarget or hasMoved, so there's nothing to restore for a simulated move).
    var wasCommitted: Boolean = false,
    var previousEnPassantTarget: Position? = null,
    var previousHasMoved: Boolean = false
)
