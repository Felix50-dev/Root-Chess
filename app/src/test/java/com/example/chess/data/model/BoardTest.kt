package com.example.chess.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun isValidPosition_returnsTrueForValidPositions() {
        val board = Board()
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(3, 4)))
        assertTrue(board.isValidPosition(Position(7, 7)))
    }

    @Test
    fun isValidPosition_returnsFalseForInvalidPositions() {
        val board = Board()
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(8, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(-5, -5)))
        assertFalse(board.isValidPosition(Position(10, 10)))
    }
}
