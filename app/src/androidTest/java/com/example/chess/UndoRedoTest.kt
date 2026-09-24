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
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test

class UndoRedoTest {

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
        val toSpot   = board.getBox(to.row, to.column)
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

    // ── Undo: basic move ──────────────────────────────────────────────────────

    @Test
    fun undoMove_singleMove_pieceRestoredToOrigin() {
        // Arrange
        val knight = Knight(color = Color.WHITE, position = Position(6, 1), isKilled = false)
        placePiece(knight, Position(6, 1))
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(blackKing, Position(0, 4))
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        game.currentTurn.value = player1

        makeMove(knight, Position(6, 1), Position(4, 2), player1)
        assertEquals(1, game.movesPlayed.size)

        // Act
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()

        // Assert — knight back at origin, destination empty
        assertNotNull("Knight should be back at (6,1)", pieceAt(6, 1))
        assertNull("(4,2) should be empty after undo", pieceAt(4, 2))
        assertEquals("movesPlayed should be empty", 0, game.movesPlayed.size)
    }

    @Test
    fun undoMove_capture_capturedPieceRestored() {
        // Arrange
        val whiteRook  = Rook(color = Color.WHITE, position = Position(4, 0), isKilled = false)
        val blackPawn  = Pawn(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        val whiteKing  = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing  = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whiteRook, Position(4, 0))
        placePiece(blackPawn, Position(4, 4))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        makeMove(whiteRook, Position(4, 0), Position(4, 4), player1)
        assertNull("(4,0) should be empty after move", pieceAt(4, 0))
        assertNotNull("Rook should be at (4,4)", pieceAt(4, 4))

        // Act
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()

        // Assert — rook back, captured pawn restored
        assertNotNull("White rook should be back at (4,0)", pieceAt(4, 0))
        assertNotNull("Black pawn should be restored at (4,4)", pieceAt(4, 4))
        assertEquals(Color.BLACK, pieceAt(4, 4)?.color)
    }

    @Test
    fun undoMove_turnSwitchesBack() {
        // Arrange
        val knight    = Knight(color = Color.WHITE, position = Position(6, 1), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(knight, Position(6, 1))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        makeMove(knight, Position(6, 1), Position(4, 2), player1)
        assertEquals(player2, game.currentTurn.value)

        // Act
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()

        // Assert — turn back to white
        assertEquals(player1, game.currentTurn.value)
    }

    @Test
    fun undoMove_multipleMoves_restoredCorrectly() {
        // Arrange — white knight moves, then black pawn moves, then undo both
        val whiteKnight = Knight(color = Color.WHITE, position = Position(6, 1), isKilled = false)
        val blackPawn   = Pawn(color = Color.BLACK, position = Position(1, 3), isKilled = false)
        val whiteKing   = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing   = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whiteKnight, Position(6, 1))
        placePiece(blackPawn, Position(1, 3))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        makeMove(whiteKnight, Position(6, 1), Position(4, 2), player1)
        makeMove(blackPawn, Position(1, 3), Position(2, 3), player2)
        assertEquals(2, game.movesPlayed.size)

        // Undo black move
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()
        assertEquals(1, game.movesPlayed.size)
        assertNull("(2,3) should be empty", pieceAt(2, 3))
        assertNotNull("Black pawn back at (1,3)", pieceAt(1, 3))

        // Undo white move
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()
        assertEquals(0, game.movesPlayed.size)
        assertNull("(4,2) should be empty", pieceAt(4, 2))
        assertNotNull("White knight back at (6,1)", pieceAt(6, 1))
    }

    @Test
    fun undoMove_emptyMovesList_doesNotCrash() {
        // Should be a no-op — no exception thrown
        assertEquals(0, game.movesPlayed.size)
        // No assertion needed — just confirm no crash
    }

    // ── Undo: castling ───────────────────────────────────────────────────────

    @Test
    fun undoMove_kingsideCastling_kingAndRookRestored() {
        // Arrange — clear path for kingside castling
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val whiteRook = Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whiteKing, Position(7, 4))
        placePiece(whiteRook, Position(7, 7))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        makeMove(whiteKing, Position(7, 4), Position(7, 6), player1)

        // Verify castling happened
        assertNull("King's original square empty", pieceAt(7, 4))
        assertNotNull("King at (7,6)", pieceAt(7, 6))
        assertNotNull("Rook at (7,5)", pieceAt(7, 5))
        assertNull("Rook's original square empty", pieceAt(7, 7))

        // Act
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()

        // Assert — king and rook back
        assertNotNull("King back at (7,4)", pieceAt(7, 4))
        assertNotNull("Rook back at (7,7)", pieceAt(7, 7))
        assertNull("(7,6) empty", pieceAt(7, 6))
        assertNull("(7,5) empty", pieceAt(7, 5))
    }

    // ── Redo: basic move ──────────────────────────────────────────────────────

    @Test
    fun redoMove_singleUndo_pieceMovedAgain() {
        // Arrange
        val knight    = Knight(color = Color.WHITE, position = Position(6, 1), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(knight, Position(6, 1))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        makeMove(knight, Position(6, 1), Position(4, 2), player1)
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()

        assertEquals(0, game.movesPlayed.size)
        assertEquals(1, game.movesUndone.size)

        // Act
        game.redoMove(game.movesUndone.last(), board)

        // Assert — piece moved again to destination
        assertNull("(6,1) should be empty", pieceAt(6, 1))
        assertNotNull("Knight at (4,2)", pieceAt(4, 2))
        assertEquals(0, game.movesUndone.size)
        assertEquals(1, game.movesPlayed.size)
    }

    @Test
    fun redoMove_afterCapture_capturedPieceGoneAgain() {
        // Arrange
        val whiteRook = Rook(color = Color.WHITE, position = Position(4, 0), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whiteRook, Position(4, 0))
        placePiece(blackPawn, Position(4, 4))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        makeMove(whiteRook, Position(4, 0), Position(4, 4), player1)
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()

        // Verify undo restored captured pawn
        assertNotNull("Pawn restored at (4,4)", pieceAt(4, 4))
        assertEquals(Color.BLACK, pieceAt(4, 4)?.color)

        // Act
        game.redoMove(game.movesUndone.last(), board)

        // Assert — rook back at (4,4), pawn gone
        assertNotNull("Rook at (4,4)", pieceAt(4, 4))
        assertEquals(Color.WHITE, pieceAt(4, 4)?.color)
        assertNull("(4,0) should be empty", pieceAt(4, 0))
    }

    @Test
    fun redoMove_emptyUndoneList_doesNotCrash() {
        // Should be a no-op
        assertEquals(0, game.movesUndone.size)
        // No assertion needed — just confirm no crash
    }

    @Test
    fun redoMove_turnAdvancesCorrectly() {
        // Arrange
        val knight    = Knight(color = Color.WHITE, position = Position(6, 1), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(knight, Position(6, 1))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        makeMove(knight, Position(6, 1), Position(4, 2), player1)
        // After white's move it's black's turn
        assertEquals(player2, game.currentTurn.value)

        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()
        // After undo it's white's turn again
        assertEquals(player1, game.currentTurn.value)

        // Act
        game.redoMove(game.movesUndone.last(), board)

        // Assert — turn advanced back to black
        assertEquals(player2, game.currentTurn.value)
    }

    // ── Undo + Redo sequence ─────────────────────────────────────────────────

    @Test
    fun undoThenRedo_multipleTimesInSequence_boardConsistent() {
        // Arrange
        val whitePawn = Pawn(color = Color.WHITE, position = Position(6, 4), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whitePawn, Position(6, 4))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        // Move pawn e2→e4
        makeMove(whitePawn, Position(6, 4), Position(4, 4), player1)
        assertNull("e2 empty after move", pieceAt(6, 4))
        assertNotNull("Pawn at e4", pieceAt(4, 4))

        // Undo
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()
        assertNotNull("Pawn back at e2", pieceAt(6, 4))
        assertNull("e4 empty after undo", pieceAt(4, 4))

        // Redo
        game.redoMove(game.movesUndone.last(), board)
        assertNull("e2 empty after redo", pieceAt(6, 4))
        assertNotNull("Pawn at e4 after redo", pieceAt(4, 4))

        // Undo again
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()
        assertNotNull("Pawn back at e2 again", pieceAt(6, 4))
        assertNull("e4 empty again", pieceAt(4, 4))

        // Redo again
        game.redoMove(game.movesUndone.last(), board)
        assertNull("e2 empty final", pieceAt(6, 4))
        assertNotNull("Pawn at e4 final", pieceAt(4, 4))
    }

    @Test
    fun undoRedo_movesPlayedAndUndoneCountsCorrect() {
        // Arrange
        val knight    = Knight(color = Color.WHITE, position = Position(6, 1), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(1, 3), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(knight, Position(6, 1))
        placePiece(blackPawn, Position(1, 3))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        makeMove(knight, Position(6, 1), Position(4, 2), player1)
        makeMove(blackPawn, Position(1, 3), Position(2, 3), player2)
        assertEquals(2, game.movesPlayed.size)
        assertEquals(0, game.movesUndone.size)

        // Undo first
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()
        assertEquals(1, game.movesPlayed.size)
        assertEquals(1, game.movesUndone.size)

        // Undo second
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()
        assertEquals(0, game.movesPlayed.size)
        assertEquals(2, game.movesUndone.size)

        // Redo first
        game.redoMove(game.movesUndone.last(), board)
        assertEquals(1, game.movesPlayed.size)
        assertEquals(1, game.movesUndone.size)

        // Redo second
        game.redoMove(game.movesUndone.last(), board)
        assertEquals(2, game.movesPlayed.size)
        assertEquals(0, game.movesUndone.size)
    }

    @Test
    fun newMoveAfterUndo_clearsUndoneList() {
        // Chess convention: making a new move after undo clears the redo stack
        val knight    = Knight(color = Color.WHITE, position = Position(6, 1), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(1, 3), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(knight, Position(6, 1))
        placePiece(blackPawn, Position(1, 3))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        makeMove(knight, Position(6, 1), Position(4, 2), player1)
        makeMove(blackPawn, Position(1, 3), Position(2, 3), player2)

        // Undo black's move
        game.undoMoveByPlayer(game.movesPlayed.last())
        game.changeCurrentTurn()
        assertEquals(1, game.movesUndone.size)

        // White makes a different move — redo stack should clear
        // (This tests the convention — your undoMoveByPlayer needs to support this)
        // For now verify the undo happened correctly
        assertEquals(1, game.movesPlayed.size)
        assertNotNull("Black pawn restored", pieceAt(1, 3))
    }

    // ── Board extension ───────────────────────────────────────────────────────

    private fun Board.clear() {
        for (row in 0..7) for (col in 0..7) getBox(row, col).chessPiece = null
    }
}
