package com.example.chess.domain

import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch

// Pure matcher: given the moves played so far, finds the longest book line
// (in `book`) that is a prefix of `playedMoves`. No Android/Stockfish/GamePlay
// dependency, so it's testable with a plain in-memory book fixture.
object OpeningDetector {

    fun detect(playedMoves: List<String>, book: List<Opening>): OpeningMatch? {
        var bestOpening: Opening? = null
        var maxMovesSize = -1

        // Direct index loop over opening book to avoid Sequence wrapper and subList allocations
        for (i in 0 until book.size) {
            val opening = book[i]
            val moves = opening.moves
            val size = moves.size

            if (size <= playedMoves.size && size > maxMovesSize) {
                var matches = true
                for (j in 0 until size) {
                    if (moves[j] != playedMoves[j]) {
                        matches = false
                        break
                    }
                }
                if (matches) {
                    bestOpening = opening
                    maxMovesSize = size
                }
            }
        }

        val best = bestOpening ?: return null
        return OpeningMatch(opening = best, matchedPlyCount = best.moves.size)
    }
}
