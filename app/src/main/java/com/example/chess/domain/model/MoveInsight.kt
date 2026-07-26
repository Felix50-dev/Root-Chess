package com.example.chess.domain.model

data class MoveInsight(
    val move: String,
    val category: String,
    val evalBefore: Int,
    val evalAfter: Int,
    val evalLost: Int,              // how many cp lost
    val bestMove: String,
    val bestContinuation: List<String>,  // next 3-4 best moves
    val playerMoveRank: Int,        // was this 1st, 2nd, 3rd best?
    val insight: InsightType,
    val insightDetail: String,      // the human readable insight
    val isCriticalMoment: Boolean,  // biggest swing in the game
    val gamePhase: GamePhase,
    val solutionGap: Int? = null,  // eval gap between best and 2nd-best line at this position
)

enum class InsightType {
    BRILLIANT_TACTIC,
    MISSED_TACTIC,
    POSITIONAL_ERROR,
    MATERIAL_BLUNDER,
    MISSED_MATE,
    GOOD_DEFENSE,
    ENDGAME_ERROR,
    OPENING_DEVIATION,
}

enum class GamePhase { OPENING, MIDDLEGAME, ENDGAME }
