package com.example.chess.data.model

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
        // Convert algebraic notation (e.g., e2) to Position
        fun fromAlgebraicNotation(notation: String): Position {
            val column = notation[0] - 'a'  // 'a' is column 0, 'b' is 1, and so on
            val row = 8 - (notation[1].toString().toInt())  // '8' corresponds to row 0
            return Position(row, column)
        }
    }
}
