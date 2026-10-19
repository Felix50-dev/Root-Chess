package com.example.chess.domain

import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch

// Pure matcher: given the moves played so far, finds the longest book line
// (in `book`) that is a prefix of `playedMoves`. No Android/Stockfish/GamePlay
// dependency, so it's testable with a plain in-memory book fixture.
object OpeningDetector {

    fun detect(playedMoves: List<String>, book: List<Opening>): OpeningMatch? {
        var bestOpening: Opening? = null
        var maxMatchedLength = -1
        val playedSize = playedMoves.size

        // Single-pass scan over opening book.
        // Replaces Sequence streaming, subList allocations, and list equality checks with
        // indexed loops, early pruning, and direct string comparison for maximum performance.
        for (i in 0 until book.size) {
            val opening = book[i]
            val moves = opening.moves
            val moveCount = moves.size

            // Skip lines longer than played moves or shorter/equal to best match found so far
            if (moveCount > playedSize || moveCount <= maxMatchedLength) {
                continue
            }

            var matches = true
            for (j in 0 until moveCount) {
                if (moves[j] != playedMoves[j]) {
                    matches = false
                    break
                }
            }

            if (matches) {
                bestOpening = opening
                maxMatchedLength = moveCount
            }
        }

        return bestOpening?.let { OpeningMatch(opening = it, matchedPlyCount = maxMatchedLength) }
    }
}
