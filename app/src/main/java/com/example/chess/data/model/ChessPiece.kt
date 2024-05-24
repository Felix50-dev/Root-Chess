package com.example.chess.data.model

import android.util.Log
import com.example.chess.R
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

private const val TAG = "ChessPiece"

sealed class ChessPiece(
    val color: Color,
    var position: Position,
    var isKilled: Boolean,
    val vectorAsset: Int,
    var hasMoved: Boolean = false,
    var canEnPassant: Boolean = false,
) {
    abstract fun canMove(board: Board, start: Spot, end: Spot): Boolean
}

class Pawn(
    color: Color,
    position: Position,
    isKilled: Boolean,
    vectorAsset: Int = if (color == Color.WHITE) R.drawable.pawn_white else R.drawable.pawn_dark,
    hasMoved: Boolean = false,
    canEnPassant: Boolean = false
) :
    ChessPiece(color, position, isKilled, vectorAsset, hasMoved, canEnPassant) {
    override fun canMove(board: Board, start: Spot, end: Spot): Boolean {
        val rowDiff = end.position.row - start.position.row
        val colDiff = end.position.column - start.position.column

        // White pawns move forward (rowDiff < 0), black pawns move backward (rowDiff > 0)
        val direction = if (color == Color.WHITE) -1 else 1

        // Regular move: one square forward
        if (colDiff == 0 && rowDiff == direction && isEmpty(end)) {
            this.canEnPassant = false
            return true
        }

        //two moves forward
        if (colDiff == 0 && rowDiff == 2 * direction && start.position.isOnStartingRow()) {
            val intermediateRow = start.position.row + direction

            val intermediateSpot = board.getBox(intermediateRow, start.position.column)
            val endSpot = board.getBox(end.position.row, end.position.column)

            // Check if both intermediate and end spots are empty
            if (isEmpty(intermediateSpot) && isEmpty(endSpot)) {
                this.canEnPassant = true
                return true
            }
        }

        // Capture move: one square diagonally forward to capture opponent's piece
        if (rowDiff == direction && abs(colDiff) == 1 && !isEmpty(end) && end.chessPiece!!.color != color) {
            this.canEnPassant = false
            return true
        }

        if (isEnPassant(board, start, end)) {
            this.canEnPassant = false
            return true
        }

        return false
    }

    // Extension functions
    private fun isEmpty(spot: Spot): Boolean {
        return spot.chessPiece == null
    }

    private fun Position.isOnStartingRow(): Boolean {
        val startingRow = if (color == Color.WHITE) 6 else 1
        return row == startingRow
    }

    private fun canPromotePawn(
        position: Position,
        promotionPiece: ChessPiece?,
        board: Board
    ): Boolean {
        // Get the position of the pawn
        Log.d(TAG, "promotePawn: $position")

        // Check if the pawn is at the last row of the board
        val lastRow = if (this.color == Color.WHITE) 0 else Board.NUM_ROWS - 1
        Log.d(TAG, "promotePawn: $lastRow")
        if (position.row == lastRow) {
            // Promote the pawn by replacing it with the specified promotion piece
            board.getBox(position.row, position.column)
            if (promotionPiece != null) {
                return true
            }
        }
        return false
    }

    fun promotePawn(position: Position, promotionPiece: ChessPiece?, board: Board) {
        val spot = board.getBox(position.row, position.column)
        if (canPromotePawn(position, promotionPiece, board)) {
            spot.chessPiece = promotionPiece
            Log.d(TAG, "promotePawn: pawn promoted to $promotionPiece")
        }
    }

    fun isEnPassant(board: Board, start: Spot, end: Spot): Boolean {
        val rowDiff = end.position.row - start.position.row
        val colDiff = abs(end.position.column - start.position.column)
        val direction = if (color == Color.WHITE) -1 else 1

        // Check if the move is a diagonal move of one square
        if (colDiff == 1 && rowDiff == direction) {
            val adjacentSpot = board.getBox(start.position.row, end.position.column)
            val adjacentPawn = adjacentSpot.chessPiece as? Pawn

            if (adjacentPawn != null && adjacentPawn.color != color) {
                // Check if the adjacent pawn just moved two squares forward
                Log.d(
                    TAG,
                    "adjacentPawn color is : ${adjacentPawn.color} position is: ${adjacentPawn.position}"
                )
                Log.d(TAG, "can EnPassant: ${adjacentPawn.canEnPassant}")
                return adjacentPawn.canEnPassant
            }
        }
        return false
    }

    fun performEnPassant(board: Board, start: Spot, end: Spot) {
        val adjacentSpot = board.getBox(start.position.row, end.position.column)
        // Capture the adjacent pawn
        adjacentSpot.chessPiece = null
        // Move the pawn to the destination square
        end.chessPiece = start.chessPiece
        start.chessPiece = null
    }
}



class Rook(
    color: Color,
    position: Position,
    isKilled: Boolean,
    vectorAsset: Int = if (color == Color.WHITE) R.drawable.rook_white else R.drawable.rook_dark,
    hasMoved: Boolean = false
) :
    ChessPiece(color, position, isKilled, vectorAsset, hasMoved) {
    override fun canMove(board: Board, start: Spot, end: Spot): Boolean {

        if (end.chessPiece?.color == this.color) {
            return false
        }

        // Check if the end spot is in the same row or column as the start spot and whether path is clear
        return start.position.isSameRowOrColumnAs(end.position) &&
                isPathClear(board, start.position, end.position)
    }

    private fun Position.isSameRowOrColumnAs(other: Position): Boolean {
        return this.row == other.row || this.column == other.column
    }

    private fun isPathClear(board: Board, start: Position, end: Position): Boolean {
        // Check if the path from start to end is clear of obstacles (other pieces)
        // Check if the path from start to end is clear of obstacles (other pieces)
        if (start.row == end.row) {
            // Moving horizontally (same row)
            val minColumn = min(start.column, end.column)
            val maxColumn = max(start.column, end.column)
            for (column in (minColumn + 1) until maxColumn) {
                val spot = board.getBox(start.row, column)
                if (spot.chessPiece != null) {
                    return false // Path is obstructed by a piece
                }
            }
        } else if (start.column == end.column) {
            // Moving vertically (same column)
            val minRow = min(start.row, end.row)
            val maxRow = max(start.row, end.row)
            for (row in (minRow + 1) until maxRow) {
                val spot = board.getBox(row, start.column)
                if (spot.chessPiece != null) {
                    return false // Path is obstructed by a piece
                }
            }
        }
        return true // Path is clear
    }

}

class Knight(
    color: Color,
    position: Position,
    isKilled: Boolean,
    vectorAsset: Int = if (color == Color.WHITE) R.drawable.knight_white else R.drawable.knight_dark,
    hasMoved: Boolean = false
) :
    ChessPiece(color, position, isKilled, vectorAsset, hasMoved) {
    override fun canMove(board: Board, start: Spot, end: Spot): Boolean {
        val rowDiff = abs(end.position.row - start.position.row)
        val colDiff = abs(end.position.column - start.position.column)

        if (end.chessPiece?.color == this.color) {
            return false
        }

        // Knight moves in an L-shaped pattern: 2 squares in one direction and 1 square perpendicular
        return (rowDiff == 2 && colDiff == 1) || (rowDiff == 1 && colDiff == 2)
    }

}

class Bishop(
    color: Color,
    position: Position,
    isKilled: Boolean,
    vectorAsset: Int = if (color == Color.WHITE) R.drawable.bishop_white else R.drawable.bishop_dark,
    hasMoved: Boolean = false
) :
    ChessPiece(color, position, isKilled, vectorAsset, hasMoved) {
    override fun canMove(board: Board, start: Spot, end: Spot): Boolean {

        if (end.chessPiece?.color == this.color) {
            return false
        }

        val rowDiff = abs(end.position.row - start.position.row)
        val colDiff = abs(end.position.column - start.position.column)

        // Bishop moves diagonally, so the row and column differences must be equal
        return rowDiff == colDiff && isPathClearForBishop(board, start, end)
    }

    private fun isPathClearForBishop(board: Board, start: Spot, end: Spot): Boolean {
        val startRow = start.position.row
        val startCol = start.position.column
        val endRow = end.position.row
        val endCol = end.position.column

        // Calculate the row and column differences
        val rowDiff = endRow - startRow
        val colDiff = endCol - startCol

        // Ensure that the bishop is moving diagonally
        if (abs(rowDiff) != abs(colDiff)) {
            return false
        }

        // Determine the direction of movement
        val rowIncrement = if (rowDiff > 0) 1 else -1
        val colIncrement = if (colDiff > 0) 1 else -1

        // Check each position along the diagonal path
        var currentRow = startRow + rowIncrement
        var currentCol = startCol + colIncrement
        while (currentRow != endRow && currentCol != endCol) {
            val spot = board.getBox(currentRow, currentCol)
            if (spot.chessPiece != null) {
                // Path is obstructed by a piece
                return false
            }
            // Move to the next position along the diagonal path
            currentRow += rowIncrement
            currentCol += colIncrement
        }

        // Path is clear
        return true
    }


}

class Queen(
    color: Color,
    position: Position,
    isKilled: Boolean,
    vectorAsset: Int = if (color == Color.WHITE) R.drawable.queen_white else R.drawable.queen_dark,
    hasMoved: Boolean = false
) :
    ChessPiece(color, position, isKilled, vectorAsset, hasMoved) {

    override fun canMove(board: Board, start: Spot, end: Spot): Boolean {

        // Check if the queen is moving along a straight line or a diagonal
        return (start.position.isSameRowOrColumnAs(end.position) || start.position.isDiagonalTo(
            end.position
        )) && isPathClear(board, start.position, end.position)

    }

    private fun Position.isSameRowOrColumnAs(other: Position): Boolean {
        return this.row == other.row || this.column == other.column
    }

    private fun Position.isDiagonalTo(other: Position): Boolean {
        val rowDiff = abs(other.row - this.row)
        val colDiff = abs(other.column - this.column)
        return rowDiff == colDiff
    }

    private fun isPathClear(board: Board, start: Position, end: Position): Boolean {
        // Check if the path from start to end is clear of obstacles (other pieces)
        if (start.row == end.row) {
            // Moving horizontally (same row)
            val minColumn = min(start.column, end.column)
            val maxColumn = max(start.column, end.column)
            for (column in (minColumn + 1) until maxColumn) {
                val spot = board.getBox(start.row, column)
                if (spot.chessPiece != null) {
                    return false // Path is obstructed by a piece
                }
            }
        } else if (start.column == end.column) {
            // Moving vertically (same column)
            val minRow = min(start.row, end.row)
            val maxRow = max(start.row, end.row)
            for (row in (minRow + 1) until maxRow) {
                val spot = board.getBox(row, start.column)
                if (spot.chessPiece != null) {
                    return false // Path is obstructed by a piece
                }
            }
        } else {
            // Moving diagonally
            val rowIncrement = if (end.row > start.row) 1 else -1
            val colIncrement = if (end.column > start.column) 1 else -1
            var currentRow = start.row + rowIncrement
            var currentCol = start.column + colIncrement
            while (currentRow != end.row && currentCol != end.column) {
                val spot = board.getBox(currentRow, currentCol)
                if (spot.chessPiece != null) {
                    return false // Path is obstructed by a piece
                }
                currentRow += rowIncrement
                currentCol += colIncrement
            }
        }
        // Path is clear or contains an opponent's piece
        val endSpot = board.getBox(end.row, end.column)
        return endSpot.chessPiece == null || endSpot.chessPiece!!.color != color
    }

}

class King(
    color: Color,
    position: Position,
    isKilled: Boolean,
    vectorAsset: Int = if (color == Color.WHITE) R.drawable.king_white else R.drawable.king_dark,
    hasMoved: Boolean = false
) :
    ChessPiece(color, position, isKilled, vectorAsset, hasMoved) {
    override fun canMove(board: Board, start: Spot, end: Spot): Boolean {
        val rowDiff = abs(end.position.row - start.position.row)
        val columnDiff = abs(end.position.column - start.position.column)

        // Check if the destination spot corresponds to a castling move
        if (rowDiff == 0 && columnDiff == 2) {
            // If it's a castling move, use the canCastle function to determine if the move is valid
            return canCastle(start, end, board)
        }

        // Check if the destination spot is either empty or occupied by an opponent's piece
        val isDestinationEmptyOrOpponent = end.chessPiece == null || end.chessPiece!!.color != color
        return rowDiff <= 1 && columnDiff <= 1 && isDestinationEmptyOrOpponent
    }

    fun canCastle(start: Spot, end: Spot, board: Board): Boolean {
        val rowDiff = abs(end.position.row - start.position.row)
        val colDiff = (end.position.column - start.position.column)

        if (colDiff == 2 && rowDiff == 0) { // King-side castling
            // Ensure the king and rook haven't moved
            val kingSideRookSpot = board.getBox(start.position.row, Board.NUM_COLUMNS - 1)
            val kingSideRook = kingSideRookSpot.chessPiece as? Rook ?: return false
            if (hasMoved || kingSideRook.hasMoved) {
                return false
            }
            // Ensure the squares between the king and rook are empty
            for (col in (start.position.column + 1) until (end.position.column)) {
                val intermediateSpot = board.getBox(start.position.row, col)
                if (!isEmpty(intermediateSpot)) {
                    return false
                }
            }

            val color = if (color == Color.WHITE) Color.BLACK else Color.WHITE
            // Ensure the king is not in check and squares the king moves through are not under attack
            return !(isCheck(board, color) || isUnderAttack(board, start.position, color))
        } else if (colDiff == -2 && rowDiff == 0) { // Queen-side castling
            // Implement similar logic for queen-side castling
            // Ensure the king and rook haven't moved
            val queenSideRookSpot = board.getBox(start.position.row, 0)
            val queenSideRook = queenSideRookSpot.chessPiece as? Rook
            if (queenSideRook?.hasMoved == true || hasMoved) {
                return false
            }
            // Ensure the squares between the king and rook are empty
            for (col in (end.position.column + 1) until (start.position.column)) {
                val intermediateSpot = board.getBox(start.position.row, col)
                if (!isEmpty(intermediateSpot)) {
                    return false
                }
            }
            // Ensure the king is not in check and squares the king moves through are not under attack
            val color = if (color == Color.WHITE) Color.BLACK else Color.WHITE
            return !isCheck(board, color) && !isUnderAttack(board, start.position, color)
        }
        return false
    }

    fun castlingMove(start: Spot, end: Spot, board: Board) {
        val rowDiff = abs(end.position.row - start.position.row)
        val colDiff = (end.position.column - start.position.column)

        val kingSideRookSpot = board.getBox(start.position.row, Board.NUM_COLUMNS - 1)
        val queenSideRookSpot = board.getBox(start.position.row, 0)
        val queenSideRook = queenSideRookSpot.chessPiece as? Rook
        val kingSideRook = kingSideRookSpot.chessPiece as? Rook

        if (colDiff == 2 && rowDiff == 0) {
            // Move the king
            end.chessPiece = this
            start.chessPiece = null
            // Move the rook
            val rookStart = board.getBox(start.position.row, Board.NUM_COLUMNS - 1)
            val rookEnd = board.getBox(start.position.row, start.position.column + 1)
            rookEnd.chessPiece = kingSideRook
            rookStart.chessPiece = null
        } else {
            // Move the king
            end.chessPiece = this
            start.chessPiece = null
            // Move the rook
            val rookStart = board.getBox(start.position.row, 0)
            val rookEnd = board.getBox(start.position.row, start.position.column - 1)
            rookEnd.chessPiece = queenSideRook
            rookStart.chessPiece = null
        }
    }

    // Extension functions
    private fun isEmpty(spot: Spot): Boolean {
        return spot.chessPiece == null
    }

    private fun isUnderAttack(board: Board, position: Position, attackerColor: Color): Boolean {
        // Iterate through all spots on the board
        for (row in 0 until Board.NUM_ROWS) {
            for (col in 0 until Board.NUM_COLUMNS) {
                val spot = board.getBox(row, col)
                val piece = spot.chessPiece
                // Check if there is a piece on the spot and it belongs to the opponent
                if (piece != null && piece.color != attackerColor) {
                    // Check if the opponent's piece can move to the target position
                    if (piece.canMove(board, spot, board.getBox(position.row, position.column))) {
                        return true // The position is under attack
                    }
                }
            }
        }
        return false // The position is not under attack
    }

    private fun isCheck(board: Board, attackerColor: Color): Boolean {
        // Iterate through all spots on the board
        for (row in 0 until Board.NUM_ROWS) {
            for (col in 0 until Board.NUM_COLUMNS) {
                val spot = board.getBox(row, col)
                val piece = spot.chessPiece
                // Check if there is a piece on the spot and it belongs to the opponent
                if (piece != null && piece.color == attackerColor) {
                    // Check if the opponent's piece can move to the king's position
                    if (piece.canMove(board, spot, board.getBox(position.row, position.column))) {
                        return true // The king is in check
                    }
                }
            }
        }
        return false // The king is not in check
    }

}

