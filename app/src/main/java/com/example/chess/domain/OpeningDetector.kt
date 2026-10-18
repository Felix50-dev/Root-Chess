package com.example.chess.domain

import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch

// Pure matcher: given the moves played so far, finds the longest book line
// (in `book`) that is a prefix of `playedMoves`. No Android/Stockfish/GamePlay
// dependency, so it's testable with a plain in-memory book fixture.
object OpeningDetector {

    fun detect(playedMoves: List<String>, book: List<Opening>): OpeningMatch? {
        val playedSize = playedMoves.size
        var best: Opening? = null
        var maxMovesSize = -1

        // Direct index loop avoids sequence wrapper, predicate lambda allocations, and subList slices.
        for (i in 0 until book.size) {
            val opening = book[i]
            val moves = opening.moves
            val movesSize = moves.size

            // Skip openings longer than played moves, or not longer than our current best match.
            if (movesSize > playedSize || movesSize <= maxMovesSize) {
                continue
            }

            // Element-by-element move comparison without subList allocations.
            var isPrefix = true
            for (j in 0 until movesSize) {
                if (moves[j] != playedMoves[j]) {
                    isPrefix = false
                    break
                }
            }

            if (isPrefix) {
                best = opening
                maxMovesSize = movesSize
            }
        }

        return if (best != null) OpeningMatch(opening = best, matchedPlyCount = maxMovesSize) else null
    }
}
