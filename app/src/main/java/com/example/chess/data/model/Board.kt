package com.example.chess.data.model

import android.util.Log
import javax.inject.Inject


private const val TAG = "Board"

class Board @Inject constructor() {
    lateinit var boxes: Array<Array<Spot>>

    init {
        resetBoard()
    }

    fun getBox(x: Int, y: Int): Spot {
        if (x < 0 || x > 7 || y < 0 || y > 7) {
            throw Exception("Index out of bound")
        }
        return boxes[x][y]
    }

    fun resetBoard() {

        // Initialize boxes property
        boxes = Array(8) { Array(8) { Spot(Position(0, 0), null) } }

        // initialize black pieces
        boxes[0][0] =
            Spot(
                Position(0, 0),
                Rook(Color.BLACK, position = Position(0, 0), isKilled = false)
            )
        boxes[0][1] =
            Spot(
                Position(0, 1),
                Knight(Color.BLACK, position = Position(0, 1), isKilled = false)
            )
        boxes[0][2] =
            Spot(
                Position(0, 2),
                Bishop(Color.BLACK, position = Position(0, 2), isKilled = false)
            )
        boxes[0][3] =
            Spot(Position(0, 3), Queen(Color.BLACK, position = Position(0, 3), isKilled = false))
        boxes[0][4] =
            Spot(Position(0, 4), King(Color.BLACK, position = Position(0, 4), isKilled = false))
        boxes[0][5] =
            Spot(Position(0, 5), Bishop(Color.BLACK, position = Position(0, 5), isKilled = false))
        boxes[0][6] =
            Spot(Position(0, 6), Knight(Color.BLACK, position = Position(0, 6), isKilled = false))
        boxes[0][7] =
            Spot(Position(0, 7), Rook(Color.BLACK, position = Position(0, 7), isKilled = false))
        //second row
        boxes[1][0] =
            Spot(Position(1, 0), Pawn(Color.BLACK, position = Position(1, 0), isKilled = false))
        boxes[1][1] =
            Spot(Position(1, 1), Pawn(Color.BLACK, position = Position(1, 1), isKilled = false))
        boxes[1][2] =
            Spot(Position(1, 2), Pawn(Color.BLACK, position = Position(1, 2), isKilled = false))
        boxes[1][3] =
            Spot(Position(1, 3), Pawn(Color.BLACK, position = Position(1, 3), isKilled = false))
        boxes[1][4] =
            Spot(Position(1, 4), Pawn(Color.BLACK, position = Position(1, 4), isKilled = false))
        boxes[1][5] =
            Spot(Position(1, 5), Pawn(Color.BLACK, position = Position(1, 5), isKilled = false))
        boxes[1][6] =
            Spot(Position(1, 6), Pawn(Color.BLACK, position = Position(1, 6), isKilled = false))
        boxes[1][7] =
            Spot(Position(1, 7), Pawn(Color.BLACK, position = Position(1, 7), isKilled = false))

        // initialize black pieces
        boxes[7][0] =
            Spot(Position(7, 0), Rook(Color.WHITE, position = Position(7, 0), isKilled = false))
        boxes[7][1] =
            Spot(Position(7, 1), Knight(Color.WHITE, position = Position(7, 1), isKilled = false))
        boxes[7][2] =
            Spot(Position(7, 2), Bishop(Color.WHITE, position = Position(7, 2), isKilled = false))
        boxes[7][3] =
            Spot(Position(7, 3), Queen(Color.WHITE, position = Position(7, 3), isKilled = false))
        boxes[7][4] =
            Spot(Position(7, 4), King(Color.WHITE, position = Position(7, 4), isKilled = false))
        boxes[7][5] =
            Spot(Position(7, 5), Bishop(Color.WHITE, position = Position(7, 5), isKilled = false))
        boxes[7][6] =
            Spot(Position(7, 6), Knight(Color.WHITE, position = Position(7, 6), isKilled = false))
        boxes[7][7] =
            Spot(Position(7, 7), Rook(Color.WHITE, position = Position(7, 7), isKilled = false))
        //second row
        boxes[6][0] =
            Spot(Position(6, 0), Pawn(Color.WHITE, position = Position(6, 0), isKilled = false))
        boxes[6][1] =
            Spot(Position(6, 1), Pawn(Color.WHITE, position = Position(6, 1), isKilled = false))
        boxes[6][2] =
            Spot(Position(6, 2), Pawn(Color.WHITE, position = Position(6, 2), isKilled = false))
        boxes[6][3] =
            Spot(Position(6, 3), Pawn(Color.WHITE, position = Position(6, 3), isKilled = false))
        boxes[6][4] =
            Spot(Position(6, 4), Pawn(Color.WHITE, position = Position(6, 4), isKilled = false))
        boxes[6][5] =
            Spot(Position(6, 5), Pawn(Color.WHITE, position = Position(6, 5), isKilled = false))
        boxes[6][6] =
            Spot(Position(6, 6), Pawn(Color.WHITE, position = Position(6, 6), isKilled = false))
        boxes[6][7] =
            Spot(Position(6, 7), Pawn(Color.WHITE, position = Position(6, 7), isKilled = false))

        // initialize remaining boxes without any piece
        for (i in 2..5) {
            for (j in 0..7) {
                boxes[i][j] = Spot(Position(i, j), null)
                val notation = boxes[i][j].position.toAlgebraicNotation()
                Log.d(TAG, "resetBoard: notation is $notation")
            }
        }
    }

    fun clone(): Board {
        val clonedBoard = Board()
        // Copy the state of each spot from the original board to the cloned board
        for (row in 0 until NUM_ROWS) {
            for (column in 0 until NUM_COLUMNS) {
                val originalSpot = boxes[row][column]
                val clonedSpot = clonedBoard.getBox(row, column)
                val chessPiece = originalSpot.chessPiece
                val position = originalSpot.position
                clonedSpot.position = position
                // Copy the state of the spot (including the chess piece, if any)
                if (chessPiece != null) {
                    val clonedChessPiece = when (chessPiece) {
                        is Pawn -> Pawn(chessPiece.color, position, chessPiece.isKilled)
                        is Rook -> Rook(chessPiece.color, position, chessPiece.isKilled)
                        is Queen -> Queen(chessPiece.color, position, chessPiece.isKilled)
                        is Knight -> Knight(chessPiece.color, position, chessPiece.isKilled)
                        is King -> King(chessPiece.color, position, chessPiece.isKilled)
                        is Bishop -> Bishop(chessPiece.color, position, chessPiece.isKilled)
                    }
                    clonedSpot.chessPiece = clonedChessPiece
                }
                Log.d(TAG, "clone: chessSpot is $clonedSpot")
            }
        }
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

