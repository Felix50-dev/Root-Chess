package com.example.chess.data.model

import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Immutable

@Immutable
data class Position(
    val row: Int,
    val column: Int
) {
    // Convert Position to algebraic notation (e.g., e2)
    fun toAlgebraicNotation(): String {
        if (row in 0..7 && column in 0..7) {
            return ALGEBRAIC_NOTATIONS[row][column]
        }
        val file = ('a' + column)  // 'a' corresponds to column 0
        val rank = (8 - row)       // Chess rank is from 1 to 8, row 0 is rank 8
        return "$file$rank"
    }

    companion object {
        // Cached 8x8 matrix of all valid board Position instances (flyweight pattern)
        private val POOL: Array<Array<Position>> = Array(8) { r ->
            Array(8) { c -> Position(r, c) }
        }

        // Cached 8x8 matrix of algebraic notation strings
        private val ALGEBRAIC_NOTATIONS: Array<Array<String>> = Array(8) { r ->
            Array(8) { c ->
                val file = ('a' + c)
                val rank = (8 - r)
                "$file$rank"
            }
        }

        // Factory function that returns a cached Position instance for valid 8x8 coordinates
        fun of(row: Int, column: Int): Position {
            return if (row in 0..7 && column in 0..7) {
                POOL[row][column]
            } else {
                Position(row, column)
            }
        }

        // Convert algebraic notation (e.g., e2) to Position
        fun fromAlgebraicNotation(notation: String): Position {
            require(notation.length >= 2) { "Invalid algebraic notation: $notation" }
            val column = notation[0] - 'a'  // 'a' is column 0, 'b' is 1, and so on
            // Avoid string allocation notation[1].toString().toInt() with direct char arithmetic
            val rank = notation[1] - '0'
            val row = 8 - rank  // '8' corresponds to row 0
            return of(row, column)
        }
    }
}
