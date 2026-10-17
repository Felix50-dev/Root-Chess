package com.example.chess.data.repository

import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.Spot

// Converts between this app's Board and standard FEN strings, so a position can be
// persisted (e.g. a puzzle extracted from a past game) and later reconstructed exactly.
// Only piece placement, active color, castling availability, and the en passant target
// are meaningful here — halfmove/fullmove counters aren't used by this engine's legality
// checks, so they're written as fixed placeholders and ignored on read.
object FenConverter {

    fun toFen(board: Board, activeColor: Color): String {
        val boxes = board.boxes
        val placement = (0 until Board.NUM_ROWS).joinToString("/") { row ->
            val rank = StringBuilder()
            var emptyRun = 0
            val rowList = boxes[row]
            for (col in 0 until Board.NUM_COLUMNS) {
                val piece = rowList[col].chessPiece
                if (piece == null) {
                    emptyRun++
                } else {
                    if (emptyRun > 0) {
                        rank.append(emptyRun)
                        emptyRun = 0
                    }
                    rank.append(pieceToChar(piece))
                }
            }
            if (emptyRun > 0) rank.append(emptyRun)
            rank.toString()
        }

        val active = if (activeColor == Color.WHITE) "w" else "b"

        val castling = buildString {
            val whiteKing = findKing(board, Color.WHITE)
            if (whiteKing != null && !whiteKing.hasMoved) {
                if (isUnmovedRook(board, 7, 7, Color.WHITE)) append('K')
                if (isUnmovedRook(board, 7, 0, Color.WHITE)) append('Q')
            }
            val blackKing = findKing(board, Color.BLACK)
            if (blackKing != null && !blackKing.hasMoved) {
                if (isUnmovedRook(board, 0, 7, Color.BLACK)) append('k')
                if (isUnmovedRook(board, 0, 0, Color.BLACK)) append('q')
            }
        }.ifEmpty { "-" }

        val enPassant = board.enPassantTarget?.toAlgebraicNotation() ?: "-"

        return "$placement $active $castling $enPassant 0 1"
    }

    // Returns the reconstructed board and whose turn it is to move — the FEN's active
    // color isn't part of Board's own state, so it can't be folded into the Board alone.
    fun fromFen(fen: String): Pair<Board, Color> {
        val fields = fen.trim().split(" ")
        val placement = fields[0]
        val activeColor = if (fields.getOrElse(1) { "w" } == "w") Color.WHITE else Color.BLACK
        val castling = fields.getOrElse(2) { "-" }
        val enPassant = fields.getOrElse(3) { "-" }

        // Performance optimization: construct empty 8x8 grid directly and pass to Board,
        // avoiding allocating standard starting pieces only to immediately discard/clear them.
        val grid: List<List<Spot>> = List(Board.NUM_ROWS) { row ->
            List(Board.NUM_COLUMNS) { col ->
                Spot(Position(row, col), null)
            }
        }

        val ranks = placement.split("/")
        for (row in ranks.indices) {
            var col = 0
            for (ch in ranks[row]) {
                if (ch.isDigit()) {
                    col += ch.digitToInt()
                } else {
                    val spot = grid[row][col]
                    spot.chessPiece = charToPiece(ch, spot.position)
                    col++
                }
            }
        }

        val board = Board(grid)

        findKing(board, Color.WHITE)?.hasMoved = !(castling.contains('K') || castling.contains('Q'))
        findKing(board, Color.BLACK)?.hasMoved = !(castling.contains('k') || castling.contains('q'))
        setRookHasMoved(board, 7, 7, hasMoved = !castling.contains('K'))
        setRookHasMoved(board, 7, 0, hasMoved = !castling.contains('Q'))
        setRookHasMoved(board, 0, 7, hasMoved = !castling.contains('k'))
        setRookHasMoved(board, 0, 0, hasMoved = !castling.contains('q'))

        board.enPassantTarget =
            if (enPassant == "-") null else Position.fromAlgebraicNotation(enPassant)

        return board to activeColor
    }

    private fun pieceToChar(piece: ChessPiece): Char {
        val base = when (piece) {
            is Pawn -> 'p'
            is Knight -> 'n'
            is Bishop -> 'b'
            is Rook -> 'r'
            is Queen -> 'q'
            is King -> 'k'
        }
        return if (piece.color == Color.WHITE) base.uppercaseChar() else base
    }

    private fun charToPiece(ch: Char, position: Position): ChessPiece {
        val color = if (ch.isUpperCase()) Color.WHITE else Color.BLACK
        return when (ch.lowercaseChar()) {
            'p' -> Pawn(color = color, position = position, isKilled = false)
            'n' -> Knight(color = color, position = position, isKilled = false)
            'b' -> Bishop(color = color, position = position, isKilled = false)
            'r' -> Rook(color = color, position = position, isKilled = false)
            'q' -> Queen(color = color, position = position, isKilled = false)
            'k' -> King(color = color, position = position, isKilled = false)
            else -> error("Unknown FEN piece character: $ch")
        }
    }

    private fun findKing(board: Board, color: Color): King? {
        val boxes = board.boxes
        for (row in 0 until Board.NUM_ROWS) {
            val rowList = boxes[row]
            for (col in 0 until Board.NUM_COLUMNS) {
                val piece = rowList[col].chessPiece
                if (piece is King && piece.color == color) return piece
            }
        }
        return null
    }

    private fun isUnmovedRook(board: Board, row: Int, col: Int, color: Color): Boolean {
        val piece = board.boxes[row][col].chessPiece
        return piece is Rook && piece.color == color && !piece.hasMoved
    }

    private fun setRookHasMoved(board: Board, row: Int, col: Int, hasMoved: Boolean) {
        val piece = board.boxes[row][col].chessPiece
        if (piece is Rook) piece.hasMoved = hasMoved
    }
}
