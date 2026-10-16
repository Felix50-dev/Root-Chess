package com.example.chess.domain

import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch

// Pure matcher: given the moves played so far, finds the longest book line
// (in `book`) that is a prefix of `playedMoves`. No Android/Stockfish/GamePlay
// dependency, so it's testable with a plain in-memory book fixture.
object OpeningDetector {

    fun detect(playedMoves: List<String>, book: List<Opening>): OpeningMatch? {
        if (playedMoves.isEmpty() || book.isEmpty()) return null

        var bestOpening: Opening? = null
        var maxMatchedSize = 0

        val playedSize = playedMoves.size

        // Iterating over the book directly with index loop avoids Kotlin sequence allocations,
        // intermediate list filtering, and subList heap allocations per candidate opening.
        for (i in book.indices) {
            val opening = book[i]
            val moves = opening.moves
            val movesSize = moves.size

            // Skip openings that are longer than played moves or shorter/equal to current best match
            if (movesSize > playedSize || movesSize <= maxMatchedSize) continue

            var matches = true
            for (j in 0 until movesSize) {
                if (moves[j] != playedMoves[j]) {
                    matches = false
                    break
                }
            }

            if (matches) {
                bestOpening = opening
                maxMatchedSize = movesSize
            }
        }

        return bestOpening?.let { OpeningMatch(opening = it, matchedPlyCount = maxMatchedSize) }
    }
}
