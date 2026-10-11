package com.example.chess.data.model

import javax.inject.Inject


class Board @Inject constructor() {
    var boxes: List<List<Spot>>

    init {
        boxes = resetBoard()
    }

    fun getBox(x: Int, y: Int): Spot {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            throw Exception("Index out of bound")
        }
        return boxes[x][y]
    }

    fun resetBoard(): List<List<Spot>> {
        return List(8) { row ->
            List(8) { col ->
                val pos = Position.of(row, col)
                when (row) {
                    0 -> createPieceSpot(row, col, Color.BLACK) // Black pieces
                    1 -> Spot(pos, Pawn(Color.BLACK, pos, isKilled = false)) // Black pawns
                    6 -> Spot(pos, Pawn(Color.WHITE, pos, isKilled = false)) // White pawns
                    7 -> createPieceSpot(row, col, Color.WHITE) // White pieces
                    else -> Spot(pos, null) // Empty spots
                }
            }
        }
    }

    private fun createPieceSpot(row: Int, col: Int, color: Color): Spot {
        val position = Position.of(row, col)
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


    fun clone(): Board {
        val clonedBoard = Board()

        // Create a deep copy of the `boxes` property
        val clonedBoxes = boxes.map { row ->
            row.map { spot ->
                // Clone the Spot with cached Position instance
                val clonedPosition = Position.of(spot.position.row, spot.position.column)
                val clonedPiece = spot.chessPiece?.let { piece ->
                    when (piece) {
                        is Pawn -> Pawn(piece.color, clonedPosition, piece.isKilled)
                        is Rook -> Rook(piece.color, clonedPosition, piece.isKilled)
                        is Knight -> Knight(piece.color, clonedPosition, piece.isKilled)
                        is Bishop -> Bishop(piece.color, clonedPosition, piece.isKilled)
                        is Queen -> Queen(piece.color, clonedPosition, piece.isKilled)
                        is King -> King(piece.color, clonedPosition, piece.isKilled)
                    }
                }
                Spot(clonedPosition, clonedPiece)
            }
        }

        clonedBoard.boxes = clonedBoxes
        return clonedBoard
    }


    // Optimized O(1) position validity check, replacing the O(N^2) 64-iteration loop
    fun isValidPosition(position: Position): Boolean {
        return position.row in 0 until NUM_ROWS && position.column in 0 until NUM_COLUMNS
    }


    companion object {
        const val NUM_ROWS: Int = 8
        const val NUM_COLUMNS: Int = 8
    }
}
