package com.example.chess.domain

import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch

// Pure matcher: given the moves played so far, finds the longest book line
// (in `book`) that is a prefix of `playedMoves`. No Android/Stockfish/GamePlay
// dependency, so it's testable with a plain in-memory book fixture.
object OpeningDetector {

    // ⚡ Bolt: Optimized to avoid Kotlin Sequence pipeline overhead and subList heap allocations.
    // Iterates with an indexed for loop and checks opening length against bestLength so far,
    // pruning candidates that cannot produce a strictly longer match.
    fun detect(playedMoves: List<String>, book: List<Opening>): OpeningMatch? {
        if (playedMoves.isEmpty() || book.isEmpty()) return null

        var bestOpening: Opening? = null
        var bestLength = 0
        val playedSize = playedMoves.size

        for (i in book.indices) {
            val opening = book[i]
            val moves = opening.moves
            val size = moves.size

            // Only evaluate openings that can fit within played moves and are longer than the best found
            if (size in (bestLength + 1)..playedSize) {
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
