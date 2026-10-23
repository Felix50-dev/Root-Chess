package com.example.chess

import androidx.test.core.app.ApplicationProvider
import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Move
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.repository.OpeningBookRepository
import com.example.chess.data.repository.TacticsRecognizer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TacticsRecognizerTest {

    private lateinit var gamePlay: GamePlay
    private lateinit var recognizer: TacticsRecognizer

    private val whitePlayer = Player(isWhite = true, playerType = PlayerType.HUMAN)
    private val blackPlayer = Player(isWhite = false, playerType = PlayerType.HUMAN)

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        gamePlay = GamePlay(Board(), StockfishEngine(), OpeningBookRepository(context))
        recognizer = TacticsRecognizer(gamePlay)
    }

    private fun createEmptyBoard(): Board {
        val board = Board()
        for (r in 0..7) {
            for (c in 0..7) {
                board.getBox(r, c).chessPiece = null
            }
        }
        return board
    }

    @Test
    fun isFork_knightForksRookAndQueen_returnsForkTactic() {
        val board = createEmptyBoard()
        val whiteKnight = com.example.chess.data.model.Knight(
            color = Color.WHITE,
            position = Position(3, 3),
            isKilled = false
        )
        val blackQueen = Queen(
            color = Color.BLACK,
            position = Position(0, 4),
            isKilled = false
        )
        val blackRook = Rook(
            color = Color.BLACK,
            position = Position(0, 0),
            isKilled = false
        )

        board.getBox(3, 3).chessPiece = whiteKnight
        board.getBox(0, 4).chessPiece = blackQueen
        board.getBox(0, 0).chessPiece = blackRook

        val move = Move(
            player = whitePlayer,
            from = board.getBox(3, 3),
            to = board.getBox(1, 2), // c7
            pieceMoved = whiteKnight
        )

        val tactic = recognizer.isFork(board, move, whitePlayer)

        assertNotNull(tactic)
        assertEquals("Fork", tactic?.name)
    }

    @Test
    fun isPieceHangingForFree_pawnAttacksQueen_returnsTrueImmediately() {
        val board = createEmptyBoard()
        val whiteQueen = Queen(color = Color.WHITE, position = Position(4, 4), isKilled = false)
        val blackPawn = Pawn(color = Color.BLACK, position = Position(3, 3), isKilled = false)

        board.getBox(4, 4).chessPiece = whiteQueen
        board.getBox(3, 3).chessPiece = blackPawn

        // White Queen is attacked by Black Pawn (value 1 < 9).
        // Since Pawn (1) < Queen (9), it should return true (hanging for free/profitably capturable).
        val isFork = recognizer.isFork(
            board,
            Move(
                player = blackPlayer,
                from = board.getBox(3, 3),
                to = board.getBox(3, 3),
                pieceMoved = blackPawn
            ),
            blackPlayer
        )
        assertNull(isFork)
    }

    @Test
    fun isPin_rookPinsKnightAgainstKing_returnsPinTactic() {
        val board = createEmptyBoard()
        val whiteRook = Rook(color = Color.WHITE, position = Position(5, 7), isKilled = false)
        val blackKnight = com.example.chess.data.model.Knight(color = Color.BLACK, position = Position(7, 4), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(7, 0), isKilled = false)

        board.getBox(5, 7).chessPiece = whiteRook
        board.getBox(7, 4).chessPiece = blackKnight
        board.getBox(7, 0).chessPiece = blackKing

        val move = Move(
            player = whitePlayer,
            from = board.getBox(5, 7),
            to = board.getBox(7, 7), // moves to h8/h1
            pieceMoved = whiteRook
        )

        val tactic = recognizer.isPin(board, move, whitePlayer)

        assertNotNull(tactic)
        assertEquals("Pin", tactic?.name)
    }
}
