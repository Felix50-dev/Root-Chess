package com.example.chess

import com.example.chess.data.model.*
import com.example.chess.data.repository.GamePlay
import org.junit.Test
import org.junit.Assert.*

class BoardAndOptimizationTest {

    @Test
    fun testIsValidPosition() {
        val board = Board()
        assertTrue(board.isValidPosition(Position(0, 0)))
        assertTrue(board.isValidPosition(Position(7, 7)))
        assertTrue(board.isValidPosition(Position(3, 4)))
        assertFalse(board.isValidPosition(Position(-1, 0)))
        assertFalse(board.isValidPosition(Position(0, 8)))
        assertFalse(board.isValidPosition(Position(8, 8)))
    }

    @Test
    fun testBoardCloneRetainsState() {
        val board = Board()

        // Let's modify some pieces on the original board to have custom states
        val pawnSpot = board.getBox(1, 0)
        val pawn = pawnSpot.chessPiece as Pawn
        pawn.hasMoved = true
        pawn.canEnPassant = true

        val cloned = board.clone()

        // Verify that the clone is indeed a different board object
        assertNotSame(board, cloned)

        // Verify cloned piece values
        val clonedPawnSpot = cloned.getBox(1, 0)
        val clonedPawn = clonedPawnSpot.chessPiece as Pawn
        assertNotSame(pawn, clonedPawn)
        assertEquals(pawn.color, clonedPawn.color)
        assertTrue(clonedPawn.hasMoved)
        assertTrue(clonedPawn.canEnPassant)

        // Verify Position reuse
        assertSame(pawnSpot.position, clonedPawnSpot.position)
    }

    @Test
    fun testBoardClonePerformance() {
        // Measure performance of cloning
        val board = Board()
        val start = System.nanoTime()
        repeat(100) {
            board.clone()
        }
        val durationMs = (System.nanoTime() - start) / 1_000_000.0
        println("Duration for 100 deep clones: $durationMs ms")
        assertTrue(durationMs < 1000.0) // Cloning should be fast
    }
}
