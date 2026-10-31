package com.example.chess

import com.example.chess.data.model.Board
import com.example.chess.data.model.Position
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun isValidPosition_validCornersAndCenter_returnsTrue() {
        val board = Board()
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(0, 7)))
        assertTrue(board.isValidPosition(Position(7, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))
        assertTrue(board.isValidPosition(Position(3, 4)))
    }

    @Test
    fun isValidPosition_outOfBounds_returnsFalse() {
        val board = Board()
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(-1, -1)))
        assertFalse(board.isValidPosition(Position(8, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(8, 8)))
        assertFalse(board.isValidPosition(Position(100, 100)))
    }
}
