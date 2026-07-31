package com.example.chess.domain.model

data class Opening(
    val eco: String,
    val name: String,
    val moves: List<String>,  // UCI, e.g. "e2e4" — the full book line for this opening
)

data class OpeningMatch(
    val opening: Opening,
    val matchedPlyCount: Int,  // how many of `opening.moves` were actually played
)
