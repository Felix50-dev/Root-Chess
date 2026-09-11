package com.example.chess.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun isValidPosition_returnsTrue_forValidPositionsOnBoard() {
        val board = Board()

        // Corner positions
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(0, 7)))
        assertTrue(board.isValidPosition(Position(7, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))

        // Middle positions
        assertTrue(board.isValidPosition(Position(3, 4)))
        assertTrue(board.isValidPosition(Position(2, 5)))
    }

    @Test
    fun isValidPosition_returnsFalse_forOutOfBoundsPositions() {
        val board = Board()

        // Negative indices
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(-1, -1)))

        // Indices beyond board boundary (>= 8)
        assertFalse(board.isValidPosition(Position(8, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(8, 8)))
        assertFalse(board.isValidPosition(Position(10, 10)))
    }
}
