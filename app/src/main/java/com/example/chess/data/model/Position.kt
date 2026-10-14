package com.example.chess.data.model

import androidx.compose.runtime.Immutable

@Immutable
data class Position(
    val row: Int,
    val column: Int
) {
    // Convert Position to algebraic notation (e.g., e2)
    fun toAlgebraicNotation(): String {
        val file = ('a' + column)  // 'a' corresponds to column 0
        val rank = (8 - row)       // Chess rank is from 1 to 8, row 0 is rank 8
        return "$file$rank"
    }

    companion object {
        // Flyweight pool caching all 64 valid Position instances on an 8x8 board
        private val POOL = Array(64) { index ->
            Position(index / 8, index % 8)
        }

        /**
         * Returns a flyweight cached [Position] instance for standard board coordinates (0..7, 0..7),
         * eliminating redundant heap allocations during move generation and board operations.
         */
        fun of(row: Int, column: Int): Position {
            return if (row in 0..7 && column in 0..7) {
                POOL[row * 8 + column]
            } else {
                Position(row, column)
            }
        }

        // Convert algebraic notation (e.g., e2) to Position
        fun fromAlgebraicNotation(notation: String): Position {
            val column = notation[0] - 'a'  // 'a' is column 0, 'b' is 1, and so on
            // Char arithmetic avoids String allocation (notation[1].toString()) and integer parsing
            val row = 8 - (notation[1] - '0')
            return of(row, column)
        }
    }
}
