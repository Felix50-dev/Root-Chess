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
                val pos = Position.of(row, col)
                when (row) {
                    0 -> createPieceSpot(row, col, Color.BLACK) // Black pieces
                    1 -> Spot(pos, Pawn(color = Color.BLACK, position = pos, isKilled = false)) // Black pawns
                    6 -> Spot(pos, Pawn(color = Color.WHITE, position = pos, isKilled = false)) // White pawns
                    7 -> createPieceSpot(row, col, Color.WHITE) // White pieces
                    else -> Spot(pos, null) // Empty spots
                }
            }
        }
    }

    private fun createPieceSpot(row: Int, col: Int, color: Color): Spot {
        val position = Position.of(row, col)
        val piece = when (col) {
            0, 7 -> Rook(color = color, position = position, isKilled = false)
            1, 6 -> Knight(color = color, position = position, isKilled = false)
            2, 5 -> Bishop(color = color, position = position, isKilled = false)
            3 -> Queen(color = color, position = position, isKilled = false)
            4 -> King(color = color, position = position, isKilled = false)
            else -> null
        }
        return Spot(position, piece)
    }


    fun clone(): Board {
        val clonedBoard = Board()

        // Create a deep copy of the `boxes` property
        val clonedBoxes = boxes.map { row ->
            row.map { spot ->
                // Position is an immutable value (val row, val column) — reuse it directly
                // instead of allocating a duplicate. Only the mutable ChessPiece/Spot wrappers
                // need real copies to keep the clone isolated from the live board.
                val clonedPosition = spot.position
                val clonedPiece = spot.chessPiece?.let { piece ->
                    // hasMoved must carry over — omitting it (as before) makes a clone
                    // think every piece is still on its starting square for castling
                    // purposes, regardless of what's actually happened in the real game.
                    //
                    // id must carry over too — it defaults to a fresh random UUID, and
                    // callers that diff pieces across two separate clones by id (e.g.
                    // TacticsRecognizer.isDiscoveredCheck) need the same logical piece
                    // to keep the same id after cloning, or every clone looks like an
                    // entirely new, unrelated set of pieces.
                    when (piece) {
                        is Pawn -> Pawn(id = piece.id, color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is Rook -> Rook(id = piece.id, color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is Knight -> Knight(id = piece.id, color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is Bishop -> Bishop(id = piece.id, color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is Queen -> Queen(id = piece.id, color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
                        is King -> King(id = piece.id, color = piece.color, position = clonedPosition, isKilled = piece.isKilled, hasMoved = piece.hasMoved)
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
        // Direct O(1) range check replaces scanning all 64 board squares
        return position.row in 0 until NUM_ROWS && position.column in 0 until NUM_COLUMNS
    }


    companion object {
        const val NUM_ROWS: Int = 8
        const val NUM_COLUMNS: Int = 8
    }
}
