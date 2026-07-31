package com.example.chess.domain

import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch

// Pure matcher: given the moves played so far, finds the longest book line
// (in `book`) that is a prefix of `playedMoves`. No Android/Stockfish/GamePlay
// dependency, so it's testable with a plain in-memory book fixture.
object OpeningDetector {

    fun detect(playedMoves: List<String>, book: List<Opening>): OpeningMatch? {
        val best = book
            .asSequence()
            .filter { it.moves.size <= playedMoves.size }
            .filter { it.moves == playedMoves.subList(0, it.moves.size) }
            .maxByOrNull { it.moves.size }
            ?: return null

        return OpeningMatch(opening = best, matchedPlyCount = best.moves.size)
    }
}
