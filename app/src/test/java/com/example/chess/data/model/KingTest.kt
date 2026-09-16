package com.example.chess.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class KingTest {

    private lateinit var board: Board

    @Before
    fun setUp() {
        board = Board()
    }

    @Test
    fun testKingNormalMove() {
        val whiteKingSpot = board.getBox(7, 4)
        val whiteKing = whiteKingSpot.chessPiece as King

        // Standard 1-step move for King to e7 position (row 6, col 4) if empty
        board.getBox(6, 4).chessPiece = null // clear pawn in front of king
        val targetSpot = board.getBox(6, 4)

        assertTrue(whiteKing.canMove(board, whiteKingSpot, targetSpot))
    }

    @Test
    fun testKingInvalidMoveMoreThanOneSquare() {
        val whiteKingSpot = board.getBox(7, 4)
        val whiteKing = whiteKingSpot.chessPiece as King

        val targetSpot = board.getBox(5, 4) // 2 squares forward without castling
        assertFalse(whiteKing.canMove(board, whiteKingSpot, targetSpot))
    }

    @Test
    fun testKingSideCastlingWhenPathClearAndNotUnderAttack() {
        val whiteKingSpot = board.getBox(7, 4)
        val whiteKing = whiteKingSpot.chessPiece as King

        // Clear bishop and knight between King and King-side Rook
        board.getBox(7, 5).chessPiece = null
        board.getBox(7, 6).chessPiece = null

        val castlingTargetSpot = board.getBox(7, 6)

        assertTrue(whiteKing.canCastle(whiteKingSpot, castlingTargetSpot, board))
    }
}
