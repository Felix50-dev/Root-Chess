package com.example.chess.data.model

import javax.inject.Inject


class Board(initialBoxes: List<List<Spot>>? = null) {

    @Inject
    constructor() : this(null)

    var boxes: List<List<Spot>>

    init {
        // Reuse provided initialBoxes if present; avoid allocating 64 Spots and 32 ChessPieces during Board.clone()
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


    /**
     * Creates a deep copy of the board and its pieces.
     * Reuses immutable Position instances and passes clonedBoxes directly to the Board constructor
     * to avoid allocating redundant throwaway default board spots/pieces.
     * Preserves piece volatile state (hasMoved, canEnPassant).
     */
    fun clone(): Board {
        val clonedBoxes = boxes.map { row ->
            row.map { spot ->
                val pos = spot.position
                val clonedPiece = spot.chessPiece?.let { piece ->
                    when (piece) {
                        is Pawn -> Pawn(piece.color, pos, piece.isKilled, hasMoved = piece.hasMoved, canEnPassant = piece.canEnPassant)
                        is Rook -> Rook(piece.color, pos, piece.isKilled, hasMoved = piece.hasMoved, canEnPassant = piece.canEnPassant)
                        is Knight -> Knight(piece.color, pos, piece.isKilled, hasMoved = piece.hasMoved, canEnPassant = piece.canEnPassant)
                        is Bishop -> Bishop(piece.color, pos, piece.isKilled, hasMoved = piece.hasMoved, canEnPassant = piece.canEnPassant)
                        is Queen -> Queen(piece.color, pos, piece.isKilled, hasMoved = piece.hasMoved, canEnPassant = piece.canEnPassant)
                        is King -> King(piece.color, pos, piece.isKilled, hasMoved = piece.hasMoved, canEnPassant = piece.canEnPassant)
                    }
                }
                Spot(pos, clonedPiece)
            }
        }

        return Board(clonedBoxes)
    }


    /**
     * Checks if a given position is within the board boundaries (8x8 grid).
     * O(1) constant time check instead of O(NUM_ROWS * NUM_COLUMNS) 64-square scan loop.
     */
    fun isValidPosition(position: Position): Boolean {
        return position.row in 0 until NUM_ROWS && position.column in 0 until NUM_COLUMNS
    }


    companion object {
        const val NUM_ROWS: Int = 8
        const val NUM_COLUMNS: Int = 8
    }
}

