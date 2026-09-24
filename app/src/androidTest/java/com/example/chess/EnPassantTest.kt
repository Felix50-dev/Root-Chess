package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.repository.GamePlay
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertSame
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class EnPassantTest {

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

        placePiece(King(color = Color.WHITE, position = Position(7, 4), isKilled = false), Position(7, 4))
        placePiece(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), Position(0, 4))
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

    // ── Two-square push arms the target ─────────────────────────────────────

    @Test
    fun twoSquarePush_setsEnPassantTargetBehindThePawn() {
        val blackPawn = Pawn(color = Color.BLACK, position = Position(1, 3), isKilled = false)
        placePiece(blackPawn, Position(1, 3))
        game.currentTurn.value = player2

        assertTrue(makeMove(blackPawn, Position(1, 3), Position(3, 3), player2))

        assertEquals(Position(2, 3), board.enPassantTarget)
    }

    // ── Capture works immediately after the push ────────────────────────────

    @Test
    fun whitePawn_capturesEnPassant_immediatelyAfterBlackTwoSquarePush() {
        val whitePawn = Pawn(color = Color.WHITE, position = Position(3, 4), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(1, 3), isKilled = false)
        placePiece(whitePawn, Position(3, 4))
        placePiece(blackPawn, Position(1, 3))

        game.currentTurn.value = player2
        assertTrue(makeMove(blackPawn, Position(1, 3), Position(3, 3), player2))

        // handleHumanMove's changeCurrentTurn() already handed the turn back to white
        assertTrue(game.handleHumanMove(player1, board.getBox(3, 4), board.getBox(2, 3), false, board))

        assertEquals("Capturing pawn should land on the en passant square", whitePawn, pieceAt(2, 3))
        assertNull("Origin square should be empty", pieceAt(3, 4))
        assertNull("Captured black pawn should be removed from its square", pieceAt(3, 3))
        assertNull("En passant target is consumed after the capture", board.enPassantTarget)

        val lastMove = game.movesPlayed.last()
        assertTrue("Move should be flagged as en passant", lastMove.isEnPassant)
        assertNotNull("Move should record the captured pawn", lastMove.pieceKilled)
        assertEquals(Color.BLACK, lastMove.pieceKilled?.color)
        assertEquals(1, game.piecesKilled.size)
        assertEquals(Color.BLACK, game.piecesKilled.first().color)
    }

    // ── Opportunity expires if not taken immediately ────────────────────────

    @Test
    fun enPassant_isNoLongerLegal_afterAnyOtherMove() {
        val whitePawn = Pawn(color = Color.WHITE, position = Position(3, 4), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(1, 3), isKilled = false)
        val idleWhitePawn = Pawn(color = Color.WHITE, position = Position(6, 0), isKilled = false)
        placePiece(whitePawn, Position(3, 4))
        placePiece(blackPawn, Position(1, 3))
        placePiece(idleWhitePawn, Position(6, 0))

        game.currentTurn.value = player2
        assertTrue(makeMove(blackPawn, Position(1, 3), Position(3, 3), player2))
        assertEquals(Position(2, 3), board.enPassantTarget)

        // White plays something unrelated instead of capturing en passant
        assertTrue(makeMove(idleWhitePawn, Position(6, 0), Position(5, 0), player1))

        assertNull("En passant right is forfeited once it isn't taken immediately", board.enPassantTarget)
        assertFalse(
            "isEnPassant should no longer recognize the (now stale) diagonal move",
            whitePawn.isEnPassant(board, board.getBox(3, 4), board.getBox(2, 3))
        )
    }

    // ── Undo restores the actual captured pawn object, not a fabricated one ─

    @Test
    fun undo_afterEnPassantCapture_restoresTheSameCapturedPawnObject() {
        val whitePawn = Pawn(color = Color.WHITE, position = Position(3, 4), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(1, 3), isKilled = false)
        placePiece(whitePawn, Position(3, 4))
        placePiece(blackPawn, Position(1, 3))

        game.currentTurn.value = player2
        makeMove(blackPawn, Position(1, 3), Position(3, 3), player2)
        game.handleHumanMove(player1, board.getBox(3, 4), board.getBox(2, 3), false, board)

        game.undoMoveByPlayer(game.movesPlayed.last())

        assertSame(
            "Undo should restore the exact same pawn instance that was captured, " +
                "not a newly fabricated one — identity is relied on elsewhere " +
                "(e.g. animation matching by piece id)",
            blackPawn,
            pieceAt(3, 3)
        )
        assertFalse("Restored pawn should no longer be marked killed", blackPawn.isKilled)
    }

    // ── Undo restores both the board and the en passant target ─────────────

    @Test
    fun undo_afterEnPassantCapture_restoresCapturedPawnAndTarget() {
        val whitePawn = Pawn(color = Color.WHITE, position = Position(3, 4), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(1, 3), isKilled = false)
        placePiece(whitePawn, Position(3, 4))
        placePiece(blackPawn, Position(1, 3))

        game.currentTurn.value = player2
        makeMove(blackPawn, Position(1, 3), Position(3, 3), player2)
        game.handleHumanMove(player1, board.getBox(3, 4), board.getBox(2, 3), false, board)

        game.undoMoveByPlayer(game.movesPlayed.last())

        assertNotNull("White pawn should be back at its origin", pieceAt(3, 4))
        assertNull("En passant destination square should be empty again", pieceAt(2, 3))
        assertNotNull("Captured black pawn should be restored", pieceAt(3, 3))
        assertEquals(Color.BLACK, pieceAt(3, 3)?.color)
        assertTrue("piecesKilled should no longer contain the restored pawn", game.piecesKilled.isEmpty())
        assertEquals(
            "En passant target should be restored to what it was before the capture",
            Position(2, 3),
            board.enPassantTarget
        )
    }

    // ── Board extension ───────────────────────────────────────────────────────

    private fun Board.clear() {
        for (row in 0..7) for (col in 0..7) getBox(row, col).chessPiece = null
    }
}
