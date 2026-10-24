package com.example.chess.data.repository

import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Move
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook

// Deterministic, ground-truth facts about a single move and the position it lands in, meant
// to be handed to an LLM verbatim so it never has to infer board geometry itself — every
// square/piece named here is read directly off the real Board, never guessed. Replaces an
// earlier approach of handing the LLM a raw FEN string, which empirically still hallucinated
// piece identity (a reasoning model treating FEN as "a jumble of characters" to interpret).
object ExplanationFacts {

    fun build(
        board: Board,
        move: Move,
        givesCheck: Boolean,
        checkEscapeFacts: List<String>,
        movedPieceFacts: List<String> = emptyList(),
        hungPieceFact: String? = null,
        bestMoveDescription: String? = null,
        // Emitted by the caller only when relevant (rook/king moved, or connectivity
        // changed) — an every-move "NOT connected" line was pure prompt noise.
        rookConnectionFact: String? = null,
        isCheckmate: Boolean = false,
    ): List<String> {
        val facts = mutableListOf<String>()
        facts += describeMove(move, givesCheck)
        if (isCheckmate) {
            val winner = move.pieceMoved?.color?.let { colorWord(it) } ?: "The mover"
            facts += "This move delivers checkmate — $winner wins and the game is over."
        }
        facts += describeMaterial(board)
        rookConnectionFact?.let { facts += it }
        facts += movedPieceFacts
        hungPieceFact?.let { facts += it }
        facts += checkEscapeFacts
        bestMoveDescription?.let { facts += it }
        return facts
    }

    private fun describeMove(move: Move, givesCheck: Boolean): String {
        val piece = move.pieceMoved
            ?: return "Move played: ${move.from.position.toAlgebraicNotation()} to ${move.to.position.toAlgebraicNotation()}"
        val from = move.from.position.toAlgebraicNotation()
        val to = move.to.position.toAlgebraicNotation()
        val captureText = move.pieceKilled?.let { killed ->
            // En passant: the captured pawn sits beside the mover (to-file, from-rank),
            // NOT on the (empty) landing square.
            val capturedOn = if (move.isEnPassant) "${to[0]}${from[1]}" else to
            val epText = if (move.isEnPassant) " en passant" else ""
            ", capturing ${killed.color.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${killed.typeName()} on $capturedOn$epText"
        } ?: ""
        val promotionText = move.promotionPiece?.let { ", promoting to a ${it.typeName()}" } ?: ""
        val checkText = if (givesCheck) ", giving check" else ""
        val colorWord = piece.color.name.lowercase().replaceFirstChar { it.uppercase() }
        return "Move played: $colorWord ${piece.typeName()} moves from $from to $to$captureText$promotionText$checkText"
    }

    private fun describeMaterial(board: Board): List<String> {
        val whitePieces = mutableListOf<String>()
        val whitePawns = mutableListOf<String>()
        val blackPieces = mutableListOf<String>()
        val blackPawns = mutableListOf<String>()
        var whitePoints = 0
        var blackPoints = 0

        // Performance Optimization: Iterate directly over board.boxes (List<List<Spot>>) to eliminate
        // redundant board.getBox(row, col) calls, row/col boundary checks, and indexing overhead.
        for (row in board.boxes) {
            for (spot in row) {
                val piece = spot.chessPiece ?: continue
                val square = spot.position.toAlgebraicNotation()
                if (piece.color == Color.WHITE) whitePoints += piece.pointValue() else blackPoints += piece.pointValue()
                if (piece is Pawn) {
                    (if (piece.color == Color.WHITE) whitePawns else blackPawns) += square
                } else {
                    val label = "${piece.typeName()} $square"
                    (if (piece.color == Color.WHITE) whitePieces else blackPieces) += label
                }
            }
        }

        fun render(pieces: List<String>, pawns: List<String>): String {
            val parts = pieces.toMutableList()
            if (pawns.isNotEmpty()) parts += "pawns ${pawns.joinToString(", ")}"
            return parts.joinToString(", ")
        }

        val balance = when {
            whitePoints == blackPoints -> "Material balance: even."
            whitePoints > blackPoints -> "Material balance: White is ahead by ${points(whitePoints - blackPoints)}."
            else -> "Material balance: Black is ahead by ${points(blackPoints - whitePoints)}."
        }
        return listOf(
            "White has: ${render(whitePieces, whitePawns)}",
            "Black has: ${render(blackPieces, blackPawns)}",
            balance,
        )
    }

    private fun points(n: Int) = if (n == 1) "1 point" else "$n points"

    // The LLM must never derive rook connectivity itself (it empirically claims "connected
    // rooks" with pieces still between them) — state it outright. Only the unambiguous
    // exactly-two-rooks case is described; 0/1/3+ rooks emit nothing.
    internal fun describeRookConnection(board: Board, color: Color): String? {
        val rooks = mutableListOf<ChessPiece>()
        // Performance Optimization: Iterate directly over board.boxes to avoid getBox overhead.
        for (row in board.boxes) {
            for (spot in row) {
                val piece = spot.chessPiece ?: continue
                if (piece is Rook && piece.color == color) rooks += piece
            }
        }
        if (rooks.size != 2) return null

        val (a, b) = rooks.sortedWith(compareBy({ it.position.column }, { it.position.row }))
        val whose = "${colorWord(color)}'s rooks on " +
            "${a.position.toAlgebraicNotation()} and ${b.position.toAlgebraicNotation()}"
        val blockers: List<ChessPiece>? = when {
            a.position.row == b.position.row ->
                (minOf(a.position.column, b.position.column) + 1 until maxOf(a.position.column, b.position.column))
                    .mapNotNull { board.getBox(a.position.row, it).chessPiece }
            a.position.column == b.position.column ->
                (minOf(a.position.row, b.position.row) + 1 until maxOf(a.position.row, b.position.row))
                    .mapNotNull { board.getBox(it, a.position.column).chessPiece }
            else -> null
        }
        return when {
            blockers == null -> "$whose are not connected (not on the same rank or file)."
            blockers.isEmpty() -> "$whose are connected (all squares between them are empty)."
            else -> {
                val names = blockers.joinToString(", ") {
                    "the ${colorWord(it.color)} ${it.typeName()} on ${it.position.toAlgebraicNotation()}"
                }
                val verb = if (blockers.size == 1) "stands" else "stand"
                "$whose are NOT connected — $names $verb between them."
            }
        }
    }

    private fun colorWord(color: Color) = color.name.lowercase().replaceFirstChar { it.uppercase() }
}

fun ChessPiece.typeName(): String = when (this) {
    is Pawn -> "Pawn"
    is Rook -> "Rook"
    is Knight -> "Knight"
    is Bishop -> "Bishop"
    is Queen -> "Queen"
    is King -> "King"
}

// Classic point values, used for the material-balance fact and the deterministic
// exchange assessments ("capturing with the Queen would lose the Queen for the Knight").
internal fun ChessPiece.pointValue(): Int = when (this) {
    is Pawn -> 1
    is Knight, is Bishop -> 3
    is Rook -> 5
    is Queen -> 9
    is King -> 0
}
