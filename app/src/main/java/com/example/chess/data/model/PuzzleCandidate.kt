package com.example.chess.data.model

// A Blunder/Mistake position extracted from a just-analyzed game, before it's persisted
// as a PuzzleEntity (timestamps are a persistence-layer concern, added at that point).
data class PuzzleCandidate(
    val fen: String,
    val correctMoveUci: String,
    val playedMoveUci: String,
    val category: String,
)
