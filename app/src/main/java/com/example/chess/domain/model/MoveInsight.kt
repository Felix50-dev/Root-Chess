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
    val hungPieceSquare: String? = null,  // square of the piece this move leaves undefended, if any
    // Mate the move newly ALLOWED against the mover (opponent mates in N after it, and no
    // forced mate against the mover existed before) — null when mate was unavoidable anyway,
    // so "the alternative would have prevented it" is only ever claimed when true.
    val allowedMateIn: Int? = null,
    // The opponent's best reply to the move actually played (engine line at the next ply) —
    // used by the threat probe to tell whether a probed threat went unaddressed.
    val opponentReplyUci: String? = null,
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
