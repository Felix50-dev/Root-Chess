package com.example.chess.domain

import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch

// Pure matcher: given the moves played so far, finds the longest book line
// (in `book`) that is a prefix of `playedMoves`. No Android/Stockfish/GamePlay
// dependency, so it's testable with a plain in-memory book fixture.
object OpeningDetector {

    // ⚡ Bolt: Optimized to avoid Sequence pipeline overhead and subList heap allocations.
    // Skips prefix checks for openings shorter than or equal to the current best match length.
    fun detect(playedMoves: List<String>, book: List<Opening>): OpeningMatch? {
        var bestOpening: Opening? = null
        var bestLength = 0

        for (i in book.indices) {
            val opening = book[i]
            val moves = opening.moves
            val size = moves.size

            // Only evaluate openings that fit within playedMoves and exceed current best match length
            if (size <= playedMoves.size && size > bestLength) {
                if (matchesPrefix(moves, playedMoves)) {
                    bestOpening = opening
                    bestLength = size
                }
            }
        }

        val best = bestOpening ?: return null
        return OpeningMatch(opening = best, matchedPlyCount = bestLength)
    }

    private fun matchesPrefix(openingMoves: List<String>, playedMoves: List<String>): Boolean {
        for (i in openingMoves.indices) {
            if (openingMoves[i] != playedMoves[i]) return false
        }
        return true
    }
}
