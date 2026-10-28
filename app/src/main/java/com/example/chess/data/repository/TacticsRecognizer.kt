package com.example.chess.data.repository

import android.util.Log
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Move
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.domain.model.DetectedTactic
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

    private fun colorWord(color: Color) = color.name.lowercase().replaceFirstChar { it.uppercase() }

    // Same vocabulary as GamePlay.pieceLabel, so tactic descriptions and the other
    // Facts speak one dialect in the LLM prompt. `pos` is passed explicitly instead of
    // reading piece.position because position fields on pieces found via getBox scans
    // can be stale — always use the scan coordinates.
    private fun label(piece: ChessPiece, pos: Position) =
        "${colorWord(piece.color)} ${piece.typeName()} on ${pos.toAlgebraicNotation()}"

    private fun slidingDirections(piece: ChessPiece): List<Pair<Int, Int>>? = when (piece) {
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
        else -> null
    }

    private data class RayPair(
        val first: ChessPiece,
        val firstPos: Position,
        val second: ChessPiece,
        val secondPos: Position,
    )

    // Walks from `origin` (exclusive) in direction (dr, dc) and returns the first two
    // enemy pieces on the ray, or null if an own piece blocks it first or fewer than
    // two enemy pieces are found. Walking square-by-square doubles as path-clearance
    // verification, so callers need no separate "can the attacker reach" check.
    // Read-only: only getBox, no getAlivePieces position-sync side effects.
    private fun findRayPair(board: Board, origin: Position, dr: Int, dc: Int, myColor: Color): RayPair? {
        var r = origin.row + dr
        var c = origin.column + dc
        var first: ChessPiece? = null
        var firstPos: Position? = null

        while (r in 0..7 && c in 0..7) {
            val sq = board.getBox(r, c).chessPiece
            if (sq != null) {
                if (sq.color == myColor) return null  // own piece blocks the ray
                if (first == null) {
                    first = sq
                    firstPos = Position(r, c)
                } else {
                    return RayPair(first, firstPos!!, sq, Position(r, c))
                }
            }
            r += dr
            c += dc
        }
        return null
    }

    // Attacking `target` is only a real threat when something can come of it: it's the
    // king (check), it's worth more than the attacker (capturing profits even if
    // defended), or it can be captured at a net profit. One capture deep — not a full
    // static exchange evaluation, but enough to stop labelling attacks on well-defended
    // equal-or-lesser pieces as tactics.
    private fun isRealThreat(board: Board, target: ChessPiece, pos: Position, attackerValue: Int) =
        target is King || target.value > attackerValue || isPieceHangingForFree(board, target, pos)

    fun isFork(board: Board, move: Move, player: Player): DetectedTactic? {
        val piece = move.pieceMoved ?: return null
        if (piece is King) return null  // kings don't fork

        // `player` is the source of truth for whose perspective this is, rather than
        // re-deriving color from `piece` — matters when the caller passes a `player`
        // that intentionally differs (e.g. evaluating a hypothetical opponent reply).
        val myColor = if (player.isWhite) Color.WHITE else Color.BLACK

        // Simulate on tempBoard only
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = piece

        // Get all pieces this piece attacks from its new position that are actually
        // threatened by the attack — attacking a well-defended equal-or-lesser piece
        // isn't a fork prong, the "forked" side just ignores it.
        val targets = mutableListOf<Pair<ChessPiece, Position>>()
        val fromSpot = tempBoard.getBox(move.to.position.row, move.to.position.column)

        for (row in 0..7) {
            for (col in 0..7) {
                val targetSquare = tempBoard.getBox(row, col)
                val target = targetSquare.chessPiece ?: continue
                if (target.color == myColor) continue  // skip own pieces
                if (target.value < MIN_FORK_TARGET_VALUE) continue
                if (!piece.canMove(tempBoard, fromSpot, targetSquare)) continue
                if (!isRealThreat(tempBoard, target, Position(row, col), piece.value)) continue
                targets.add(target to Position(row, col))
            }
        }

        Log.d(TAG, "isFork: qualifying targets are ${targets.map { it.second }}")

        if (targets.size < 2) return null

        // A fork that just hangs the forking piece for free isn't a real threat — the
        // opponent simply takes it instead of losing one of the forked pieces.
        if (isPieceHangingForFree(tempBoard, piece, move.to.position)) return null

        // The double attack must be created by THIS move: a slider gliding along a line
        // while keeping the same targets under attack would otherwise re-report its old
        // fork every ply. At least one prong has to be new from the destination square.
        val originSpot = board.getBox(move.from.position.row, move.from.position.column)
        val createsNewThreat = targets.any { (_, pos) ->
            !piece.canMove(board, originSpot, board.getBox(pos.row, pos.column))
        }
        if (!createsNewThreat) return null

        return DetectedTactic(
            name = "Fork",
            description = "The ${label(piece, move.to.position)} forks the " +
                targets.joinToString(" and ") { (target, pos) -> label(target, pos) } + "."
        )
    }

    fun isPin(board: Board, move: Move, player: Player): DetectedTactic? {
        val movedPiece = move.pieceMoved ?: return null
        // Only sliding pieces pin; slidingDirections doubles as the piece-type gate.
        val directions = slidingDirections(movedPiece) ?: return null

        // `player` is the source of truth for whose perspective this is, rather than
        // re-deriving color from `movedPiece` — see isFork for the same reasoning.
        val myColor = if (player.isWhite) Color.WHITE else Color.BLACK

        // Simulate on tempBoard
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = movedPiece

        // A pin where the pinning piece itself is just hanging for free isn't a
        // lasting threat — the opponent captures it instead of living with the pin.
        // Property of the destination square, not of any one direction, so checked once.
        if (isPieceHangingForFree(tempBoard, movedPiece, move.to.position)) return null

        // Pins this piece was ALREADY delivering from its origin square, on the real
        // pre-move board. A slider gliding along the pinning ray keeps the same pin —
        // that's not a tactic this move created, so it must not be re-reported.
        // Identity by id: Board.clone() preserves piece ids.
        val preExistingPins = directions.mapNotNull { (dr, dc) ->
            findRayPair(board, move.from.position, dr, dc, myColor)
        }.filter { qualifiesAsPin(it) }
            .map { it.first.id to it.second.id }
            .toSet()

        for ((dr, dc) in directions) {
            val pin = findRayPair(tempBoard, move.to.position, dr, dc, myColor) ?: continue
            if (!qualifiesAsPin(pin)) continue

            // A pawn "pinned" along a file isn't pinned at all — its pushes stay on the
            // ray, so nothing is restrained and the shielded piece is never exposed.
            if (pin.first is Pawn && dc == 0) continue

            // A defended pawn pinned on a rank or diagonal isn't a reportable tactic
            // either: the pinner can never profitably take it, and immobilizing a pawn
            // wins nothing. Pieces (value >= 3) stay reportable even when defended —
            // a knight pinned to its king is genuinely out of the game.
            if (pin.first is Pawn && !isPieceHangingForFree(tempBoard, pin.first, pin.firstPos)) continue

            // continue, not return: a genuinely new pin in another direction still counts.
            if ((pin.first.id to pin.second.id) in preExistingPins) continue

            return DetectedTactic(
                name = "Pin",
                description = "The ${label(movedPiece, move.to.position)} pins the " +
                    "${label(pin.first, pin.firstPos)} against the ${label(pin.second, pin.secondPos)}."
            )
        }

        return null
    }

    // A pin needs an ordinary piece in front — never the king itself, since a king
    // being attacked is a check, not a pin — with either the enemy king or a strictly
    // more valuable piece directly behind it (absolute pin vs. relative pin; a pin
    // against an equal-or-lesser-value piece isn't a real tactic, matching isSkewer's
    // strictly-greater comparison).
    private fun qualifiesAsPin(pair: RayPair): Boolean {
        if (pair.first is King) return false
        return pair.second is King || pair.second.value > pair.first.value
    }

    fun isDiscoveredCheck(board: Board, move: Move, player: Player): DetectedTactic? {
        val piece = move.pieceMoved ?: return null

        // Simulate on tempBoard. (The simulation ignores castling's rook hop and the
        // en-passant victim square — pre-existing limitation shared by all detectors.)
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

        if (newAttackers.isEmpty()) return null

        val opponentColor = if (player.isWhite) Color.BLACK else Color.WHITE
        val kingPos = game.findKing(tempBoard, opponentColor)?.position ?: return null
        // newAttackers come via getAlivePieces, which syncs positions — safe to read.
        val attacker = newAttackers.first()

        return DetectedTactic(
            name = "Discovered Check",
            description = "Moving the ${label(piece, move.to.position)} revealed a discovered check: " +
                "the ${label(attacker, attacker.position)} now checks the " +
                "${colorWord(opponentColor)} King on ${kingPos.toAlgebraicNotation()}."
        )
    }

    private fun attackersToOpponentKing(board: Board, player: Player): List<ChessPiece> {
        val opponentColor = if (player.isWhite) Color.BLACK else Color.WHITE
        val kingSpot = game.findKing(board, opponentColor) ?: return emptyList()
        return attackersOf(board, player, kingSpot.position)
    }

    // All of `player`'s pieces that can (pseudo-legally) reach `target` on `board`.
    private fun attackersOf(board: Board, player: Player, target: Position): List<ChessPiece> {
        val targetSpot = board.getBox(target.row, target.column)
        return game.getAlivePieces(player, board).filter { piece ->
            val fromSpot = board.getBox(piece.position.row, piece.position.column)
            piece.canMove(board, fromSpot, targetSpot)
        }
    }

    // Discovered Check's sibling: moving a piece reveals a DIFFERENT one of the
    // mover's pieces now attacking an enemy piece it didn't attack before — same
    // detection shape as isDiscoveredCheck, generalized from "attacks the king" to
    // "attacks any sufficiently valuable non-king enemy piece".
    fun isDiscoveredAttack(board: Board, move: Move, player: Player): DetectedTactic? {
        val piece = move.pieceMoved ?: return null

        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = piece

        // True pre-move state — see isDiscoveredCheck for why the moving piece must
        // stay on its origin square here rather than being removed.
        val preMoveBoard = board.clone()

        val opponentColor = if (player.isWhite) Color.BLACK else Color.WHITE

        for (row in 0..7) {
            for (col in 0..7) {
                val targetAfter = tempBoard.getBox(row, col).chessPiece ?: continue
                if (targetAfter.color != opponentColor) continue
                if (targetAfter is King) continue  // that's isDiscoveredCheck's job
                if (targetAfter.value < MIN_FORK_TARGET_VALUE) continue

                val targetPosition = Position(row, col)
                val beforeAttackerIds = attackersOf(preMoveBoard, player, targetPosition)
                    .map { it.id }
                    .toSet()
                val afterAttackers = attackersOf(tempBoard, player, targetPosition)

                val newAttacker = afterAttackers.firstOrNull { attacker ->
                    attacker.id !in beforeAttackerIds && attacker.id != piece.id
                } ?: continue

                // The revealed attack must actually threaten something: attacking a
                // well-defended equal-or-lesser piece is a non-event, and a revealed
                // attacker that itself hangs for free just gets captured.
                if (!isRealThreat(tempBoard, targetAfter, targetPosition, newAttacker.value)) continue
                if (isPieceHangingForFree(tempBoard, newAttacker, newAttacker.position)) continue

                return DetectedTactic(
                    name = "Discovered Attack",
                    description = "Moving the ${label(piece, move.to.position)} uncovered a discovered attack: " +
                        "the ${label(newAttacker, newAttacker.position)} now attacks the " +
                        "${label(targetAfter, targetPosition)}."
                )
            }
        }

        return null
    }

    fun isSkewer(board: Board, move: Move): DetectedTactic? {
        val piece = move.pieceMoved ?: return null
        // Only sliding pieces skewer; slidingDirections doubles as the piece-type gate.
        val directions = slidingDirections(piece) ?: return null
        val myColor = piece.color

        // Simulate entirely on tempBoard
        val tempBoard = board.clone()
        tempBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        tempBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = piece

        // A skewer that just hangs the skewering piece for free isn't a real threat —
        // the opponent simply takes it instead of losing the piece behind. This is a
        // property of the piece's destination square, not of any one direction, so it
        // only needs to be checked once.
        if (isPieceHangingForFree(tempBoard, piece, move.to.position)) return null

        // Skewers this piece was already delivering from its origin square — a slider
        // gliding along the same ray must not re-report its old skewer (see isPin).
        val preExistingSkewers = directions.mapNotNull { (dr, dc) ->
            findRayPair(board, move.from.position, dr, dc, myColor)
        }.filter { it.first.value > it.second.value }
            .map { it.first.id to it.second.id }
            .toSet()

        for ((dr, dc) in directions) {
            val pair = findRayPair(tempBoard, move.to.position, dr, dc, myColor) ?: continue
            if (pair.first.value <= pair.second.value) continue

            // The piece BEHIND is what the skewer actually wins — if capturing it once
            // the front piece moves gains nothing (defended, equal-or-lesser value),
            // the "skewer" threatens nothing. The front piece is removed first: it's
            // about to be forced off the ray, and while it stands it blocks the very
            // attack being evaluated. Approximation: where the front piece lands could
            // still change the back piece's defenses.
            val boardWithoutFront = tempBoard.clone()
            boardWithoutFront.getBox(pair.firstPos.row, pair.firstPos.column).chessPiece = null
            if (!isRealThreat(boardWithoutFront, pair.second, pair.secondPos, piece.value)) continue

            if ((pair.first.id to pair.second.id) in preExistingSkewers) continue

            return DetectedTactic(
                name = "Skewer",
                description = "The ${label(piece, move.to.position)} skewers the " +
                    "${label(pair.first, pair.firstPos)} against the ${label(pair.second, pair.secondPos)}."
            )
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
                // Optimization: prune canMove check if attacker's value cannot lower cheapestAttacker
                val currentCheapest = cheapestAttacker ?: Int.MAX_VALUE
                if (attacker.value >= currentCheapest) continue

                if (attacker.canMove(board, board.getBox(row, col), targetSquare)) {
                    cheapestAttacker = attacker.value
                }
            }
        }
        val attackerValue = cheapestAttacker ?: return false  // not attacked at all

        // Optimization: if cheapest attacker value is strictly less than piece value,
        // capturing is guaranteed net-profitable regardless of defenders. Skip board cloning and defender search.
        if (attackerValue < piece.value) return true

        // canMove() refuses to "capture" a square occupied by a friendly piece, so
        // checking defenders directly against `board` would always find none (every
        // defender would see its own side's piece still sitting there). Simulate an
        // enemy piece having just captured on that square instead, so canMove() can
        // answer the real geometric question — including for pawns, whose diagonal
        // capture move specifically requires an enemy piece present on the target.
        val afterCapture = board.clone()
        afterCapture.getBox(position.row, position.column).chessPiece =
            Pawn(color = opponentColor, position = position, isKilled = false)

        val targetSpotInAfterCapture = afterCapture.getBox(position.row, position.column)
        val isDefended = (0..7).any { row ->
            (0..7).any { col ->
                val defenderSpot = afterCapture.getBox(row, col)
                val defender = defenderSpot.chessPiece
                defender != null && defender.color == piece.color &&
                    defender.canMove(
                        afterCapture,
                        defenderSpot,
                        targetSpotInAfterCapture
                    )
            }
        }

        return !isDefended
    }

    fun isDoubleCheck(board: Board, move: Move, player: Player): DetectedTactic? {
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
        if (attackers.size < 2) return null

        // Positions synced by getAlivePieces above — safe to read.
        return DetectedTactic(
            name = "Double Check",
            description = "Double check: the ${label(attackers[0], attackers[0].position)} and the " +
                "${label(attackers[1], attackers[1].position)} both check the " +
                "${colorWord(opponentColor)} King on ${kingPos.toAlgebraicNotation()}."
        )
    }


    fun Position.next(direction: Pair<Int, Int>): Position? {
        val newRow = row + direction.first
        val newCol = column + direction.second
        return if (newRow in 0..7 && newCol in 0..7) Position(newRow, newCol) else null
    }

}
