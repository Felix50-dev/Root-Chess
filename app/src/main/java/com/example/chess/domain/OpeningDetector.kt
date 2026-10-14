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

        for (i in 0 until book.size) {
            val candidate = book[i]
            val moves = candidate.moves
            val candidateSize = moves.size

            // Only consider openings that are <= playedMoves and strictly longer than current best
            if (candidateSize in (maxMatchedSize + 1)..playedSize) {
                var isMatch = true
                for (j in 0 until candidateSize) {
                    if (moves[j] != playedMoves[j]) {
                        isMatch = false
                        break
                    }
                }
                if (isMatch) {
                    bestOpening = candidate
                    maxMatchedSize = candidateSize
                }
            }
        }

        val best = bestOpening ?: return null
        return OpeningMatch(opening = best, matchedPlyCount = maxMatchedSize)
    }
}
