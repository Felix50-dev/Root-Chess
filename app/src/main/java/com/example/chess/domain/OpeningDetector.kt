package com.example.chess.domain

import com.example.chess.domain.model.Opening
import com.example.chess.domain.model.OpeningMatch

// Pure matcher: given the moves played so far, finds the longest book line
// (in `book`) that is a prefix of `playedMoves`. No Android/Stockfish/GamePlay
// dependency, so it's testable with a plain in-memory book fixture.
object OpeningDetector {

    fun detect(playedMoves: List<String>, book: List<Opening>): OpeningMatch? {
        val playedSize = playedMoves.size
        if (playedSize == 0) return null

        var bestOpening: Opening? = null
        var maxMatchedSize = -1

        // Direct indexed iteration avoids Sequence pipeline allocations, intermediate subList() heap
        // creations, and redundant comparisons for shorter lines once a longer match is found.
        for (i in book.indices) {
            val opening = book[i]
            val moves = opening.moves
            val size = moves.size
            if (size in (maxMatchedSize + 1)..playedSize) {
                var matches = true
                for (j in 0 until size) {
                    if (moves[j] != playedMoves[j]) {
                        matches = false
                        break
                    }
                }
                if (matches) {
                    bestOpening = opening
                    maxMatchedSize = size
                }
            }
        }

        return bestOpening?.let { OpeningMatch(opening = it, matchedPlyCount = maxMatchedSize) }
    }
}
