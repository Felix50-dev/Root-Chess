package com.example.chess.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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
    fun testBoardClonePreservesState() {
        val board = Board()
        val pawnSpot = board.getBox(1, 0)
        val pawn = pawnSpot.chessPiece as Pawn
        pawn.hasMoved = true
        pawn.canEnPassant = true

        val clonedBoard = board.clone()

        assertNotSame(board, clonedBoard)
        assertNotSame(board.boxes, clonedBoard.boxes)

        val clonedPawnSpot = clonedBoard.getBox(1, 0)
        val clonedPawn = clonedPawnSpot.chessPiece as Pawn

        assertNotNull(clonedPawn)
        assertNotSame(pawn, clonedPawn)
        assertEquals(pawn.color, clonedPawn.color)
        assertTrue(clonedPawn.hasMoved)
        assertTrue(clonedPawn.canEnPassant)
    }

    @Test
    fun testResetBoard() {
        val board = Board()
        assertNotNull(board.getBox(0, 4).chessPiece) // Black King
        assertNotNull(board.getBox(7, 4).chessPiece) // White King
        assertEquals(Color.BLACK, board.getBox(0, 4).chessPiece?.color)
        assertEquals(Color.WHITE, board.getBox(7, 4).chessPiece?.color)
    }
}
