package com.example.chess.data.repository

import android.util.Log
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Move
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import javax.inject.Inject
import kotlin.math.abs
import kotlin.math.sign

private const val TAG = "TacticsRecognizer"

class TacticsRecognizer @Inject constructor(
    private val game: GamePlay
) {

    private companion object {
        // Forks on pawns (value=1) excluded — only tactically significant forks
        // Change to 1 if you want to include pawn forks
        const val MIN_FORK_TARGET_VALUE = 3
    }

    fun isFork(board: Board, move: Move, player: Player): String? {
        val piece = move.pieceMoved ?: return null
        if (piece is King) return null  // kings don't fork

        // Simulate on tempBoard only
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = piece

        // Get all squares the piece attacks from its new position
        val attackedPieces = mutableListOf<ChessPiece>()

        for (row in 0..7) {
            for (col in 0..7) {
                val targetSquare = tempBoard.getBox(row, col)
                val target = targetSquare.chessPiece ?: continue
                if (target.color == piece.color) continue  // skip own pieces

                val fromSpot = tempBoard.getBox(move.to.position.row, move.to.position.column)
                if (piece.canMove(tempBoard, fromSpot, targetSquare)) {
                    if (target.value >= MIN_FORK_TARGET_VALUE) {
                        attackedPieces.add(target)
                    }
                }
            }
        }

        Log.d(TAG, "isFork: attacked pieces are $attackedPieces")

        return if (attackedPieces.size >= 2) "Fork" else null
    }


    fun isPin(board: Board, move: Move, player: Player): String? {
        val movedPiece = move.pieceMoved ?: return null
        if (movedPiece !is Queen && movedPiece !is Rook && movedPiece !is Bishop) return null

        // Simulate on tempBoard
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = movedPiece

        val opponentColor = if (movedPiece.color == Color.WHITE) Color.BLACK else Color.WHITE
        val opponentKingSpot = game.findKing(tempBoard, opponentColor) ?: return null

        // Attacker destination must be aligned with opponent king
        if (!isAligned(move.to.position, opponentKingSpot.position)) return null

        // Squares between attacker destination and king
        val betweenSquares = squaresBetween(move.to.position, opponentKingSpot.position)

        // Collect pieces between attacker and king
        val blockingPieces = betweenSquares.mapNotNull { sq ->
            tempBoard.getBox(sq.row, sq.column).chessPiece
        }

        // Must be exactly ONE opponent piece between attacker and king
        if (blockingPieces.size != 1) return null
        val pinnedPiece = blockingPieces.first()
        if (pinnedPiece.color == movedPiece.color) return null

        // Pin verification — simply remove the pinned piece and check
        // if attacker can reach king directly
        val verifyBoard = tempBoard.clone()
        verifyBoard.getBox(
            pinnedPiece.position.row,
            pinnedPiece.position.column
        ).chessPiece = null

        val attackerSpot = verifyBoard.getBox(
            move.to.position.row,
            move.to.position.column
        )
        val kingSpot = verifyBoard.getBox(
            opponentKingSpot.position.row,
            opponentKingSpot.position.column
        )

        // If attacker can reach king when pinned piece is removed — it's a real pin
        return if (movedPiece.canMove(verifyBoard, attackerSpot, kingSpot)) "Pin" else null
    }

    fun isDiscoveredCheck(board: Board, move: Move, player: Player): String? {
        val piece = move.pieceMoved ?: return null

        // Simulate on tempBoard
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = piece

        // Use a pre-move board that still has moving piece removed from origin
        // so path-blocking is evaluated correctly in both states
        val preMoveBoard = board.clone()
        preMoveBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null

        val beforeAttackerIds = attackersToOpponentKing(preMoveBoard, player)
            .map { it.id }
            .toSet()
        Log.d(TAG, "isDiscoveredCheck: beforeAttackerIds: $beforeAttackerIds")

        val afterAttackers = attackersToOpponentKing(tempBoard, player)
        Log.d(TAG, "isDiscoveredCheck: afterAttackers: $afterAttackers")

        // New attackers = pieces attacking king after move that weren't before
        // Exclude the moving piece itself — that's just a regular check
        val newAttackers = afterAttackers.filter { afterPiece ->
            afterPiece.id !in beforeAttackerIds && afterPiece.id != piece.id
        }
        Log.d(TAG, "isDiscoveredCheck: newAttackers: $newAttackers")

        return if (newAttackers.isNotEmpty()) "Discovered Check" else null
    }

    private fun attackersToOpponentKing(board: Board, player: Player): List<ChessPiece> {
        val opponentColor = if (player.isWhite) Color.BLACK else Color.WHITE
        val kingSpot = game.findKing(board, opponentColor) ?: return emptyList()
        val kingPos = kingSpot.position

        return game.getAlivePieces(player, board).filter { piece ->
            val fromSpot = board.getBox(piece.position.row, piece.position.column)
            val toSpot = board.getBox(kingPos.row, kingPos.column)
            piece.canMove(board, fromSpot, toSpot)
        }
    }

    fun isSkewer(board: Board, move: Move): String? {
        val piece = move.pieceMoved ?: return null
        if (piece !is Rook && piece !is Bishop && piece !is Queen) return null

        // Simulate entirely on tempBoard
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = piece

        // Only check directions valid for the piece type
        val directions = when (piece) {
            is Rook -> listOf(
                Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1)
            )
            is Bishop -> listOf(
                Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)
            )
            is Queen -> listOf(
                Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1),
                Pair(1, 1), Pair(1, -1), Pair(-1, 1), Pair(-1, -1)
            )
            else -> return null
        }

        for ((dr, dc) in directions) {
            var r = move.to.position.row + dr
            var c = move.to.position.column + dc

            var firstPiece: ChessPiece? = null
            var secondPiece: ChessPiece? = null

            while (r in 0..7 && c in 0..7) {
                val sqPiece = tempBoard.getBox(r, c).chessPiece  // ← use tempBoard
                if (sqPiece != null) {
                    if (sqPiece.color == piece.color) break

                    if (firstPiece == null) {
                        firstPiece = sqPiece
                    } else {
                        secondPiece = sqPiece
                        break
                    }
                }
                r += dr
                c += dc
            }

            if (firstPiece != null && secondPiece != null &&
                pieceValue(firstPiece) > pieceValue(secondPiece)
            ) {
                return "Skewer"
            }
        }

        return null
        // No undo needed — tempBoard is a clone
    }

    private fun pieceValue(piece: ChessPiece): Int {
        return when (piece) {
            is King -> 1000
            is Queen -> 9
            is Rook -> 5
            is Bishop, is Knight -> 3
            is Pawn -> 1
        }
    }

    fun isDoubleCheck(board: Board, move: Move, player: Player): String? {
        val piece = move.pieceMoved ?: return null

        // Simulate entirely on tempBoard
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = piece

        val opponentColor = if (player.isWhite) Color.BLACK else Color.WHITE
        val kingPos = game.findKing(tempBoard, opponentColor)?.position ?: return null

        // Count how many player pieces attack the king after the move
        val attackers = game.getAlivePieces(player, tempBoard)
            .filter { attackingPiece ->
                val from = tempBoard.getBox(attackingPiece.position.row, attackingPiece.position.column)
                val kingSpot = tempBoard.getBox(kingPos.row, kingPos.column)
                attackingPiece.canMove(tempBoard, from, kingSpot)  // use canMove directly, no side effects
            }

        Log.d(TAG, "isDoubleCheck: attackers are $attackers")

        // No need to undo — tempBoard is a clone
        return if (attackers.size >= 2) "Double Check" else null
    }


    private fun squaresBetween(a: Position, b: Position): List<Position> {
        val squares = mutableListOf<Position>()

        val rowDiff = b.row - a.row
        val colDiff = b.column - a.column

        val rowStep = rowDiff.sign
        val colStep = colDiff.sign

        // Check alignment: vertical, horizontal, or diagonal
        val isSameRow = rowDiff == 0
        val isSameCol = colDiff == 0
        val isDiagonal = abs(rowDiff) == abs(colDiff)

        if (!isSameRow && !isSameCol && !isDiagonal) return emptyList() // not aligned

        var currentRow = a.row + rowStep
        var currentCol = a.column + colStep

        while (currentRow != b.row || currentCol != b.column) {
            squares.add(Position(currentRow, currentCol))
            currentRow += rowStep
            currentCol += colStep
        }

        return squares
    }

    private fun isAligned(a: Position, b: Position): Boolean {
        return a.row == b.row || a.column == b.column ||
                abs(a.row - b.row) == abs(a.column - b.column)
    }

    fun Position.next(direction: Pair<Int, Int>): Position? {
        val newRow = row + direction.first
        val newCol = column + direction.second
        return if (newRow in 0..7 && newCol in 0..7) Position(newRow, newCol) else null
    }

}
