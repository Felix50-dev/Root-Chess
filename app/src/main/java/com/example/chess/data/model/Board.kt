package com.example.chess.data.model

import javax.inject.Inject


class Board(
    var boxes: List<List<Spot>> = resetBoard()
) {
    @Inject
    constructor() : this(resetBoard())

    fun getBox(x: Int, y: Int): Spot {
        if (x < 0 || x >= NUM_ROWS || y < 0 || y >= NUM_COLUMNS) {
            throw Exception("Index out of bound")
        }
        return boxes[x][y]
    }

    /**
     * Efficiently clones the board.
     * Reuses immutable Position objects and correctly copies volatile piece states.
     * Bypasses default board initialization for better performance.
     */
    fun clone(): Board {
        val clonedBoxes = boxes.map { row ->
            row.map { spot ->
                val clonedPiece = spot.chessPiece?.let { piece ->
                    when (piece) {
                        is Pawn -> Pawn(piece.color, piece.position, piece.isKilled, hasMoved = piece.hasMoved, canEnPassant = piece.canEnPassant)
                        is Rook -> Rook(piece.color, piece.position, piece.isKilled, hasMoved = piece.hasMoved)
                        is Knight -> Knight(piece.color, piece.position, piece.isKilled, hasMoved = piece.hasMoved)
                        is Bishop -> Bishop(piece.color, piece.position, piece.isKilled, hasMoved = piece.hasMoved)
                        is Queen -> Queen(piece.color, piece.position, piece.isKilled, hasMoved = piece.hasMoved)
                        is King -> King(piece.color, piece.position, piece.isKilled, hasMoved = piece.hasMoved)
                    }
                }
                Spot(spot.position, clonedPiece)
            }
        }
        return Board(clonedBoxes)
    }

    /**
     * Optimized validation using O(1) boundary checks instead of O(N^2) scan.
     */
    fun isValidPosition(position: Position): Boolean {
        return position.row in 0 until NUM_ROWS &&
                position.column in 0 until NUM_COLUMNS
    }

    companion object {
        const val NUM_ROWS: Int = 8
        const val NUM_COLUMNS: Int = 8

        fun resetBoard(): List<List<Spot>> {
            return List(NUM_ROWS) { row ->
                List(NUM_COLUMNS) { col ->
                    when (row) {
                        0 -> createPieceSpot(row, col, Color.BLACK) // Black pieces
                        1 -> Spot(Position(row, col), Pawn(Color.BLACK, Position(row, col), isKilled = false)) // Black pawns
                        6 -> Spot(Position(row, col), Pawn(Color.WHITE, Position(row, col), isKilled = false)) // White pawns
                        7 -> createPieceSpot(row, col, Color.WHITE) // White pieces
                        else -> Spot(Position(row, col), null) // Empty spots
                    }
                }
            }
        }

        private fun createPieceSpot(row: Int, col: Int, color: Color): Spot {
            val position = Position(row, col)
            val piece = when (col) {
                0, 7 -> Rook(color, position, isKilled = false)
                1, 6 -> Knight(color, position, isKilled = false)
                2, 5 -> Bishop(color, position, isKilled = false)
                3 -> Queen(color, position, isKilled = false)
                4 -> King(color, position, isKilled = false)
                else -> null
            }
            return Spot(position, piece)
        }
    }
}

