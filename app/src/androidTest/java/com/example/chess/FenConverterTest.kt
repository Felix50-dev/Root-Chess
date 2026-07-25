package com.example.chess

import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Position
import com.example.chess.data.model.Rook
import com.example.chess.data.repository.FenConverter
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class FenConverterTest {

    private fun clearBoard(board: Board) {
        for (row in 0..7) for (col in 0..7) board.getBox(row, col).chessPiece = null
    }

    @Test
    fun toFen_startingPosition_matchesStandardFen() {
        val board = Board() // constructor already sets up the standard starting position
        val fen = FenConverter.toFen(board, Color.WHITE)

        assertEquals(
            "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
            fen
        )
    }

    @Test
    fun roundTrip_startingPosition_preservesPiecePlacementAndActiveColor() {
        val original = Board()
        val fen = FenConverter.toFen(original, Color.BLACK)

        val (rebuilt, activeColor) = FenConverter.fromFen(fen)

        assertEquals(Color.BLACK, activeColor)
        for (row in 0..7) {
            for (col in 0..7) {
                val originalPiece = original.getBox(row, col).chessPiece
                val rebuiltPiece = rebuilt.getBox(row, col).chessPiece
                if (originalPiece == null) {
                    assertNull("Expected empty at ($row,$col)", rebuiltPiece)
                } else {
                    assertEquals("Piece type mismatch at ($row,$col)",
                        originalPiece::class, rebuiltPiece?.let { it::class
                        })
                    assertEquals("Piece color mismatch at ($row,$col)",
                        originalPiece.color, rebuiltPiece?.color)
                }
            }
        }
    }

    @Test
    fun roundTrip_kingAndRookHaveMoved_castlingRightsLost() {
        val board = Board()
        clearBoard(board)
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        king.hasMoved = true
        board.getBox(7, 4).chessPiece = king
        val rook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        board.getBox(7, 0).chessPiece = rook
        board.getBox(0, 4).chessPiece = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)

        val fen = FenConverter.toFen(board, Color.WHITE)
        assertTrue("A king that has moved must not grant any castling rights",
            !fen.split(" ")[2].let { it.contains('K') || it.contains('Q') })

        val (rebuilt, _) = FenConverter.fromFen(fen)
        val rebuiltKing = rebuilt.getBox(7, 4).chessPiece as King
        assertTrue("Rebuilt king must be marked as having moved", rebuiltKing.hasMoved)
    }

    @Test
    fun roundTrip_rookHasMoved_thatSideCastlingRightLost_otherSideKept() {
        val board = Board()
        clearBoard(board)
        board.getBox(7, 4).chessPiece = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val kingsideRook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        kingsideRook.hasMoved = true
        board.getBox(7, 7).chessPiece = kingsideRook
        board.getBox(7, 0).chessPiece = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        board.getBox(0, 4).chessPiece = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)

        val fen = FenConverter.toFen(board, Color.WHITE)
        val castling = fen.split(" ")[2]
        assertFalse("Kingside right should be gone", castling.contains('K'))
        assertTrue("Queenside right should remain", castling.contains('Q'))

        val (rebuilt, _) = FenConverter.fromFen(fen)
        assertTrue((rebuilt.getBox(7, 7).chessPiece as Rook).hasMoved)
        assertFalse((rebuilt.getBox(7, 0).chessPiece as Rook).hasMoved)
    }

    @Test
    fun roundTrip_enPassantTargetSet_preserved() {
        val board = Board()
        board.enPassantTarget = Position(2, 4) // e6, as if e7-e5 was just played

        val fen = FenConverter.toFen(board, Color.WHITE)
        assertEquals("e6", fen.split(" ")[3])

        val (rebuilt, _) = FenConverter.fromFen(fen)
        assertEquals(Position(2, 4), rebuilt.enPassantTarget)
    }

    @Test
    fun roundTrip_noEnPassantTarget_writesDash() {
        val board = Board()
        board.enPassantTarget = null

        val fen = FenConverter.toFen(board, Color.WHITE)
        assertEquals("-", fen.split(" ")[3])

        val (rebuilt, _) = FenConverter.fromFen(fen)
        assertNull(rebuilt.enPassantTarget)
    }

    @Test
    fun roundTrip_sparsePosition_emptyRunsCountedCorrectly() {
        val board = Board()
        clearBoard(board)
        board.getBox(0, 0).chessPiece = King(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        board.getBox(7, 7).chessPiece = King(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        board.getBox(3, 3).chessPiece = Pawn(color = Color.WHITE, position = Position(3, 3), isKilled = false)

        val fen = FenConverter.toFen(board, Color.WHITE)
        val (rebuilt, _) = FenConverter.fromFen(fen)

        assertTrue(rebuilt.getBox(0, 0).chessPiece is King)
        assertTrue(rebuilt.getBox(7, 7).chessPiece is King)
        assertTrue(rebuilt.getBox(3, 3).chessPiece is Pawn)
        // Every other square must be empty
        for (row in 0..7) for (col in 0..7) {
            if (Position(row, col) !in setOf(Position(0, 0), Position(7, 7), Position(3, 3))) {
                assertNull("Expected empty at ($row,$col)", rebuilt.getBox(row, col).chessPiece)
            }
        }
    }
}
