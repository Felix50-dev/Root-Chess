package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Rook
import com.example.chess.data.repository.GamePlay
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class HasMovedRestorationTest {

    private lateinit var board: Board
    private lateinit var game: GamePlay
    private lateinit var player1: Player  // white
    private lateinit var player2: Player  // black

    @Before
    fun setup() {
        board = Board()
        val stockfish = StockfishEngine()
        game = testGamePlay(board, stockfish)
        player1 = Player(isWhite = true, PlayerType.HUMAN)
        player2 = Player(isWhite = false, PlayerType.HUMAN)
        game.init(player1, player2, true)
        board.clear()
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun makeMove(
        piece: com.example.chess.data.model.ChessPiece,
        from: Position,
        to: Position,
        player: Player,
    ): Boolean {
        val fromSpot = board.getBox(from.row, from.column)
        val toSpot = board.getBox(to.row, to.column)
        fromSpot.chessPiece = piece
        return game.handleHumanMove(player, fromSpot, toSpot, false, board)
    }

    private fun placePiece(
        piece: com.example.chess.data.model.ChessPiece,
        position: Position,
    ) {
        board.getBox(position.row, position.column).chessPiece = piece
    }

    private fun pieceAt(row: Int, col: Int) =
        board.getBox(row, col).chessPiece

    // ── An otherwise-legal move that self-checks gets auto-reverted ────────

    @Test
    fun illegalMove_thatExposesOwnKingToCheck_doesNotLeaveHasMovedStuckTrue() {
        // White king on the e-file, white rook directly in front of it, black rook
        // pinning down the same file. Moving the white rook off the e-file is a
        // geometrically legal rook move, but exposes the king to check.
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val whiteRook = Rook(color = Color.WHITE, position = Position(6, 4), isKilled = false)
        val blackRook = Rook(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(whiteRook, Position(6, 4))
        placePiece(blackRook, Position(0, 4))
        placePiece(blackKing, Position(0, 0))
        game.currentTurn.value = player1

        val moved = makeMove(whiteRook, Position(6, 4), Position(6, 3), player1)

        assertFalse("Move exposing the king to check must be rejected", moved)
        assertEquals("Rook should be reverted to its origin", whiteRook, pieceAt(6, 4))
        assertNull("Destination square should be empty again", pieceAt(6, 3))
        assertFalse(
            "hasMoved must be restored to false — the rook never actually moved",
            whiteRook.hasMoved
        )
    }

    // ── The user-facing Undo button restores hasMoved for a real move ──────

    @Test
    fun undoMoveByPlayer_restoresHasMoved_forARealCommittedMove() {
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val whiteRook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(whiteRook, Position(7, 0))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        assertTrue(makeMove(whiteRook, Position(7, 0), Position(7, 3), player1))
        assertTrue("Sanity check: makeMove should have set hasMoved", whiteRook.hasMoved)

        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()

        assertNotNull("Rook should be back at its origin", pieceAt(7, 0))
        assertNull("Destination square should be empty again", pieceAt(7, 3))
        assertFalse(
            "hasMoved must be restored to its pre-move value after undo",
            whiteRook.hasMoved
        )
    }

    // ── End-to-end: castling right survives a reverted illegal king move ───

    @Test
    fun castlingRight_isPreserved_afterAnIllegalKingMoveIsReverted() {
        // Black rook makes (6,4) an attacked square, so the king's one-square
        // advance there is geometrically legal but leaves it in check.
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackRook = Rook(color = Color.BLACK, position = Position(6, 5), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackRook, Position(6, 5))
        placePiece(blackKing, Position(0, 0))
        game.currentTurn.value = player1

        val moved = makeMove(whiteKing, Position(7, 4), Position(6, 4), player1)
        assertFalse("King moving into an attacked square must be rejected", moved)
        assertFalse(
            "King's hasMoved must be restored to false after the illegal move is reverted",
            whiteKing.hasMoved
        )

        // Clear the attacker and set up a normal king-side castle — this must now
        // succeed, proving the king's castling right wasn't permanently lost.
        board.getBox(6, 5).chessPiece = null
        val whiteRook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        placePiece(whiteRook, Position(7, 7))

        val canCastle = whiteKing.canCastle(board.getBox(7, 4), board.getBox(7, 6), board)
        assertTrue("Castling should still be legal — the king never actually moved", canCastle)
    }

    // ── A different piece type: pawn ────────────────────────────────────────

    @Test
    fun illegalPawnMove_thatExposesOwnKingToCheck_doesNotLeaveHasMovedStuckTrue() {
        // Same e-file pin as the rook test above, but with a pawn as the blocker. A
        // straight push would stay on the e-file (still blocking the pin), so this uses
        // a diagonal capture instead, which actually takes the pawn off the file —
        // proves the fix isn't specific to Rook/King, it's in the shared makeMove/undo path.
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val whitePawn = Pawn(color = Color.WHITE, position = Position(6, 4), isKilled = false)
        val blackRook = Rook(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        val blackKnight = Knight(color = Color.BLACK, position = Position(5, 5), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(whitePawn, Position(6, 4))
        placePiece(blackRook, Position(0, 4))
        placePiece(blackKnight, Position(5, 5))
        placePiece(blackKing, Position(0, 0))
        game.currentTurn.value = player1

        val moved = makeMove(whitePawn, Position(6, 4), Position(5, 5), player1)

        assertFalse("Pawn capture exposing the king to check must be rejected", moved)
        assertEquals("Pawn should be reverted to its origin", whitePawn, pieceAt(6, 4))
        assertEquals("Captured knight should be restored", blackKnight, pieceAt(5, 5))
        assertFalse(
            "hasMoved must be restored to false — the pawn never actually moved",
            whitePawn.hasMoved
        )
    }

    // ── Legality-scanning (simulateMove/undoMove) must never touch hasMoved ─

    @Test
    fun getRemainingValidMoves_scanning_doesNotCorruptHasMoved() {
        // getRemainingValidMoves() simulates every candidate move and undoes it via the
        // 2-arg undoMove() to check for self-check. That path must never set wasCommitted,
        // so it must never touch hasMoved either — regardless of how many candidates it
        // simulates internally.
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val whiteRook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(whiteRook, Position(7, 0))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        val validMoves = game.getRemainingValidMoves(Position(7, 0), player1, board)

        assertTrue("Sanity check: the rook should have legal moves", validMoves.isNotEmpty())
        assertFalse(
            "Scanning for valid moves must not mutate hasMoved as a side effect",
            whiteRook.hasMoved
        )
        assertEquals("Rook must still be at its original square", whiteRook, pieceAt(7, 0))
    }

    // ── Redo re-applies hasMoved after an undo ──────────────────────────────

    @Test
    fun redoMove_reappliesHasMovedTrue_afterUndo() {
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val whiteRook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(whiteRook, Position(7, 0))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        assertTrue(makeMove(whiteRook, Position(7, 0), Position(7, 3), player1))
        val move = game.movesPlayed.last()

        game.undoMoveByPlayer(move)
        game.changeCurrentTurn()
        assertFalse("hasMoved should be false right after undo", whiteRook.hasMoved)
        assertTrue("Undone move should be tracked for redo", game.movesUndone.contains(move))

        game.redoMove(move, board)

        assertTrue("hasMoved should be true again after redo", whiteRook.hasMoved)
        assertEquals("Rook should be back at the redone destination", whiteRook, pieceAt(7, 3))
        assertNull("Origin square should be empty again after redo", pieceAt(7, 0))
    }

    // ── Sequential undos restore each piece's hasMoved independently ───────

    @Test
    fun sequentialUndos_restoreHasMoved_independentlyPerPiece() {
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val whiteRook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(1, 7), isKilled = false)
        val blackRook = Rook(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(whiteRook, Position(7, 0))
        placePiece(blackKing, Position(1, 7))
        placePiece(blackRook, Position(0, 0))
        game.currentTurn.value = player1

        assertTrue(makeMove(whiteRook, Position(7, 0), Position(7, 3), player1))
        assertTrue(makeMove(blackRook, Position(0, 0), Position(0, 3), player2))
        assertTrue("Sanity check", whiteRook.hasMoved)
        assertTrue("Sanity check", blackRook.hasMoved)

        // Undo black's move, then white's move — same order as repeatedly pressing Undo.
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()

        assertFalse("White rook's hasMoved must be independently restored", whiteRook.hasMoved)
        assertFalse("Black rook's hasMoved must be independently restored", blackRook.hasMoved)
        assertEquals(whiteRook, pieceAt(7, 0))
        assertEquals(blackRook, pieceAt(0, 0))
    }

    // ── Board extension ───────────────────────────────────────────────────────

    private fun Board.clear() {
        for (row in 0..7) for (col in 0..7) getBox(row, col).chessPiece = null
    }
}
