package com.example.chess

import com.example.chess.data.model.Board
import com.example.chess.data.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun isValidPosition_validCoordinates_returnsTrue() {
        val board = Board()

        // Corners
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(0, 7)))
        assertTrue(board.isValidPosition(Position(7, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))

        // Center squares
        assertTrue(board.isValidPosition(Position(3, 3)))
        assertTrue(board.isValidPosition(Position(4, 4)))
    }

    @Test
    fun isValidPosition_outOfBoundsCoordinates_returnsFalse() {
        val board = Board()

        // Negative coordinates
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(-1, -1)))

        // Greater than or equal to 8
        assertFalse(board.isValidPosition(Position(8, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(8, 8)))
        assertFalse(board.isValidPosition(Position(100, 100)))
    }

    @Test
    fun positionFromAlgebraicNotation_parsesCorrectly() {
        // e2 -> col: 4 ('e' - 'a'), row: 6 (8 - 2)
        val e2 = Position.fromAlgebraicNotation("e2")
        assertEquals(6, e2.row)
        assertEquals(4, e2.column)
        assertEquals("e2", e2.toAlgebraicNotation())

        // a1 -> col: 0 ('a' - 'a'), row: 7 (8 - 1)
        val a1 = Position.fromAlgebraicNotation("a1")
        assertEquals(7, a1.row)
        assertEquals(0, a1.column)
        assertEquals("a1", a1.toAlgebraicNotation())

        // h8 -> col: 7 ('h' - 'a'), row: 0 (8 - 8)
        val h8 = Position.fromAlgebraicNotation("h8")
        assertEquals(0, h8.row)
        assertEquals(7, h8.column)
        assertEquals("h8", h8.toAlgebraicNotation())

        // e4 -> col: 4, row: 4
        val e4 = Position.fromAlgebraicNotation("e4")
        assertEquals(4, e4.row)
        assertEquals(4, e4.column)
        assertEquals("e4", e4.toAlgebraicNotation())
    }
}
