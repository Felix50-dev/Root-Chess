package com.example.chess

import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

class KingTest {

    private lateinit var board: Board
    private lateinit var king: King
    private val white = Color.WHITE
    private val black = Color.BLACK

    @Before
    fun setup() {
        board = Board() // assuming a fresh board instance
    }

    @Test
    fun `king can move one square in any direction`() {
        king = King(white, Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = king
        val directions = listOf(
            Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1),
            Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)
        )
        for ((dx, dy) in directions) {
            val newRow = king.position.row + dx
            val newCol = king.position.column + dy
            val end = board.getBox(newRow, newCol)
            assertTrue("Failed on move to ($newRow, $newCol)", king.canMove(board, board.getBox(4, 4), end))
        }
    }

    @Test
    fun `king cannot move more than one square`() {
        king = King(white, Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = king
        val invalidMoves = listOf(
            Pair(2, 4), // 2 steps up
            Pair(4, 6), // 2 steps right
            Pair(6, 4), // 2 steps down
            Pair(4, 1), // 3 steps left
            Pair(2, 2), // 2 steps diagonal
            Pair(6, 6)  // 2 steps diagonal other direction
        )
        for ((row, col) in invalidMoves) {
            val end = board.getBox(row, col)
            assertFalse("Should not be allowed to move to ($row, $col)", king.canMove(board, board.getBox(4, 4), end))
        }
    }

    @Test
    fun `king cannot capture same color piece`() {
        king = King(white, Position(7, 4), isKilled = false)
        board.getBox(7, 4).chessPiece = king
        val allyPawn = Pawn(white, Position(6, 4), isKilled = false)
        board.getBox(6, 4).chessPiece = allyPawn
        val canMove = king.canMove(board, board.getBox(7, 4), board.getBox(6, 4))
        assertFalse("King should not capture same color", canMove)
    }

    @Test
    fun `king can capture opponent piece`() {
        king = King(white, Position(7, 4), isKilled = false)
        board.getBox(7, 4).chessPiece = king
        val enemyPawn = Pawn(black, Position(6, 4), false )
        board.getBox(6, 4).chessPiece = enemyPawn
        val canMove = king.canMove(board, board.getBox(7, 4), board.getBox(6, 4))
        assertTrue("King should be able to capture opponent", canMove)
    }

    @Test
    fun `king side castling succeeds when valid`() {
        king = King(white, Position(7, 4), false)
        board.getBox(7, 4).chessPiece = king
        val rook = Rook(white, Position(7, 7), false)
        board.getBox(7, 7).chessPiece = rook
        board.getBox(7, 5).chessPiece = null
        board.getBox(7, 6).chessPiece = null
        king.hasMoved = false
        rook.hasMoved = false

        val canCastle = king.canCastle(board.getBox(7, 4), board.getBox(7, 6), board)
        assertTrue("Valid king-side castling should be allowed", canCastle)
    }

    @Test
    fun `queen side castling fails when piece blocks`() {
        king = King(white, Position(7, 4), false)
        board.getBox(7, 4).chessPiece = king
        val rook = Rook(white, Position(7, 0), false)
        board.getBox(7, 0).chessPiece = rook
        board.getBox(7, 3).chessPiece = Queen(white, Position(7, 3), false)
        king.hasMoved = false
        rook.hasMoved = false

        val canCastle = king.canCastle(board.getBox(7, 4), board.getBox(7, 2), board)
        assertFalse("Queen-side castling should fail when piece blocks path", canCastle)
    }

    @Test
    fun `king cannot castle when in check`() {
        king = King(white, Position(7, 4), false)
        board.getBox(7, 4).chessPiece = king
        val rook = Rook(white, Position(7, 7), false)
        board.getBox(7, 7).chessPiece = rook
        board.getBox(7, 5).chessPiece = null
        board.getBox(7, 6).chessPiece = null
        king.hasMoved = false
        rook.hasMoved = false

        // Attacker placing king in check
        val enemyRook = Rook(black, Position(6, 4), false)
        board.getBox(6, 4).chessPiece = enemyRook

        val canCastle = king.canCastle(board.getBox(7, 4), board.getBox(7, 6), board)
        assertFalse("King should not castle when in check", canCastle)
    }

    @Test
    fun `castling moves king and rook correctly`() {
        king = King(white, Position(7, 4), false)
        board.getBox(7, 4).chessPiece = king
        val rook = Rook(white, Position(7, 7), false)
        board.getBox(7, 7).chessPiece = rook
        board.getBox(7, 5).chessPiece = null
        board.getBox(7, 6).chessPiece = null
        king.hasMoved = false
        rook.hasMoved = false

        king.castlingMove(board.getBox(7, 4), board.getBox(7, 6), board)

        assertEquals("King should move to (7,6)", king, board.getBox(7, 6).chessPiece)
        assertEquals("Rook should move to (7,5)", rook, board.getBox(7, 5).chessPiece)
    }

    @Test
    fun `king should not crash when near board edges`() {
        val edgePositions = listOf(
            Position(0, 0),   // top-left corner
            Position(0, 7),   // top-right corner
            Position(7, 0),   // bottom-left corner
            Position(7, 7)    // bottom-right corner
        )

        val directions = listOf(
            Pair(-1, 0), Pair(1, 0), Pair(0, -1), Pair(0, 1),
            Pair(-1, -1), Pair(-1, 1), Pair(1, -1), Pair(1, 1)
        )

        for (pos in edgePositions) {
            val king = King(white, pos, false)
            board.getBox(pos.row, pos.column).chessPiece = king

            for ((dx, dy) in directions) {
                val newRow = pos.row + dx
                val newCol = pos.column + dy

                try {
                    if (newRow in 0..7 && newCol in 0..7) {
                        val start = board.getBox(pos.row, pos.column)
                        val end = board.getBox(newRow, newCol)
                        king.canMove(board, start, end) // just check it runs
                    } else {
                        // out-of-bounds move should be safely rejected
                        // optionally: check that no exception occurs even if you call canMove
                        // if your canMove handles out-of-bounds internally
                    }
                } catch (e: IndexOutOfBoundsException) {
                    fail("King move from $pos to ($newRow, $newCol) caused IndexOutOfBoundsException")
                }
            }

            board.getBox(pos.row, pos.column).chessPiece = null // cleanup
        }
    }
}
