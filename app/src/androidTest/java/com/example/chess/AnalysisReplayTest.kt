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
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.repository.GamePlay
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Covers GamePlay.convertToMoveObject()/applyMove() — the pair used to replay a game's
 * move-string history onto an isolated analysis board for TacticsRecognizer/capture-
 * opportunity/missed-mate detection. Previously castling, en passant, and promotion were
 * not detected on the returned Move and not applied correctly, silently corrupting the
 * replay board for the rest of any analyzed game containing one of them.
 */
class AnalysisReplayTest {

    private lateinit var board: Board
    private lateinit var game: GamePlay
    private lateinit var player1: Player  // white
    private lateinit var player2: Player  // black

    @Before
    fun setup() {
        board = Board()
        val stockfish = StockfishEngine()
        game = GamePlay(board, stockfish)
        player1 = Player(isWhite = true, PlayerType.HUMAN)
        player2 = Player(isWhite = false, PlayerType.HUMAN)
        game.init(player1, player2, true)
        board.clear()
    }

    private fun placePiece(piece: com.example.chess.data.model.ChessPiece, position: Position) {
        board.getBox(position.row, position.column).chessPiece = piece
    }

    private fun pieceAt(row: Int, col: Int) = board.getBox(row, col).chessPiece

    // ── Castling ─────────────────────────────────────────────────────────────

    @Test
    fun convertToMoveObject_detectsCastling_andApplyMoveMovesBothPieces() {
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val whiteRook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(whiteRook, Position(7, 7))

        val move = game.convertToMoveObject("e1g1", board, player1)
        assertTrue("King-side castle must be detected as a castling move", move.isCastlingMove)

        game.applyMove(move, board)

        assertEquals("King should land on g1", whiteKing, pieceAt(7, 6))
        assertEquals("Rook should land on f1", whiteRook, pieceAt(7, 5))
        assertNull("e1 should be empty", pieceAt(7, 4))
        assertNull("h1 should be empty", pieceAt(7, 7))
    }

    // ── En passant ───────────────────────────────────────────────────────────

    @Test
    fun convertToMoveObject_detectsEnPassant_andRecordsTheActualCapturedPawn() {
        val whitePawn = Pawn(color = Color.WHITE, position = Position(3, 4), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        placePiece(whitePawn, Position(3, 4))
        placePiece(blackPawn, Position(3, 3))
        board.enPassantTarget = Position(2, 3)

        val move = game.convertToMoveObject("e5d6", board, player1)
        assertTrue("Diagonal move onto the en passant target must be detected", move.isEnPassant)
        assertEquals(
            "pieceKilled must be the pawn beside the mover, not whatever sits on the (empty) destination",
            blackPawn,
            move.pieceKilled
        )

        game.applyMove(move, board)

        assertEquals("White pawn should land on d6", whitePawn, pieceAt(2, 3))
        assertNull("Captured black pawn's square must be cleared", pieceAt(3, 3))
        assertNull("e5 should be empty", pieceAt(3, 4))
    }

    @Test
    fun convertToMoveObject_normalDiagonalCapture_isNotMisdetectedAsEnPassant() {
        // Same move string shape as the en passant test, but the destination is occupied
        // by the actual captured piece (a normal capture), and there's no en passant
        // target armed — must NOT be misclassified as en passant.
        val whitePawn = Pawn(color = Color.WHITE, position = Position(3, 4), isKilled = false)
        val blackKnight = Knight(color = Color.BLACK, position = Position(2, 3), isKilled = false)
        placePiece(whitePawn, Position(3, 4))
        placePiece(blackKnight, Position(2, 3))
        board.enPassantTarget = null

        val move = game.convertToMoveObject("e5d6", board, player1)

        assertFalse("A normal diagonal capture must not be flagged as en passant", move.isEnPassant)
        assertEquals("pieceKilled should be the piece actually on the destination", blackKnight, move.pieceKilled)
    }

    // ── Promotion ────────────────────────────────────────────────────────────

    @Test
    fun convertToMoveObject_detectsPromotion_andApplyMovePlacesThePromotedPiece() {
        val whitePawn = Pawn(color = Color.WHITE, position = Position(1, 4), isKilled = false)
        placePiece(whitePawn, Position(1, 4))

        val move = game.convertToMoveObject("e7e8q", board, player1)
        assertTrue("Promotion piece should be constructed", move.promotionPiece is Queen)

        game.applyMove(move, board)

        val promoted = pieceAt(0, 4)
        assertTrue("Destination must hold the promoted Queen, not the original pawn", promoted is Queen)
        assertEquals(Color.WHITE, promoted?.color)
        assertNull("e7 should be empty after promotion", pieceAt(1, 4))
    }

    // ── End-to-end: castling, en passant, and promotion in the same replay ─

    @Test
    fun sequentialReplay_ofCastlingEnPassantAndPromotion_doesNotCorruptTheBoard() {
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val whiteRook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        val whiteEpPawn = Pawn(color = Color.WHITE, position = Position(3, 4), isKilled = false)
        val blackEpPawn = Pawn(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        val whitePromoPawn = Pawn(color = Color.WHITE, position = Position(1, 0), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 7), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(whiteRook, Position(7, 7))
        placePiece(whiteEpPawn, Position(3, 4))
        placePiece(blackEpPawn, Position(3, 3))
        placePiece(whitePromoPawn, Position(1, 0))
        placePiece(blackKing, Position(0, 7))
        board.enPassantTarget = Position(2, 3)

        // 1. En passant capture.
        val epMove = game.convertToMoveObject("e5d6", board, player1)
        assertTrue(epMove.isEnPassant)
        game.applyMove(epMove, board)
        assertEquals(whiteEpPawn, pieceAt(2, 3))
        assertNull(pieceAt(3, 3))

        // 2. King-side castle — proves step 1 didn't disturb unrelated squares/state.
        val castleMove = game.convertToMoveObject("e1g1", board, player1)
        assertTrue(castleMove.isCastlingMove)
        game.applyMove(castleMove, board)
        assertEquals(whiteKing, pieceAt(7, 6))
        assertEquals(whiteRook, pieceAt(7, 5))

        // 3. Promotion — proves the earlier two moves left the board in a valid,
        //    continuable state for the rest of the analysis replay.
        val promoMove = game.convertToMoveObject("a7a8q", board, player1)
        assertTrue(promoMove.promotionPiece is Queen)
        game.applyMove(promoMove, board)
        val promoted = pieceAt(0, 0)
        assertTrue(promoted is Queen)
        assertEquals(Color.WHITE, promoted?.color)

        // Everything from the earlier steps must still be exactly where it should be.
        assertEquals("Castled king should still be on g1", whiteKing, pieceAt(7, 6))
        assertEquals("Castled rook should still be on f1", whiteRook, pieceAt(7, 5))
        assertEquals("En passant pawn should still be on d6", whiteEpPawn, pieceAt(2, 3))
        assertNotNull("Black king should be untouched", pieceAt(0, 7))
    }

    // ── Board extension ───────────────────────────────────────────────────────

    private fun Board.clear() {
        for (row in 0..7) for (col in 0..7) getBox(row, col).chessPiece = null
    }
}
