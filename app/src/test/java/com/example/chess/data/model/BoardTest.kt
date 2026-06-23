package com.example.chess.data.model

import org.junit.Test
import org.junit.Assert.*

class BoardTest {

    @Test
    fun testIsValidPosition() {
        val board = Board()
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
    }

    @Test
    fun testClone() {
        val board = Board()
        val clonedBoard = board.clone()

        assertNotSame(board, clonedBoard)
        assertNotSame(board.boxes, clonedBoard.boxes)

        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val originalSpot = board.getBox(row, col)
                val clonedSpot = clonedBoard.getBox(row, col)
                assertNotSame(originalSpot, clonedSpot)
                if (originalSpot.chessPiece != null) {
                    assertNotNull(clonedSpot.chessPiece)
                    assertNotSame(originalSpot.chessPiece, clonedSpot.chessPiece)
                    assertEquals(originalSpot.chessPiece!!.color, clonedSpot.chessPiece!!.color)
                    assertEquals(originalSpot.chessPiece!!.javaClass, clonedSpot.chessPiece!!.javaClass)
                    assertEquals(originalSpot.chessPiece!!.hasMoved, clonedSpot.chessPiece!!.hasMoved)
                    assertEquals(originalSpot.chessPiece!!.canEnPassant, clonedSpot.chessPiece!!.canEnPassant)
                    // Position should be reused (referential equality)
                    assertSame(originalSpot.position, clonedSpot.position)
                } else {
                    assertNull(clonedSpot.chessPiece)
                }
            }
        }
    }

    @Test
    fun testClonePreservesState() {
        val board = Board()
        val whitePawnSpot = board.getBox(6, 0)
        val whitePawn = whitePawnSpot.chessPiece as Pawn
        whitePawn.hasMoved = true
        whitePawn.canEnPassant = true

        val clonedBoard = board.clone()
        val clonedPawn = clonedBoard.getBox(6, 0).chessPiece as Pawn

        assertTrue(clonedPawn.hasMoved)
        assertTrue(clonedPawn.canEnPassant)
    }
}
