package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.King
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Rook
import com.example.chess.data.repository.GamePlay
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class StalemateAndTurnSwitchTest {

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
        clearBoard()
    }

    private fun clearBoard() {
        for (row in 0..7) for (col in 0..7) board.getBox(row, col).chessPiece = null
    }

    private fun place(piece: com.example.chess.data.model.ChessPiece, row: Int, col: Int) {
        board.getBox(row, col).chessPiece = piece
        piece.position = Position(row, col)
    }

    private fun makeMove(from: Position, to: Position, player: Player): Boolean {
        val fromSpot = board.getBox(from.row, from.column)
        val toSpot = board.getBox(to.row, to.column)
        return game.handleHumanMove(player, fromSpot, toSpot, false, board)
    }

    // handleHumanMove() used to gate changeCurrentTurn()/return true on !isGameOver, so
    // a move that delivered checkmate left currentTurn stuck on the mating player. That
    // gate was pointless — handleHumanMove's own isGameOver check already blocks further
    // moves once the game is over — and it broke "Show Board" (resetGameStatus() would
    // let the same player who just mated move again immediately).
    @Test
    fun checkmatingMove_stillSwitchesCurrentTurn() {
        // Row 0: K p . . . . . .   (Black King a8, Black Pawn b8)
        // Row 1: . p . . . . . .   (Black Pawn b7)
        // Row 7: R . . . . . . K   (White Rook a1 pre-move at h1, White King h2)
        place(King(color = Color.BLACK, position = Position(0, 0), isKilled = false), 0, 0)
        place(Pawn(color = Color.BLACK, position = Position(0, 1), isKilled = false), 0, 1)
        place(Pawn(color = Color.BLACK, position = Position(1, 1), isKilled = false), 1, 1)
        place(Rook(color = Color.WHITE, position = Position(7, 7), isKilled = false), 7, 7)
        place(King(color = Color.WHITE, position = Position(6, 7), isKilled = false), 6, 7)
        game.currentTurn.value = player1

        // Rh1-a1: delivers check up the a-file. The king can't escape (a7 is still on
        // the a-file; b7/b8 are occupied by its own pawns) or block/capture the rook.
        val moved = makeMove(Position(7, 7), Position(7, 0), player1)

        assertTrue("The mating move itself must be accepted", moved)
        assertEquals("White delivered the mate, so White wins", GameStatus.WHITE_WIN, game.status)
        assertTrue(game.isGameOver)
        assertEquals(
            "Turn must still switch to the losing player even though the game is over",
            player2,
            game.currentTurn.value
        )
    }
}
