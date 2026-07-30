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
        assertFalse(board.isValidPosition(Position(8, 7)))
        assertFalse(board.isValidPosition(Position(7, 8)))
    }

    @Test
    fun testBoardCloningPreservesState() {
        val board = Board()
        // Get a piece, say a pawn
        val pawnSpot = board.getBox(6, 4)
        val pawn = pawnSpot.chessPiece as Pawn
        pawn.hasMoved = true
        pawn.canEnPassant = true

        val clonedBoard = board.clone()
        val clonedPawnSpot = clonedBoard.getBox(6, 4)
        val clonedPawn = clonedPawnSpot.chessPiece as Pawn

        // Check deep copy correctness
        assertNotSame(pawn, clonedPawn)
        assertNotSame(pawnSpot, clonedPawnSpot)
        assertEquals(pawn.color, clonedPawn.color)
        assertEquals(pawn.isKilled, clonedPawn.isKilled)
        assertTrue(clonedPawn.hasMoved)
        assertTrue(clonedPawn.canEnPassant)
    }

    @Test
    fun testCustomBoardConstructorAndClone() {
        val customBoxes = List(8) { row ->
            List(8) { col ->
                Spot(Position(row, col), null)
            }
        }
        val customBoard = Board(customBoxes)
        assertSame(customBoxes, customBoard.boxes)
        assertNull(customBoard.getBox(0, 0).chessPiece)
    }
}
