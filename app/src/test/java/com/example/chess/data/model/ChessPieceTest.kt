package com.example.chess.data.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ChessPieceTest {

    @Test
    fun testRookCanMoveAndPathClear() {
        val board = Board()
        val startSpot = board.getBox(7, 0) // White Rook
        val emptySpot = board.getBox(5, 0)

        // Clear piece in front of rook (Pawn at 6, 0)
        board.getBox(6, 0).chessPiece = null

        val rook = startSpot.chessPiece as Rook
        assertTrue("Rook should be able to move along empty column", rook.canMove(board, startSpot, emptySpot))
    }

    @Test
    fun testBishopCanMoveAndPathClear() {
        val board = Board()
        val startSpot = board.getBox(7, 2) // White Bishop
        val emptySpot = board.getBox(5, 0)

        // Clear pawn at (6, 1)
        board.getBox(6, 1).chessPiece = null

        val bishop = startSpot.chessPiece as Bishop
        assertTrue("Bishop should be able to move diagonally along clear path", bishop.canMove(board, startSpot, emptySpot))
    }

    @Test
    fun testQueenCanMoveAndPathClear() {
        val board = Board()
        val startSpot = board.getBox(7, 3) // White Queen
        val emptySpot = board.getBox(4, 3)

        // Clear pawn at (6, 3)
        board.getBox(6, 3).chessPiece = null

        val queen = startSpot.chessPiece as Queen
        assertTrue("Queen should be able to move vertically along clear path", queen.canMove(board, startSpot, emptySpot))
    }

    @Test
    fun testKingCanCastleWhenPathClearAndNotUnderAttack() {
        val board = Board()
        // White King is at (7, 4), White Rook is at (7, 7)
        // Clear pieces at (7, 5) and (7, 6)
        board.getBox(7, 5).chessPiece = null
        board.getBox(7, 6).chessPiece = null

        val kingSpot = board.getBox(7, 4)
        val targetSpot = board.getBox(7, 6)
        val king = kingSpot.chessPiece as King

        assertTrue("King should be able to castle king-side when clear and safe", king.canCastle(kingSpot, targetSpot, board))
    }
}
