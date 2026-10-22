package com.example.chess.domain

import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch

// Pure matcher: given the moves played so far, finds the longest book line
// (in `book`) that is a prefix of `playedMoves`. No Android/Stockfish/GamePlay
// dependency, so it's testable with a plain in-memory book fixture.
object OpeningDetector {

    fun detect(playedMoves: List<String>, book: List<Opening>): OpeningMatch? {
        var bestOpening: Opening? = null
        var maxMatchedSize = -1

        // Optimization: Iterating directly over `book` without sequence wrappers
        // or creating `subList()` view objects/copies for each opening candidate.
        // Element-by-element comparison stops early on mismatches to avoid allocation
        // and reduce CPU cycles when matching played moves against opening book entries.
        for (i in 0 until book.size) {
            val opening = book[i]
            val movesSize = opening.moves.size

            if (movesSize > playedMoves.size || movesSize <= maxMatchedSize) {
                continue
            }

            var matches = true
            for (j in 0 until movesSize) {
                if (opening.moves[j] != playedMoves[j]) {
                    matches = false
                    break
                }
            }

            if (matches) {
                bestOpening = opening
                maxMatchedSize = movesSize
            }
        }

        val best = bestOpening ?: return null
        return OpeningMatch(opening = best, matchedPlyCount = maxMatchedSize)
    }
}
