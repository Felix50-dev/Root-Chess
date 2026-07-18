package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.GameMode
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.repository.GamePlay
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test

/**
 * ChessGameViewModel.undoMove()/redoMove() used to gate "undo both the AI's reply and the
 * human move together" on `playerWhite.playerType == AI || playerDark.playerType == AI` —
 * templates hard-coded to PlayerType.HUMAN and never reassigned, so that condition could
 * never be true. The fix checks `_gameMode.value == GameMode.COMPUTER` instead.
 *
 * The ViewModel itself can't be constructed here (its init block starts a real Stockfish
 * subprocess and needs SoundPool/Context wiring), so this exercises the exact corrected
 * branch against a real GamePlay move history to verify the actual behavioral consequence:
 * how many plies a single Undo reverts.
 */
class UndoRedoModeLogicTest {

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

    // Mirrors ChessGameViewModel.undoMove()'s branch exactly, post-fix.
    private fun undoMoveForMode(gameMode: GameMode) {
        if (gameMode == GameMode.COMPUTER) {
            if (game.movesPlayed.isNotEmpty()) game.undoMoveByPlayer(game.movesPlayed.last())
            if (game.movesPlayed.isNotEmpty()) game.undoMoveByPlayer(game.movesPlayed.last())
        } else {
            game.undoMoveByPlayer(game.movesPlayed.last())
            game.changeCurrentTurn()
        }
    }

    // Sets up two real, independently-played plies (white then black) to undo from.
    private fun playTwoMoves() {
        val whiteKnight = Knight(color = Color.WHITE, position = Position(7, 1), isKilled = false)
        val blackKnight = Knight(color = Color.BLACK, position = Position(0, 1), isKilled = false)
        val whiteKing = King(color = Color.WHITE, position = Position(7, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        placePiece(whiteKnight, Position(7, 1))
        placePiece(blackKnight, Position(0, 1))
        placePiece(whiteKing, Position(7, 4))
        placePiece(blackKing, Position(0, 4))
        game.currentTurn.value = player1

        assertEquals(true, makeMove(whiteKnight, Position(7, 1), Position(5, 2), player1))
        assertEquals(true, makeMove(blackKnight, Position(0, 1), Position(2, 2), player2))
        assertEquals(2, game.movesPlayed.size)
    }

    @Test
    fun computerMode_undo_revertsBothTheAiReplyAndThePlayerMoveTogether() {
        playTwoMoves()

        undoMoveForMode(GameMode.COMPUTER)

        assertEquals(
            "A single Undo in COMPUTER mode must revert both plies (the AI's reply and " +
                "the human move that triggered it) — this was impossible before the fix, " +
                "since the AI-detection condition could never be true",
            0,
            game.movesPlayed.size
        )
    }

    @Test
    fun friendMode_undo_revertsOnlyTheSingleLastMove() {
        playTwoMoves()

        undoMoveForMode(GameMode.FRIEND)

        assertEquals(
            "A single Undo outside COMPUTER mode must revert only the last move",
            1,
            game.movesPlayed.size
        )
    }

    // ── Board extension ───────────────────────────────────────────────────────

    private fun Board.clear() {
        for (row in 0..7) for (col in 0..7) getBox(row, col).chessPiece = null
    }
}
