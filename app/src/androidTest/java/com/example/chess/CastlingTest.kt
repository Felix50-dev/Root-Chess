package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.Spot
import com.example.chess.data.repository.GamePlay
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class CastlingTest {

    private lateinit var board: Board
    private lateinit var game: GamePlay
    private lateinit var player1: Player
    private lateinit var player2: Player

    @Before
    fun setup() {
        board = Board()
        val stockfish = StockfishEngine()
        game = GamePlay(board, stockfish)
        player1 = Player(isWhite = true,  playerType = PlayerType.HUMAN)
        player2 = Player(isWhite = false, playerType = PlayerType.HUMAN)
        game.init(player1, player2, true)
        clearBoard()
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private fun clearBoard() {
        for (row in 0..7) for (col in 0..7)
            board.getBox(row, col).chessPiece = null
    }

    private fun place(piece: com.example.chess.data.model.ChessPiece, row: Int, col: Int) {
        board.getBox(row, col).chessPiece = piece
        piece.position = Position(row, col)
    }

    private fun spotAt(row: Int, col: Int): Spot = board.getBox(row, col)
    private fun pieceAt(row: Int, col: Int) = board.getBox(row, col).chessPiece

    private fun canCastle(king: King, fromRow: Int, fromCol: Int, toCol: Int): Boolean {
        val start = spotAt(fromRow, fromCol)
        val end   = spotAt(fromRow, toCol)
        return king.canCastle(start, end, board)
    }

    private fun doCastle(king: King, fromRow: Int, fromCol: Int, toCol: Int) {
        val start = spotAt(fromRow, fromCol)
        val end   = spotAt(fromRow, toCol)
        king.castlingMove(start, end, board)
    }

    // ── canCastle — king-side ────────────────────────────────────────────────

    @Test
    fun kingSideCastle_clearedPath_allowed() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 7)
        // Also need a black king somewhere so detectCheck has both players
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertTrue("King-side castling should be allowed with clear path",
            canCastle(king, 7, 4, 6))
    }

    @Test
    fun kingSideCastle_bishopInWay_notAllowed() {
        val king   = King(color = Color.WHITE,  position = Position(7, 4), isKilled = false)
        val rook   = Rook(color = Color.WHITE,  position = Position(7, 7), isKilled = false)
        val bishop = Bishop(color = Color.WHITE, position = Position(7, 5), isKilled = false)
        place(king,   7, 4)
        place(rook,   7, 7)
        place(bishop, 7, 5)   // blocks f1
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("Bishop on f1 should block king-side castling",
            canCastle(king, 7, 4, 6))
    }

    @Test
    fun kingSideCastle_knightInWay_notAllowed() {
        val king   = King(color = Color.WHITE,  position = Position(7, 4), isKilled = false)
        val rook   = Rook(color = Color.WHITE,  position = Position(7, 7), isKilled = false)
        val knight = Knight(color = Color.WHITE, position = Position(7, 6), isKilled = false)
        place(king,   7, 4)
        place(rook,   7, 7)
        place(knight, 7, 6)   // blocks g1
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("Knight on g1 should block king-side castling",
            canCastle(king, 7, 4, 6))
    }

    @Test
    fun kingSideCastle_kingAlreadyMoved_notAllowed() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        king.hasMoved = true
        val rook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 7)
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("King that has already moved cannot castle",
            canCastle(king, 7, 4, 6))
    }

    @Test
    fun kingSideCastle_rookAlreadyMoved_notAllowed() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        rook.hasMoved = true
        place(king, 7, 4)
        place(rook, 7, 7)
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("Rook that has already moved cannot be used for castling",
            canCastle(king, 7, 4, 6))
    }

    @Test
    fun kingSideCastle_noRookOnH1_notAllowed() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        place(king, 7, 4)
        // No rook placed at h1
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("No rook on h1 — king-side castling not allowed",
            canCastle(king, 7, 4, 6))
    }

    @Test
    fun kingSideCastle_nonRookOnH1_notAllowed() {
        val king   = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val bishop = Bishop(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        place(king,   7, 4)
        place(bishop, 7, 7)   // bishop where rook should be
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("Non-rook piece on h1 should not allow castling",
            canCastle(king, 7, 4, 6))
    }

    // ── canCastle — queen-side ───────────────────────────────────────────────

    @Test
    fun queenSideCastle_clearedPath_allowed() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 0)
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertTrue("Queen-side castling should be allowed with clear path",
            canCastle(king, 7, 4, 2))
    }

    @Test
    fun queenSideCastle_queenInWay_notAllowed() {
        val king  = King(color = Color.WHITE,  position = Position(7, 4), isKilled = false)
        val rook  = Rook(color = Color.WHITE,  position = Position(7, 0), isKilled = false)
        val queen = Queen(color = Color.WHITE, position = Position(7, 3), isKilled = false)
        place(king,  7, 4)
        place(rook,  7, 0)
        place(queen, 7, 3)   // blocks d1
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("Queen on d1 should block queen-side castling",
            canCastle(king, 7, 4, 2))
    }

    @Test
    fun queenSideCastle_knightOnB1_notAllowed() {
        val king   = King(color = Color.WHITE,  position = Position(7, 4), isKilled = false)
        val rook   = Rook(color = Color.WHITE,  position = Position(7, 0), isKilled = false)
        val knight = Knight(color = Color.WHITE, position = Position(7, 1), isKilled = false)
        place(king,   7, 4)
        place(rook,   7, 0)
        place(knight, 7, 1)   // blocks b1
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("Knight on b1 should block queen-side castling",
            canCastle(king, 7, 4, 2))
    }

    @Test
    fun queenSideCastle_bishopOnC1_notAllowed() {
        val king   = King(color = Color.WHITE,  position = Position(7, 4), isKilled = false)
        val rook   = Rook(color = Color.WHITE,  position = Position(7, 0), isKilled = false)
        val bishop = Bishop(color = Color.WHITE, position = Position(7, 2), isKilled = false)
        place(king,   7, 4)
        place(rook,   7, 0)
        place(bishop, 7, 2)   // blocks c1
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("Bishop on c1 should block queen-side castling",
            canCastle(king, 7, 4, 2))
    }

    @Test
    fun queenSideCastle_kingAlreadyMoved_notAllowed() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        king.hasMoved = true
        val rook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 0)
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("King that has already moved cannot castle queen-side",
            canCastle(king, 7, 4, 2))
    }

    @Test
    fun queenSideCastle_rookAlreadyMoved_notAllowed() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        rook.hasMoved = true
        place(king, 7, 4)
        place(rook, 7, 0)
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("Rook that has already moved cannot be used for castling",
            canCastle(king, 7, 4, 2))
    }

    @Test
    fun queenSideCastle_wrongColorRook_notAllowed() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.BLACK, position = Position(7, 0), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 0)   // opponent's rook
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)

        assertFalse("Cannot castle with opponent's rook",
            canCastle(king, 7, 4, 2))
    }

    // ── castlingMove — piece placement after castling ────────────────────────

    @Test
    fun castlingMove_kingSide_kingLandsOnG1() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 7)

        doCastle(king, 7, 4, 6)

        assertNull("e1 should be empty after castling",   pieceAt(7, 4))
        assertNotNull("g1 should have the king",          pieceAt(7, 6))
        assertTrue("g1 piece should be the king",        pieceAt(7, 6) is King)
        assertEquals(Color.WHITE,                         pieceAt(7, 6)?.color)
    }

    @Test
    fun castlingMove_kingSide_rookLandsOnF1() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 7)

        doCastle(king, 7, 4, 6)

        assertNull("h1 should be empty after castling",   pieceAt(7, 7))
        assertNotNull("f1 should have the rook",          pieceAt(7, 5))
        assertTrue("f1 piece should be the rook",        pieceAt(7, 5) is Rook)
        assertEquals(Color.WHITE,                         pieceAt(7, 5)?.color)
    }

    @Test
    fun castlingMove_queenSide_kingLandsOnC1() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 0)

        doCastle(king, 7, 4, 2)

        assertNull("e1 should be empty after castling",   pieceAt(7, 4))
        assertNotNull("c1 should have the king",          pieceAt(7, 2))
        assertTrue("c1 piece should be the king",        pieceAt(7, 2) is King)
        assertEquals(Color.WHITE,                         pieceAt(7, 2)?.color)
    }

    @Test
    fun castlingMove_queenSide_rookLandsOnD1() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 0)

        doCastle(king, 7, 4, 2)

        assertNull("a1 should be empty after castling",   pieceAt(7, 0))
        assertNotNull("d1 should have the rook",          pieceAt(7, 3))
        assertTrue("d1 piece should be the rook",        pieceAt(7, 3) is Rook)
        assertEquals(Color.WHITE,                         pieceAt(7, 3)?.color)
    }

    @Test
    fun castlingMove_kingSide_kingPositionFieldUpdated() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 7)

        doCastle(king, 7, 4, 6)

        assertEquals("King's position field should be g1 after castling",
            Position(7, 6), king.position)
    }

    @Test
    fun castlingMove_queenSide_rookPositionFieldUpdated() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 0)

        doCastle(king, 7, 4, 2)

        assertEquals("Rook's position field should be d1 after castling",
            Position(7, 3), rook.position)
    }

    // castlingMove() runs from both a real committed move (GamePlay.makeMove) and a
    // purely speculative legality scan (GamePlay.simulateMove, used to compute
    // valid-move dots) — it must not set hasMoved itself, only report which rook moved,
    // so the caller can decide (mirrors how the king's own hasMoved is handled). See
    // getRemainingValidMoves_scanningCastlingSquare_doesNotPoisonRookHasMoved and
    // undoMoveByPlayer_afterCastling_restoresRookHasMovedAndCastlingRight below for the
    // real-move contract this enables.

    @Test
    fun castlingMove_kingSide_returnsTheRookItMoved() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 7)

        val moved = king.castlingMove(spotAt(7, 4), spotAt(7, 6), board)

        assertEquals("castlingMove should return the rook it moved", rook, moved)
        assertFalse(
            "castlingMove itself must not set hasMoved — it also runs during " +
                "speculative legality scans, which must not mutate committed state",
            rook.hasMoved
        )
    }

    @Test
    fun castlingMove_queenSide_returnsTheRookItMoved() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 0)

        val moved = king.castlingMove(spotAt(7, 4), spotAt(7, 2), board)

        assertEquals("castlingMove should return the rook it moved", rook, moved)
        assertFalse(
            "castlingMove itself must not set hasMoved — it also runs during " +
                "speculative legality scans, which must not mutate committed state",
            rook.hasMoved
        )
    }

    // ── Regression: computing valid-move dots must not poison castling rights ──
    //
    // castlingMove() runs both for a real committed move (GamePlay.makeMove) and for a
    // purely speculative legality scan (GamePlay.simulateMove, used by
    // getRemainingValidMoves to compute the valid-move dots shown after selecting a
    // piece). It used to set the rook's hasMoved unconditionally, so merely selecting
    // the king — before making any real move — permanently broke castling.

    @Test
    fun getRemainingValidMoves_scanningCastlingSquare_doesNotPoisonRookHasMoved() {
        val king = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val rook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        place(king, 7, 4)
        place(rook, 7, 7)
        place(King(color = Color.BLACK, position = Position(0, 4), isKilled = false), 0, 4)
        game.currentTurn.value = player1

        val validMoves = game.getRemainingValidMoves(Position(7, 4), player1, board)

        assertTrue(
            "King-side castling square should be offered as a valid move",
            validMoves.contains(Position(7, 6))
        )
        assertFalse(
            "Merely computing valid moves must not mark the rook as having moved",
            rook.hasMoved
        )
        assertTrue(
            "Castling must still be legal after the scan",
            canCastle(king, 7, 4, 6)
        )
    }

    // ── Black castling ────────────────────────────────────────────────────────

    @Test
    fun blackKingSideCastle_clearedPath_allowed() {
        val king = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        val rook = Rook(color = Color.BLACK, position = Position(0, 7), isKilled = false)
        place(king, 0, 4)
        place(rook, 0, 7)
        place(King(color = Color.WHITE, position = Position(7, 4), isKilled = false), 7, 4)

        assertTrue("Black king-side castling should be allowed with clear path",
            canCastle(king, 0, 4, 6))
    }

    @Test
    fun blackQueenSideCastle_clearedPath_allowed() {
        val king = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        val rook = Rook(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        place(king, 0, 4)
        place(rook, 0, 0)
        place(King(color = Color.WHITE, position = Position(7, 4), isKilled = false), 7, 4)

        assertTrue("Black queen-side castling should be allowed with clear path",
            canCastle(king, 0, 4, 2))
    }

    @Test
    fun blackKingSideCastle_kingLandsOnG8_rookOnF8() {
        val king = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        val rook = Rook(color = Color.BLACK, position = Position(0, 7), isKilled = false)
        place(king, 0, 4)
        place(rook, 0, 7)

        doCastle(king, 0, 4, 6)

        assertNull("e8 should be empty",        pieceAt(0, 4))
        assertTrue("g8 should have black king", pieceAt(0, 6) is King)
        assertTrue("f8 should have black rook", pieceAt(0, 5) is Rook)
        assertNull("h8 should be empty",        pieceAt(0, 7))
    }

    @Test
    fun blackQueenSideCastle_kingLandsOnC8_rookOnD8() {
        val king = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        val rook = Rook(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        place(king, 0, 4)
        place(rook, 0, 0)

        doCastle(king, 0, 4, 2)

        assertNull("e8 should be empty",        pieceAt(0, 4))
        assertTrue("c8 should have black king", pieceAt(0, 2) is King)
        assertTrue("d8 should have black rook", pieceAt(0, 3) is Rook)
        assertNull("a8 should be empty",        pieceAt(0, 0))
    }

    // ── After castling, no extra pieces moved ─────────────────────────────────

    @Test
    fun kingSideCastle_noOtherPiecesAffected() {
        val king   = King(color = Color.WHITE,   position = Position(7, 4), isKilled = false)
        val rook   = Rook(color = Color.WHITE,   position = Position(7, 7), isKilled = false)
        val pawn1  = Pawn(color = Color.WHITE,   position = Position(6, 0), isKilled = false)
        val pawn2  = Pawn(color = Color.WHITE,   position = Position(6, 7), isKilled = false)
        place(king,  7, 4)
        place(rook,  7, 7)
        place(pawn1, 6, 0)
        place(pawn2, 6, 7)

        doCastle(king, 7, 4, 6)

        // Pawns should be untouched
        assertTrue("a2 pawn should be untouched", pieceAt(6, 0) is Pawn)
        assertTrue("h2 pawn should be untouched", pieceAt(6, 7) is Pawn)
        // Only king (g1) and rook (f1) changed
        assertNull("e1 empty",  pieceAt(7, 4))
        assertNull("h1 empty",  pieceAt(7, 7))
        assertTrue(pieceAt(7, 6) is King)
        assertTrue(pieceAt(7, 5) is Rook)
    }

    @Test
    fun queenSideCastle_noOtherPiecesAffected() {
        val king   = King(color = Color.WHITE,  position = Position(7, 4), isKilled = false)
        val rook   = Rook(color = Color.WHITE,  position = Position(7, 0), isKilled = false)
        val pawn1  = Pawn(color = Color.WHITE,  position = Position(6, 0), isKilled = false)
        val pawn2  = Pawn(color = Color.WHITE,  position = Position(6, 4), isKilled = false)
        place(king,  7, 4)
        place(rook,  7, 0)
        place(pawn1, 6, 0)
        place(pawn2, 6, 4)

        doCastle(king, 7, 4, 2)

        // Pawns should be untouched
        assertTrue("a2 pawn untouched", pieceAt(6, 0) is Pawn)
        assertTrue("e2 pawn untouched", pieceAt(6, 4) is Pawn)
        // Only king (c1) and rook (d1) changed
        assertNull("e1 empty", pieceAt(7, 4))
        assertNull("a1 empty", pieceAt(7, 0))
        assertTrue(pieceAt(7, 2) is King)
        assertTrue(pieceAt(7, 3) is Rook)
    }
}
