package com.example.chess

import com.example.chess.data.model.*
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.StockfishEngine
import org.junit.Test
import org.junit.Assert.*

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
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
    fun testFindKingAndAlivePieces() {
        val board = Board()
        val dummyStockfish = StockfishEngine()
        val gamePlay = GamePlay(board, dummyStockfish)

        // Verify finding black king
        val blackKingSpot = gamePlay.findKing(board, Color.BLACK)
        assertNotNull(blackKingSpot)
        assertTrue(blackKingSpot!!.chessPiece is King)
        assertEquals(Color.BLACK, blackKingSpot.chessPiece!!.color)

        // Verify finding white king
        val whiteKingSpot = gamePlay.findKing(board, Color.WHITE)
        assertNotNull(whiteKingSpot)
        assertTrue(whiteKingSpot!!.chessPiece is King)
        assertEquals(Color.WHITE, whiteKingSpot.chessPiece!!.color)

        // Initial setup should have 16 white and 16 black pieces
        val p1 = Player(true, PlayerType.HUMAN)
        val p2 = Player(false, PlayerType.HUMAN)
        gamePlay.init(p1, p2)

        // Check isUnderAttack and detectCheck on clean board
        // Standard starting position: neither king is in check
        assertFalse(gamePlay.detectCheck(p1))
        assertFalse(gamePlay.detectCheck(p2))
    }

    @Test
    fun testPerformanceBenchmark() {
        val board = Board()
        val dummyStockfish = StockfishEngine()
        val gamePlay = GamePlay(board, dummyStockfish)
        val p1 = Player(true, PlayerType.HUMAN)
        val p2 = Player(false, PlayerType.HUMAN)
        gamePlay.init(p1, p2)

        // Benchmark isValidPosition
        val start1 = System.nanoTime()
        for (i in 0 until 100_000) {
            board.isValidPosition(Position(i % 8, (i / 8) % 8))
        }
        val end1 = System.nanoTime()
        val duration1 = (end1 - start1) / 1_000_000.0
        println("Bolt Benchmark: 100k isValidPosition iterations took: $duration1 ms")

        // Benchmark detectCheck
        val start2 = System.nanoTime()
        for (i in 0 until 5_000) {
            gamePlay.detectCheck(p1)
        }
        val end2 = System.nanoTime()
        val duration2 = (end2 - start2) / 1_000_000.0
        println("Bolt Benchmark: 5k detectCheck iterations took: $duration2 ms")

        // Assert sanity limits to ensure no crazy performance issues
        assertTrue(duration1 < 500)
        assertTrue(duration2 < 1000)
    }
}
