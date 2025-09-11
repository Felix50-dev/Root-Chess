package com.example.chess.data.repository

import android.util.Log
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.Move
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
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

    fun isFork(board: Board, move: Move, player: Player): String? {
        Log.d(TAG, "isFork: move is $move")

        val piece = move.pieceMoved ?: return null

        // --- simulate the move ---
        val originalFromPiece = move.from.chessPiece
        val originalToPiece = move.to.chessPiece

        move.from.chessPiece = null
        move.to.chessPiece = piece

        // now we can check attacked squares
        val attackedSquares = game.getValidMoves(move.to.position, player,board)
        Log.d(TAG, "isFork: attacked squares are: $attackedSquares current turn is $player")

        val attackedPieces = attackedSquares.mapNotNull {
            val target = board.getBox(it.row, it.column).chessPiece
            Log.d(TAG, "isFork: target is: $target")
            if (target != null && target.color != piece.color && target.value >= 3) target else null
        }

        // --- undo the move ---
        move.from.chessPiece = originalFromPiece
        move.to.chessPiece = originalToPiece

        return if (attackedPieces.size >= 2) "Fork" else null
    }


    fun isPin(board: Board, move: Move, player: Player): String? {
        Log.d(TAG, "isPin: move is $move")

        val movedPiece = move.pieceMoved ?: return null
        if (movedPiece !is Queen && movedPiece !is Rook && movedPiece !is Bishop) return null

        val opponentColor = if (movedPiece.color == Color.WHITE) Color.BLACK else Color.WHITE
        val kingPos = game.findKing(board, opponentColor)

        // Check if the move aligns the moved piece with the opponent king
        if (!isAligned(move.to.position, kingPos!!.position)) return null
        Log.d(TAG, "isPin: passed this point")

        val betweenSquares = squaresBetween(move.to.position, kingPos.position)
        Log.d(TAG, "isPin: between squares are $betweenSquares")
        val blockingPieces = betweenSquares.mapNotNull { sq ->
            val piece = board.getBox(sq.row, sq.column).chessPiece
            Log.d(TAG, "isPin: piece is $piece")
            if (piece != null && piece.color == opponentColor) piece else null
        }

        return if (blockingPieces.size == 1) "Pin" else null
    }

    fun isDiscoveredCheck(board: Board, move: Move, player: Player): String? {
        Log.d(TAG, "isDiscoveredCheck: move is: $move")
        val tempBoard = board.clone()
        val beforeAttackers = attackersToOpponentKing(tempBoard, player).toSet()
        Log.d(TAG, "isDiscoveredCheck: before attackers are: $beforeAttackers")
        Log.d(TAG, "isDiscoveredCheck: board state is: ${tempBoard.getBox(2,2)} ${tempBoard.getBox(3,1)} ${tempBoard.getBox(0,4)}")

        // --- Simulate the move ---
        val fromSpot = tempBoard.getBox(move.from.position.row, move.from.position.column)
        val toSpot = tempBoard.getBox(move.to.position.row, move.to.position.column)

        val movingPiece = move.pieceMoved ?: return null
        val capturedPiece = toSpot.chessPiece // might be null

        // apply move
        fromSpot.chessPiece = null
        toSpot.chessPiece = movingPiece
        movingPiece.position = move.to.position

        Log.d(TAG, "isDiscoveredCheck: simulated board state = from:$fromSpot to:$toSpot")
        Log.d(TAG, "isDiscoveredCheck: board state is: ${tempBoard.getBox(2,2)} ${tempBoard.getBox(3,1)} ${tempBoard.getBox(0,4)}")

        val afterAttackers = attackersToOpponentKing(tempBoard, player).toSet()
        Log.d(TAG, "isDiscoveredCheck: after attackers are: $afterAttackers")

        // Exclude the moving piece itself — if it attacks king, that’s just check, not discovered check
        val newAttackers = (afterAttackers - beforeAttackers).filter { it != move.pieceMoved }
        Log.d(TAG, "isDiscoveredCheck: newAttackers are: $newAttackers")

        return if (newAttackers.isNotEmpty()) "Discovered Check" else null
    }


    fun isSkewer(board: Board, move: Move): String? {
        if (!game.makeMove(board, move, game.currentTurn.value!!)) return null

        val movedPiece = move.pieceMoved ?: return null
        if (movedPiece.value !in listOf(5, 3, 9)) return null

        val opponentColor = if (movedPiece.color == Color.WHITE) Color.BLACK else Color.WHITE
        val direction = directionBetween(move.from.position, move.to.position) ?: return null

        // From the move's destination, trace forward in the same direction
        var current = move.to.position
        var firstPiece: ChessPiece? = null
        var secondPiece: ChessPiece? = null

        while (true) {
            current = current.next(direction) ?: break
            val piece = board.getBox(current.row, current.column).chessPiece ?: continue

            if (piece.color != opponentColor) break

            if (firstPiece == null) {
                firstPiece = piece
            } else {
                secondPiece = piece
                break
            }
        }

        // Skewer = stronger piece in front, weaker piece behind
        return if (firstPiece != null && secondPiece != null && firstPiece.value > secondPiece.value) {
            "Skewer"
        } else null
    }

    fun isDoubleCheck(board: Board, move: Move): String? {
        val opponentColor = if (move.pieceMoved!!.color == Color.WHITE) Color.BLACK else Color.WHITE
        val kingPos = game.findKing(board, opponentColor)?.position ?: return null
        Log.d(TAG, "isDoubleCheck: king is at position: $kingPos")

        // Step 2: Get all attacking pieces after the move
        val attackers = game.getAlivePieces(Player(move.pieceMoved.color == Color.WHITE, PlayerType.HUMAN), board)
            .filter { piece ->
                val targets = game.getRemainingValidMoves(piece.position, Player(move.pieceMoved.color == Color.WHITE, PlayerType.HUMAN), board)
                kingPos in targets
            }

        // Step 3: Double check = exactly 2 pieces giving check
        return if (attackers.size == 2) "Double Check" else null
    }


    private fun directionBetween(from: Position, to: Position): Pair<Int, Int>? {
        val dRow = to.row - from.row
        val dCol = to.column - from.column

        val stepRow = dRow.sign
        val stepCol = dCol.sign

        return when {
            dRow == 0 -> Pair(0, stepCol)                     // Horizontal
            dCol == 0 -> Pair(stepRow, 0)                     // Vertical
            abs(dRow) == abs(dCol) -> Pair(stepRow, stepCol) // Diagonal
            else -> null                                      // Not aligned
        }
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

    private fun attackersToOpponentKing(board: Board, player: Player): List<ChessPiece> {
        val attackers = mutableListOf<ChessPiece>()
        val opponentColor = if (player.isWhite) Color.BLACK else Color.WHITE
        val kingSpot = game.findKing(board, opponentColor)
        val kingPos = kingSpot?.position
        val playerPieces = game.getAlivePieces(player, board)
        for (piece in playerPieces) {
            val validPositions = game.getRemainingValidMoves(piece.position, player, board)
            if (kingPos in validPositions) {
                attackers.add(piece)
            }
        }
        return attackers
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