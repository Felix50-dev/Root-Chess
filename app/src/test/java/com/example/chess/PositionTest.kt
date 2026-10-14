package com.example.chess

import com.example.chess.data.model.Board
import com.example.chess.data.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class PositionTest {

    @Test
    fun testPositionFlyweightPoolReusesInstances() {
        val pos1 = Position.of(3, 4)
        val pos2 = Position.of(3, 4)
        assertSame("Position.of(3, 4) must return the same pooled instance", pos1, pos2)
    }

    @Test
    fun testFromAlgebraicNotationUsesPooledInstancesAndCharArithmetic() {
        val pos1 = Position.fromAlgebraicNotation("e4")
        val pos2 = Position.of(4, 4)
        assertSame("fromAlgebraicNotation must return pooled position for e4", pos2, pos1)
        assertEquals(4, pos1.row)
        assertEquals(4, pos1.column)
        assertEquals("e4", pos1.toAlgebraicNotation())
    }

    @Test
    fun testIsValidPosition() {
        val board = Board()
        assertTrue(board.isValidPosition(Position.of(0, 0)))
        assertTrue(board.isValidPosition(Position.of(7, 7)))
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(8, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(0, 8)))
    }
}
