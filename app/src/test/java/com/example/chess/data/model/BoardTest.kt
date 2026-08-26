package com.example.chess.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun `isValidPosition returns true for valid board positions`() {
        val board = Board()
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))
        assertTrue(board.isValidPosition(Position(3, 4)))
    }

    @Test
    fun `isValidPosition returns false for out of bound positions`() {
        val board = Board()
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(8, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(10, 10)))
        assertFalse(board.isValidPosition(Position(-5, -5)))
    }
}
