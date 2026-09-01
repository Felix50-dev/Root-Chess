package com.example.chess.data.model

import javax.inject.Inject


class Board(initialBoxes: List<List<Spot>>? = null) {
    @Inject
    constructor() : this(null)

    var boxes: List<List<Spot>>

    init {
        boxes = initialBoxes ?: resetBoard()
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
            3 -> if (color == Color.BLACK) Queen(color, position, isKilled = false) else Queen(color, position, isKilled = false)
            4 -> if (color == Color.BLACK) King(color, position, isKilled = false) else King(color, position, isKilled = false)
            else -> null
        }
        return Spot(position, piece)
    }


    fun clone(): Board {
        // Create a deep copy of boxes while reusing immutable Position instances and cloning piece state
        val clonedBoxes = boxes.map { row ->
            row.map { spot ->
                // Position is immutable, reuse position reference to avoid allocating 64 extra objects per clone
                val position = spot.position
                val clonedPiece = spot.chessPiece?.let { piece ->
                    when (piece) {
                        is Pawn -> Pawn(piece.color, position, piece.isKilled, piece.vectorAsset, piece.hasMoved, piece.canEnPassant)
                        is Rook -> Rook(piece.color, position, piece.isKilled, piece.vectorAsset, piece.hasMoved, piece.canEnPassant)
                        is Knight -> Knight(piece.color, position, piece.isKilled, piece.vectorAsset, piece.hasMoved, piece.canEnPassant)
                        is Bishop -> Bishop(piece.color, position, piece.isKilled, piece.vectorAsset, piece.hasMoved, piece.canEnPassant)
                        is Queen -> Queen(piece.color, position, piece.isKilled, piece.vectorAsset, piece.hasMoved, piece.canEnPassant)
                        is King -> King(piece.color, position, piece.isKilled, piece.vectorAsset, piece.hasMoved, piece.canEnPassant)
                    }
                }
                Spot(position, clonedPiece)
            }
        }

        // Pass clonedBoxes directly to primary constructor to bypass redundant resetBoard() call
        return Board(clonedBoxes)
    }


    fun isValidPosition(position: Position): Boolean {
        // O(1) range check replacing O(N^2) 64-square board traversal
        return position.row in 0 until NUM_ROWS && position.column in 0 until NUM_COLUMNS
    }


    companion object {
        const val NUM_ROWS: Int = 8
        const val NUM_COLUMNS: Int = 8
    }
}

