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
        hungPiece: ChessPiece?,
        hungSquare: String?,
    ): List<String> {
        val facts = mutableListOf<String>()
        facts += describeMove(move, givesCheck)
        facts += describeMaterial(board)
        if (hungPiece != null && hungSquare != null) {
            facts += "The ${hungPiece.typeName()} on $hungSquare is undefended and can be captured."
        }
        facts += checkEscapeFacts
        return facts
    }

    private fun describeMove(move: Move, givesCheck: Boolean): String {
        val piece = move.pieceMoved
            ?: return "Move played: ${move.from.position.toAlgebraicNotation()} to ${move.to.position.toAlgebraicNotation()}"
        val from = move.from.position.toAlgebraicNotation()
        val to = move.to.position.toAlgebraicNotation()
        val captureText = move.pieceKilled?.let { killed ->
            ", capturing ${killed.color.name.lowercase().replaceFirstChar { c -> c.uppercase() }} ${killed.typeName()} on $to"
        } ?: ""
        val checkText = if (givesCheck) ", giving check" else ""
        val colorWord = piece.color.name.lowercase().replaceFirstChar { it.uppercase() }
        return "$colorWord ${piece.typeName()} moves from $from to $to$captureText$checkText"
    }

    private fun describeMaterial(board: Board): List<String> {
        val whitePieces = mutableListOf<String>()
        val whitePawns = mutableListOf<String>()
        val blackPieces = mutableListOf<String>()
        val blackPawns = mutableListOf<String>()

        for (row in 0 until Board.NUM_ROWS) {
            for (col in 0 until Board.NUM_COLUMNS) {
                val spot = board.getBox(row, col)
                val piece = spot.chessPiece ?: continue
                val square = spot.position.toAlgebraicNotation()
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

        return listOf(
            "White has: ${render(whitePieces, whitePawns)}",
            "Black has: ${render(blackPieces, blackPawns)}",
        )
    }
}

fun ChessPiece.typeName(): String = when (this) {
    is Pawn -> "Pawn"
    is Rook -> "Rook"
    is Knight -> "Knight"
    is Bishop -> "Bishop"
    is Queen -> "Queen"
    is King -> "King"
}
