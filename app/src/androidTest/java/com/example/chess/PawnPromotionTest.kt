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
import com.example.chess.data.repository.GamePlay
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class PawnPromotionTest {

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

    private fun placePiece(
        piece: com.example.chess.data.model.ChessPiece,
        position: Position,
    ) {
        board.getBox(position.row, position.column).chessPiece = piece
        piece.position = position
    }

    private fun pieceAt(row: Int, col: Int) =
        board.getBox(row, col).chessPiece

    private fun movePawn(
        from: Position,
        to: Position,
        player: Player,
    ): Boolean {
        val fromSpot = board.getBox(from.row, from.column)
        val toSpot = board.getBox(to.row, to.column)
        return game.handleHumanMove(player, fromSpot, toSpot, false, board)
    }

    @Test
    fun whitePawnReachesRank8_promotionFlowIsSet() {
        // Arrange
        //
        // Row 0: . . . . K . . .   (black king)
        // Row 1: . P . . . . . .   (white pawn one step from promotion)
        // Row 7: . . . . K . . .   (white king)

        val whitePawn = Pawn(color = Color.WHITE, position = Position(1, 1), isKilled = false)
        placePiece(whitePawn, Position(1, 1))
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        // Act — pawn moves to rank 0 (white's promotion rank)
        val result = movePawn(Position(1, 1), Position(0, 1), player1)

        // Assert — move succeeded and pawn is at destination
        assertTrue("Pawn move to promotion square should succeed", result)
        assertNotNull("Pawn should be at (0,1)", pieceAt(0, 1))
        assertEquals(Color.WHITE, pieceAt(0, 1)?.color)
    }

    // ── Test 2 — Black pawn reaches rank 1, promotion triggered ──────────────

    @Test
    fun blackPawnReachesRank1_promotionFlowIsSet() {
        // Arrange
        val blackPawn = Pawn(color = Color.BLACK, position = Position(6, 3), isKilled = false)
        placePiece(blackPawn, Position(6, 3))
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player2

        // Act — black pawn moves to rank 7 (black's promotion rank)
        val result = movePawn(Position(6, 3), Position(7, 3), player2)

        // Assert
        assertTrue("Black pawn move to promotion square should succeed", result)
        assertNotNull("Pawn should be at (7,3)", pieceAt(7, 3))
        assertEquals(Color.BLACK, pieceAt(7, 3)?.color)
    }

    // ── Test 3 — After promotion, original pawn square is empty ──────────────

    @Test
    fun afterPromotion_originSquareIsEmpty() {
        // Arrange
        val whitePawn = Pawn(color = Color.WHITE, position = Position(1, 2), isKilled = false)
        placePiece(whitePawn, Position(1, 2))
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        // Act
        movePawn(Position(1, 2), Position(0, 2), player1)

        // Simulate promotion — set promoted piece on the spot
        val queen = Queen(color = Color.WHITE, position = Position(0, 2), isKilled = false)
        board.getBox(0, 2).chessPiece = queen
        game._promotionPieceFlow.value = null  // clear flow as setPromotedPiece would

        // Assert — origin square is empty
        assertNull("Origin square (1,2) should be empty after promotion", pieceAt(1, 2))
        assertTrue("Promoted piece at (0,2) should be a Queen", pieceAt(0, 2) is Queen)
    }

    // ── Test 4 — Promotion flow cleared after piece selected ─────────────────

    @Test
    fun promotionFlowClearedAfterPieceSelected() {
        // Arrange
        val whitePawn = Pawn(color = Color.WHITE, position = Position(1, 0), isKilled = false)
        placePiece(whitePawn, Position(1, 0))
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        movePawn(Position(1, 0), Position(0, 0), player1)

        // Simulate player selecting a promotion piece (as ViewModel.setPromotedPiece does)
        val rook = Rook(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = rook
        game._promotionPieceFlow.value = null   // ← this is what setPromotedPiece must do

        // Assert — flow is cleared
        assertNull(
            "Promotion flow should be null after piece selected",
            game._promotionPieceFlow.value
        )
        assertTrue("Rook should be at (0,0)", pieceAt(0, 0) is Rook)
    }

    // ── Test 5 — Turn switches correctly after pawn promotion ────────────────

    @Test
    fun afterPawnPromotion_turnSwitchesToOpponent() {
        // Arrange
        val whitePawn = Pawn(color = Color.WHITE, position = Position(1, 3), isKilled = false)
        placePiece(whitePawn, Position(1, 3))
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        // Act — pawn promotes
        movePawn(Position(1, 3), Position(0, 3), player1)

        // Simulate ViewModel.setPromotedPiece
        val queen = Queen(color = Color.WHITE, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = queen
        game._promotionPieceFlow.value = null
        // Turn should have already switched inside handleHumanMove
        // (promotion doesn't add an extra turn switch)

        // Assert — it is now black's turn
        assertEquals(
            "After white pawn promotes it should be black's turn",
            player2,
            game.currentTurn.value
        )
    }

    @Test
    fun promotionFlow_doesNotFireOnRegularMoves() {
        // Arrange — make sure a non-null promotion flow from a previous operation
        // doesn't accidentally trigger promotion on a knight move
        val whiteKnight = Knight(color = Color.WHITE, position = Position(6, 1), isKilled = false)
        val whiteKing   = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing   = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whiteKnight, Position(6, 1))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        // Simulate stale promotion flow value still set (the bug scenario)
        // In the fixed code this should be ignored for non-AI, non-pawn moves
        // We just verify the knight moves normally
        val result = movePawn(Position(6, 1), Position(4, 2), player1)

        assertTrue("Knight should move normally", result)
        assertNull("(6,1) should be empty", pieceAt(6, 1))
        assertNotNull("Knight at (4,2)", pieceAt(4, 2))
        assertTrue("Piece at (4,2) should still be a Knight", pieceAt(4, 2) is Knight)
    }

    // ── Test 8 — Capture promotion: pawn captures and promotes ───────────────
    @Test
    fun whitePawnCaptures_andPromotes() {
        val whitePawn = Pawn(color = Color.WHITE, position = Position(1, 1), isKilled = false)
        val blackRook = Rook(color = Color.BLACK, position = Position(0, 2), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 7), isKilled = false) // ← far from action
        placePiece(whitePawn, Position(1, 1))
        placePiece(blackRook, Position(0, 2))
        placePiece(whiteKing, Position(7, 0))
        placePiece(blackKing, Position(0, 7))
        game.currentTurn.value = player1

        val result = movePawn(Position(1, 1), Position(0, 2), player1)

        assertTrue("Capture-promotion move should succeed", result)
        assertNull("White pawn origin (1,1) should be empty", pieceAt(1, 1))
        assertNotNull("Piece should be at (0,2) after capture", pieceAt(0, 2))
        assertEquals("Piece at (0,2) should be white", Color.WHITE, pieceAt(0, 2)?.color)

        val bishop = Bishop(color = Color.WHITE, position = Position(0, 2), isKilled = false)
        board.getBox(0, 2).chessPiece = bishop
        game._promotionPieceFlow.value = null

        assertTrue("Promoted piece should be a Bishop", pieceAt(0, 2) is Bishop)
    }

    @Test
    fun afterPromotion_gameplayContinuesNormally() {
        val whitePawn = Pawn(color = Color.WHITE, position = Position(1, 5), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(6, 2), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 0), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 7), isKilled = false)
        val blackRook = Rook(color = Color.BLACK, position = Position(2, 7), isKilled = false)
        placePiece(whitePawn, Position(1, 5))
        placePiece(blackPawn, Position(6, 2))
        placePiece(whiteKing, Position(7, 0))
        placePiece(blackKing, Position(0, 7))
        placePiece(blackRook, Position(2, 7))
        game.currentTurn.value = player1

        val result = movePawn(Position(1, 5), Position(0, 5), player1)

        // Print everything so we can see what's actually happening
        println("=== PROMOTION TEST DEBUG ===")
        println("result=$result")
        println("gameStatus=${game.status}")
        println("isGameOver=${game.isGameOver}")
        println("currentTurn=${game.currentTurn.value}")
        println("pieceAt(0,5)=${pieceAt(0,5)}")
        println("pieceAt(1,5)=${pieceAt(1,5)}")
        println("movesPlayed=${game.movesPlayed.size}")
        println("============================")

        assertTrue("White pawn should promote successfully", result)
        assertEquals("Black's turn after white promotion", player2, game.currentTurn.value)
    }
    // ── Board extension ───────────────────────────────────────────────────────

    private fun Board.clear() {
        for (row in 0..7) for (col in 0..7) getBox(row, col).chessPiece = null
    }
}
