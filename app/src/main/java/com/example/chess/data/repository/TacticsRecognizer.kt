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

        if (attackedPieces.size < 2) return null

        // A fork that just hangs the forking piece for free isn't a real threat — the
        // opponent simply takes it instead of losing one of the forked pieces.
        if (isPieceHangingForFree(tempBoard, piece, move.to.position)) return null

        return "Fork"
    }


    fun isPin(board: Board, move: Move, player: Player): String? {
        val movedPiece = move.pieceMoved ?: return null
        if (movedPiece !is Queen && movedPiece !is Rook && movedPiece !is Bishop) return null

        // Simulate on tempBoard
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = movedPiece

        // Only check directions valid for the piece type — same approach as
        // isSkewer. Walking square-by-square also doubles as path-clearance
        // verification, so there's no separate "can the attacker actually reach
        // the back piece" check needed afterward.
        val directions = when (movedPiece) {
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

        // A pin where the pinning piece itself is just hanging for free isn't a
        // lasting threat — the opponent captures it instead of living with the pin.
        // Same property-of-the-destination-square reasoning as isSkewer, checked once.
        val isHanging = isPieceHangingForFree(tempBoard, movedPiece, move.to.position)

        for ((dr, dc) in directions) {
            var r = move.to.position.row + dr
            var c = move.to.position.column + dc

            var pinnedPiece: ChessPiece? = null
            var backPiece: ChessPiece? = null

            while (r in 0..7 && c in 0..7) {
                val sq = tempBoard.getBox(r, c).chessPiece
                if (sq != null) {
                    if (sq.color == movedPiece.color) break  // own piece blocks the ray

                    if (pinnedPiece == null) {
                        pinnedPiece = sq
                    } else {
                        backPiece = sq
                        break
                    }
                }
                r += dr
                c += dc
            }

            // A pin needs an ordinary piece in front — never the king itself, since a
            // king being attacked is a check, not a pin — with either the enemy king
            // or a strictly more valuable piece directly behind it (absolute pin vs.
            // relative pin; a pin against an equal-or-lesser-value piece isn't a real
            // tactic, matching isSkewer's strictly-greater comparison).
            if (pinnedPiece != null && pinnedPiece !is King && backPiece != null && !isHanging) {
                val isKingBehind = backPiece is King
                val isMoreValuable = pieceValue(backPiece) > pieceValue(pinnedPiece)
                if (isKingBehind || isMoreValuable) {
                    return "Pin"
                }
            }
        }

        return null
    }

    fun isDiscoveredCheck(board: Board, move: Move, player: Player): String? {
        val piece = move.pieceMoved ?: return null

        // Simulate on tempBoard
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = piece

        // The true pre-move state is just the board as it was — the moving piece is
        // still on its origin square, still blocking whatever it blocks there. (This
        // used to also null out the origin square here, which self-defeated the
        // comparison: it revealed the very same discovered attack we're trying to
        // detect in the "before" snapshot too, so a genuine discovered check could
        // never be told apart from a piece that was already attacking beforehand.)
        // Cloned (rather than using `board` directly) so getAlivePieces' position-sync
        // side effect never touches the caller's real board.
        val preMoveBoard = board.clone()
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

        // A skewer that just hangs the skewering piece for free isn't a real threat —
        // the opponent simply takes it instead of losing the piece behind. This is a
        // property of the piece's destination square, not of any one direction, so it
        // only needs to be checked once.
        val isHanging = isPieceHangingForFree(tempBoard, piece, move.to.position)

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
                pieceValue(firstPiece) > pieceValue(secondPiece) && !isHanging
            ) {
                return "Skewer"
            }
        }

        return null
        // No undo needed — tempBoard is a clone
    }

    // Returns true if `piece` (at `position` on `board`) would be captured for free by
    // the opponent: attacked and either undefended, or only defended by something that
    // would still leave the opponent net ahead in value.
    private fun isPieceHangingForFree(board: Board, piece: ChessPiece, position: Position): Boolean {
        val opponentColor = if (piece.color == Color.WHITE) Color.BLACK else Color.WHITE
        val targetSquare = board.getBox(position.row, position.column)

        var cheapestAttacker: Int? = null
        for (row in 0..7) {
            for (col in 0..7) {
                val attacker = board.getBox(row, col).chessPiece ?: continue
                if (attacker.color != opponentColor) continue
                if (attacker.canMove(board, board.getBox(row, col), targetSquare)) {
                    cheapestAttacker = minOf(cheapestAttacker ?: Int.MAX_VALUE, attacker.value)
                }
            }
        }
        val attackerValue = cheapestAttacker ?: return false  // not attacked at all

        // canMove() refuses to "capture" a square occupied by a friendly piece, so
        // checking defenders directly against `board` would always find none (every
        // defender would see its own side's piece still sitting there). Simulate an
        // enemy piece having just captured on that square instead, so canMove() can
        // answer the real geometric question — including for pawns, whose diagonal
        // capture move specifically requires an enemy piece present on the target.
        val afterCapture = board.clone()
        afterCapture.getBox(position.row, position.column).chessPiece =
            Pawn(color = opponentColor, position = position, isKilled = false)

        val isDefended = (0..7).any { row ->
            (0..7).any { col ->
                val defender = afterCapture.getBox(row, col).chessPiece
                defender != null && defender.color == piece.color &&
                    defender.canMove(
                        afterCapture,
                        afterCapture.getBox(row, col),
                        afterCapture.getBox(position.row, position.column)
                    )
            }
        }

        return !isDefended || attackerValue < piece.value
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


    fun Position.next(direction: Pair<Int, Int>): Position? {
        val newRow = row + direction.first
        val newCol = column + direction.second
        return if (newRow in 0..7 && newCol in 0..7) Position(newRow, newCol) else null
    }

}
