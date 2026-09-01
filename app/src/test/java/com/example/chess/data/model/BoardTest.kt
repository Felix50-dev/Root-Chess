package com.example.chess.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun testIsValidPosition() {
        val board = Board()
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))
        assertTrue(board.isValidPosition(Position(3, 4)))

        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, -1)))
        assertFalse(board.isValidPosition(Position(8, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
    }

    @Test
    fun testCloneBoard() {
        val originalBoard = Board()
        val pawn = originalBoard.getBox(1, 0).chessPiece as Pawn
        pawn.hasMoved = true
        pawn.canEnPassant = true

        val clonedBoard = originalBoard.clone()

        // Verify boxes structure is distinct
        assertNotSame(originalBoard.boxes, clonedBoard.boxes)
        assertNotSame(originalBoard.getBox(1, 0), clonedBoard.getBox(1, 0))

        // Verify pieces are distinct objects but maintain state
        val clonedPawn = clonedBoard.getBox(1, 0).chessPiece as Pawn
        assertNotSame(pawn, clonedPawn)
        assertEquals(pawn.color, clonedPawn.color)
        assertTrue(clonedPawn.hasMoved)
        assertTrue(clonedPawn.canEnPassant)

        // Verify modifying clone doesn't mutate original
        clonedBoard.getBox(1, 0).chessPiece = null
        assertTrue(originalBoard.getBox(1, 0).chessPiece != null)
    }
}
