package com.example.chess.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PositionTest {

    @Test
    fun testPositionCaching() {
        // Position.of(r, c) should return the exact same cached instance for valid board coordinates
        for (row in 0..7) {
            for (col in 0..7) {
                val pos1 = Position.of(row, col)
                val pos2 = Position.of(row, col)
                assertSame(pos1, pos2)
            }
        }
    }

    @Test
    fun testAlgebraicNotationConversion() {
        // Test standard chess positions
        val a8 = Position.of(0, 0)
        assertEquals("a8", a8.toAlgebraicNotation())

        val e2 = Position.of(6, 4)
        assertEquals("e2", e2.toAlgebraicNotation())

        val h1 = Position.of(7, 7)
        assertEquals("h1", h1.toAlgebraicNotation())

        // Test fromAlgebraicNotation returns cached position
        val parsedE2 = Position.fromAlgebraicNotation("e2")
        assertEquals(6, parsedE2.row)
        assertEquals(4, parsedE2.column)
        assertSame(e2, parsedE2)

        val parsedA8 = Position.fromAlgebraicNotation("a8")
        assertSame(a8, parsedA8)
    }

    @Test
    fun testIsValidPosition() {
        val board = Board()
        assertTrue(board.isValidPosition(Position.of(0, 0)))
        assertTrue(board.isValidPosition(Position.of(7, 7)))
        assertTrue(board.isValidPosition(Position.of(3, 4)))

        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(8, 8)))
    }
}
