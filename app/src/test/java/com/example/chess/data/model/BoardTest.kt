package com.example.chess.data.model

import org.junit.Assert.*
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
    fun testBoardCloningAndIndependence() {
        val originalBoard = Board()
        val originalPawn = originalBoard.getBox(1, 0).chessPiece as Pawn

        // Let's set some volatile states
        originalPawn.hasMoved = true
        originalPawn.canEnPassant = true

        val clonedBoard = originalBoard.clone()
        val clonedPawn = clonedBoard.getBox(1, 0).chessPiece as Pawn

        // Check if volatile states are correctly preserved/copied
        assertTrue(clonedPawn.hasMoved)
        assertTrue(clonedPawn.canEnPassant)
        assertEquals(originalPawn.color, clonedPawn.color)

        // Modify the cloned piece and check that the original remains unchanged
        clonedPawn.hasMoved = false
        clonedPawn.canEnPassant = false

        assertTrue(originalPawn.hasMoved)
        assertTrue(originalPawn.canEnPassant)
        assertFalse(clonedPawn.hasMoved)
        assertFalse(clonedPawn.canEnPassant)
    }

    @Test
    fun testManualInitializationBypassesResetBoard() {
        // Create custom empty box setup
        val customBoxes = List(8) { row ->
            List(8) { col ->
                Spot(Position(row, col), null)
            }
        }
        val board = Board(customBoxes)
        // Check that all boxes are empty (meaning resetBoard was bypassed)
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                assertNull(board.getBox(row, col).chessPiece)
            }
        }
    }
}
