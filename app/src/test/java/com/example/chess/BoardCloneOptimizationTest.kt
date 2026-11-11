package com.example.chess

import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BoardCloneOptimizationTest {

    @Test
    fun testDefaultBoardConstructorInitializesStandardStartingPosition() {
        val board = Board()
        assertEquals(Board.NUM_ROWS, board.boxes.size)
        assertEquals(Board.NUM_COLUMNS, board.boxes[0].size)

        // Row 1 should be Black Pawns, Row 6 should be White Pawns
        for (col in 0 until 8) {
            val blackPawn = board.getBox(1, col).chessPiece
            assertTrue(blackPawn is Pawn && blackPawn.color == Color.BLACK)
            val whitePawn = board.getBox(6, col).chessPiece
            assertTrue(whitePawn is Pawn && whitePawn.color == Color.WHITE)
        }
    }

    @Test
    fun testBoardClonePreservesStateAndIsolatesMutation() {
        val board = Board()
        board.enPassantTarget = Position(2, 3)

        // Mutate a square on original board
        board.getBox(4, 4).chessPiece = Knight(color = Color.WHITE, position = Position(4, 4), isKilled = false)

        val cloned = board.clone()

        // Verify clone has exact same structure and values
        assertEquals(Position(2, 3), cloned.enPassantTarget)
        val clonedKnight = cloned.getBox(4, 4).chessPiece
        assertTrue(clonedKnight is Knight && clonedKnight.color == Color.WHITE)
        assertNotSame(board.getBox(4, 4).chessPiece, clonedKnight)

        // Mutate clone and assert original is unaffected
        cloned.getBox(4, 4).chessPiece = null
        assertTrue(board.getBox(4, 4).chessPiece is Knight)
        assertNull(cloned.getBox(4, 4).chessPiece)
    }

    @Test
    fun testConstructorWithInitialBoxesBypassesResetBoard() {
        // Construct a board with empty spots only
        val emptyBoxes = List(8) { row ->
            List(8) { col ->
                com.example.chess.data.model.Spot(Position(row, col), null)
            }
        }
        val customBoard = Board(emptyBoxes)

        // Verify that resetBoard was not invoked by checking all squares are null
        for (r in 0 until 8) {
            for (c in 0 until 8) {
                assertNull(customBoard.getBox(r, c).chessPiece)
            }
        }
    }
}
