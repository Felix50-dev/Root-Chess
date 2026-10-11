package com.example.chess.data.model

data class Position(
    val row: Int,
    val column: Int
) {
    // Pre-calculate algebraic notation so toAlgebraicNotation() is O(1) with zero allocations
    val algebraicNotation: String = "${('a' + column)}${8 - row}"

    // Convert Position to algebraic notation (e.g., e2)
    fun toAlgebraicNotation(): String = algebraicNotation

    companion object {
        // Cached 8x8 grid of Position instances for zero heap allocations on board positions
        private val CACHE = Array(8) { row ->
            Array(8) { col ->
                Position(row, col)
            }
        }

        /**
         * Returns a cached [Position] instance for valid board coordinates (0..7, 0..7),
         * or creates a new [Position] if coordinates are outside standard board bounds.
         */
        fun of(row: Int, column: Int): Position {
            return if (row in 0..7 && column in 0..7) {
                CACHE[row][column]
            } else {
                Position(row, column)
            }
        }

        // Convert algebraic notation (e.g., e2) to Position using fast char arithmetic and position cache
        fun fromAlgebraicNotation(notation: String): Position {
            val column = notation[0] - 'a'  // 'a' is column 0, 'b' is 1, and so on
            val row = 8 - (notation[1] - '0')  // '8' corresponds to row 0, using char math instead of toString().toInt()
            return of(row, column)
        }
    }
}
