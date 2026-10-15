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

        // Optimization: Zero-allocation matching loop replaces sequence stream and subList() slices.
        // Direct index iteration avoids sequence overhead, subList heap allocations, and redundant comparisons.
        for (i in book.indices) {
            val opening = book[i]
            val movesSize = opening.moves.size

            // Prune openings that exceed played moves length or cannot beat the longest match found so far.
            if (movesSize > playedMoves.size || movesSize <= maxMatchedSize) {
                continue
            }

            // Verify prefix match element-by-element without allocating intermediate subList slices.
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
