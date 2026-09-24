package com.example.chess.data.repository

import com.example.chess.domain.model.PositionEval

// Deterministic, ground-truth fact sentences about opponent threats discovered by the
// null-move probe (see GamePlay.threatProbeFacts), handed to the LLM alongside
// ExplanationFacts. Pure string/score logic only — no Board or engine dependency — so
// every template and verdict predicate is JVM-testable without a device.
object ThreatFacts {

    // Minimum eval gain (cp, opponent's perspective) the opponent's "free move" must
    // achieve over their standing eval before it counts as a real threat — below this
    // the position is quiet and no threat facts are emitted.
    const val THREAT_GAIN_THRESHOLD_CP = 150

    // Standing evals are clamped to this magnitude before threat-gain arithmetic so
    // mate-adjacent or degenerate scores can't swamp the threshold comparison.
    const val EVAL_CLAMP_CP = 800

    fun describeThreat(
        opponentColorWord: String,
        pieceType: String,
        from: String,
        to: String,
        capturedColorWord: String?,
        capturedType: String?,
        capturedSquare: String?,
        mateIn: Int?,
    ): String = when {
        mateIn != null ->
            "Opponent threat: $opponentColorWord is threatening a forced checkmate in $mateIn, " +
                "starting with the $opponentColorWord $pieceType on $from to $to."
        capturedType != null ->
            "Opponent threat: if it were $opponentColorWord's turn, the $opponentColorWord " +
                "$pieceType on $from could capture the $capturedColorWord $capturedType on $capturedSquare."
        else ->
            "Opponent threat: if it were $opponentColorWord's turn, the $opponentColorWord " +
                "$pieceType on $from could play to $to with a strong threat."
    }

    fun inCheckFact(moverColorWord: String): String =
        "$moverColorWord was in check before this move — escaping the check was itself " +
            "the immediate problem to solve."

    fun playedMoveFailsFact(): String =
        "The move played did NOT deal with this threat — it is now the opponent's best reply."

    fun alternativeParriesFact(): String =
        "The stronger alternative would have dealt with this threat — after it, " +
            "this threat no longer works."

    // From/to squares identify a threat; a promotion suffix on either side must not
    // break the match ("e7e8q" executes the threat "e7e8").
    fun matchesThreat(threatUci: String, replyUci: String?): Boolean =
        replyUci != null && threatUci.take(4) == replyUci.take(4)

    // After the stronger alternative, a threat counts as parried when it is no longer
    // the opponent's best resource, or it still is but no longer gains anything
    // meaningful (replyEval is from the opponent's perspective in the post-alternative
    // position; clampedEvalBeforeCp is the mover's clamped standing eval at P, so
    // score + eval is the same gain convention used to select the threat).
    fun isParriedByAlternative(
        threatUci: String,
        replyEval: PositionEval,
        clampedEvalBeforeCp: Int,
    ): Boolean = when {
        !matchesThreat(threatUci, replyEval.bestMove) -> true
        replyEval.mateIn > 0 -> false
        else -> replyEval.score + clampedEvalBeforeCp < THREAT_GAIN_THRESHOLD_CP
    }
}
