package com.example.chess.domain.model

data class MoveExplanationRequest(
    val type: String,               // "Best", "Blunder", "Mistake", "Brilliant" etc.
    val movePlayed: String,         // UCI coordinate notation e.g. "g1f3" ("e7e8q" for promotion)
    val bestMove: String?,          // Stockfish best move, UCI coordinate notation
    val chessPiece: String?,        // piece involved in capture opportunity
    val evalDelta: Float,           // pawns, negative = the move lost ground
    val tactic: List<DetectedTactic>?, // from TacticsRecognizer; descriptions are rendered as Facts
    val mateIn: Int?,               // if MissedMate, how many moves
    val matingPiece: String?,       // piece that delivers mate
    val allowedMateIn: Int? = null, // mate this move newly allowed AGAINST the mover (see MoveInsight)
    val facts: List<String>,        // deterministic, verified facts about the position/move — see ExplanationFacts
    val playerElo: Int,             // for explanation depth calibration
    val moveNumber: Int,            // full-move number in the game (White+Black share one)
    val playerColor: String         // "White" or "Black"
)
