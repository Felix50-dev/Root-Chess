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
    fun testBoardCloneReusesPositions() {
        val board = Board()
        val clonedBoard = board.clone()

        for (row in 0 until 8) {
            for (col in 0 until 8) {
                val originalSpot = board.getBox(row, col)
                val clonedSpot = clonedBoard.getBox(row, col)

                // Position objects should be reused
                assertSame("Position should be reused at ($row, $col)", originalSpot.position, clonedSpot.position)

                // Spots should be different
                assertNotSame("Spot should be cloned at ($row, $col)", originalSpot, clonedSpot)

                // Pieces should be different but have same state
                if (originalSpot.chessPiece != null) {
                    assertNotSame("Piece should be cloned at ($row, $col)", originalSpot.chessPiece, clonedSpot.chessPiece)
                    assertEquals("Piece color should match at ($row, $col)", originalSpot.chessPiece!!.color, clonedSpot.chessPiece!!.color)
                }
            }
        }
    }

    @Test
    fun testBoardClonePreservesPieceState() {
        val board = Board()
        val pawnSpot = board.getBox(1, 0)
        val pawn = pawnSpot.chessPiece as Pawn
        pawn.hasMoved = true
        pawn.canEnPassant = true

        val clonedBoard = board.clone()
        val clonedPawn = clonedBoard.getBox(1, 0).chessPiece as Pawn

        assertTrue("Cloned pawn should have hasMoved = true", clonedPawn.hasMoved)
        assertTrue("Cloned pawn should have canEnPassant = true", clonedPawn.canEnPassant)
    }

    @Test
    fun testBoardConstructorBypassesResetWhenBoxesProvided() {
        val customBoxes = List(8) { row ->
            List(8) { col ->
                Spot(Position(row, col), null)
            }
        }
        val board = Board(customBoxes)

        // If it bypassed resetBoard(), all spots should be empty
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                assertNull("Spot at ($row, $col) should be empty", board.getBox(row, col).chessPiece)
            }
        }
    }
}
