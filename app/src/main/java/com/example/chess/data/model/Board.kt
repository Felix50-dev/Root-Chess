package com.example.chess.data.model

import javax.inject.Inject
class Board @Inject constructor() {
    var boxes: List<List<Spot>>

    // The square a pawn can be captured on via en passant right now (i.e. the square
    // "behind" a pawn that just made a two-square move), or null if none is available.
    // Standard FEN-style tracking — mirrors the real chess rule instead of a mutable
    // per-piece flag, and is only ever written by GamePlay.makeMove().
    var enPassantTarget: Position? = null

    init {
        boxes = resetBoard()
    }

    fun getBox(row: Int, col: Int): Spot {
        require(row in 0 until NUM_ROWS && col in 0 until NUM_COLUMNS) {
            "Index out of bound: row=$row col=$col"
        }
        return boxes[row][col]
    }


    fun resetBoard(): List<List<Spot>> {
        enPassantTarget = null
        return List(8) { row ->
            List(8) { col ->
                when (row) {
                    0 -> createPieceSpot(row, col, Color.BLACK) // Black pieces
                    1 -> Spot(Position(row, col), Pawn(color = Color.BLACK, position =  Position(row, col), isKilled = false)) // Black pawns
                    6 -> Spot(Position(row, col), Pawn(color = Color.WHITE, position = Position(row, col), isKilled = false)) // White pawns
                    7 -> createPieceSpot(row, col, Color.WHITE) // White pieces
                    else -> Spot(Position(row, col), null) // Empty spots
                }
            }
        }
    }

    private fun createPieceSpot(row: Int, col: Int, color: Color): Spot {
        val position = Position(row, col)
        val piece = when (col) {
            0, 7 -> Rook(color = color, position = position, isKilled = false)
            1, 6 -> Knight(color = color, position = position, isKilled = false)
            2, 5 -> Bishop(color = color, position = position, isKilled = false)
            3 -> if (color == Color.BLACK) Queen(color = color, position = position, isKilled = false) else Queen(color = color, position = position, isKilled = false)
            4 -> if (color == Color.BLACK) King(color = color, position = position, isKilled = false) else King(color = color, position =  position, isKilled = false)
            else -> null
        }
        return Spot(position, piece)
    }


    fun clone(): Board {
        val clonedBoard = Board()

        // Create a deep copy of the `boxes` property
        val clonedBoxes = boxes.map { row ->
            row.map { spot ->
                // Clone the Spot
                val clonedPosition = Position(spot.position.row, spot.position.column)
                val clonedPiece = spot.chessPiece?.let { piece ->
                    // hasMoved must carry over — omitting it (as before) makes a clone
                    // think every piece is still on its starting square for castling
                    // purposes, regardless of what's actually happened in the real game.
                    when (piece) {
                        is Pawn -> Pawn(color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is Rook -> Rook(color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is Knight -> Knight(color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is Bishop -> Bishop(color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is Queen -> Queen(color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is King -> King(color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                    }
                }
                Spot(clonedPosition, clonedPiece)
            }
        }
        clonedBoard.boxes = clonedBoxes
        clonedBoard.enPassantTarget = enPassantTarget
        return clonedBoard
    }


    fun isValidPosition(position: Position): Boolean {
        var isPositionValid = false
        for (row in 0 until NUM_ROWS) {
            for (column in 0 until NUM_COLUMNS) {
                if (boxes[row][column].position == position
                ) isPositionValid = true
            }
        }
        return isPositionValid
    }


    companion object {
        const val NUM_ROWS: Int = 8
        const val NUM_COLUMNS: Int = 8
    }
}

