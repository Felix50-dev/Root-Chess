package com.example.chess.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardTest {

    @Test
    fun testBoardInitialization() {
        val board = Board()
        assertEquals(8, board.boxes.size)
        assertEquals(8, board.boxes[0].size)

        // Check a pawn on starting rank
        val whitePawnSpot = board.getBox(6, 0)
        assertTrue(whitePawnSpot.chessPiece is Pawn)
        assertEquals(Color.WHITE, whitePawnSpot.chessPiece?.color)
    }

    @Test
    fun testIsValidPosition() {
        val board = Board()
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(8, 8)))
    }

    @Test
    fun testBoardClonePreservesStateAndIsDeepCopy() {
        val board = Board()
        val pawn = board.getBox(6, 0).chessPiece as Pawn
        pawn.hasMoved = true
        pawn.canEnPassant = true

        val clonedBoard = board.clone()

        // Verify cloned board is a distinct instance
        assertNotSame(board, clonedBoard)
        assertNotSame(board.boxes[6][0], clonedBoard.boxes[6][0])

        // Verify cloned piece is a distinct instance but preserves volatile flags
        val clonedPawn = clonedBoard.getBox(6, 0).chessPiece as Pawn
        assertNotSame(pawn, clonedPawn)
        assertTrue(clonedPawn.hasMoved)
        assertTrue(clonedPawn.canEnPassant)
    }
}
