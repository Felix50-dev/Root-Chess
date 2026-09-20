package com.example.chess.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class BoardTest {

    private lateinit var board: Board

    @Before
    fun setUp() {
        board = Board()
    }

    @Test
    fun isValidPosition_validPositions_returnsTrue() {
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))
        assertTrue(board.isValidPosition(Position(3, 4)))
        assertTrue(board.isValidPosition(Position(0, 7)))
        assertTrue(board.isValidPosition(Position(7, 0)))
    }

    @Test
    fun isValidPosition_invalidPositions_returnsFalse() {
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(8, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(-1, -1)))
        assertFalse(board.isValidPosition(Position(8, 8)))
    }
}
