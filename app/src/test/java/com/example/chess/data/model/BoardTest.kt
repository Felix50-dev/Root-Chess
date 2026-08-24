package com.example.chess.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun testIsValidPosition_validPositions() {
        val board = Board()
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))
        assertTrue(board.isValidPosition(Position(3, 4)))
    }

    @Test
    fun testIsValidPosition_invalidPositions() {
        val board = Board()
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(8, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(-5, -5)))
    }

    @Test
    fun testClone_preservesPieceStateAndStructure() {
        val board = Board()
        val originalPawn = board.getBox(1, 0).chessPiece as Pawn
        originalPawn.hasMoved = true
        originalPawn.canEnPassant = true

        val clonedBoard = board.clone()

        // Verify boards are distinct instances
        assertNotSame(board, clonedBoard)
        assertNotSame(board.boxes, clonedBoard.boxes)

        // Verify cloned piece preserves hasMoved and canEnPassant state
        val clonedPawn = clonedBoard.getBox(1, 0).chessPiece as Pawn
        assertNotSame(originalPawn, clonedPawn)
        assertTrue(clonedPawn.hasMoved)
        assertTrue(clonedPawn.canEnPassant)
        assertEquals(originalPawn.color, clonedPawn.color)

        // Verify modifying cloned piece doesn't affect original
        clonedPawn.hasMoved = false
        assertTrue(originalPawn.hasMoved)
    }
}
