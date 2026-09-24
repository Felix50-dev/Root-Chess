package com.example.chess

import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.Position
import com.example.chess.data.repository.FenConverter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

// JVM tests for the FEN mechanics the null-move threat probe relies on: the active color
// is an independent argument (so a position can be emitted with the side to move flipped)
// and the en-passant field follows board.enPassantTarget (which the probe blanks — ep
// rights are void after a "pass").
class FenNullMoveFlipTest {

    @Test
    fun toFen_activeColorIsIndependentOfBoardState() {
        val board = Board()
        val asWhite = FenConverter.toFen(board, Color.WHITE)
        val asBlack = FenConverter.toFen(board, Color.BLACK)
        assertTrue("expected ' w ' in: $asWhite", asWhite.contains(" w "))
        assertTrue("expected ' b ' in: $asBlack", asBlack.contains(" b "))
        // Same placement either way — only the active-color field differs.
        assertEquals(asWhite.substringBefore(" "), asBlack.substringBefore(" "))
    }

    @Test
    fun toFen_startPositionKeepsAllCastlingRights() {
        val fen = FenConverter.toFen(Board(), Color.BLACK)
        assertTrue("expected KQkq in: $fen", fen.contains(" KQkq "))
    }

    @Test
    fun toFen_enPassantFollowsBoardTarget_andBlanksWhenCleared() {
        val board = Board()
        board.enPassantTarget = Position.fromAlgebraicNotation("e3")
        assertTrue(FenConverter.toFen(board, Color.BLACK).contains(" e3 "))

        // The probe's null-move flip clears the target — the FEN must show "-".
        board.enPassantTarget = null
        assertTrue(FenConverter.toFen(board, Color.BLACK).contains(" - "))
    }

    @Test
    fun toFen_roundTripsThroughFromFen() {
        val board = Board()
        val fen = FenConverter.toFen(board, Color.BLACK)
        val (rebuilt, activeColor) = FenConverter.fromFen(fen)
        assertEquals(Color.BLACK, activeColor)
        assertEquals(fen, FenConverter.toFen(rebuilt, activeColor))
    }
}
