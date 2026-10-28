package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
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
import org.mockito.Mockito

class TacticsRecognizerTest {

    private lateinit var board: Board
    private lateinit var game: GamePlay
    private lateinit var recognizer: TacticsRecognizer

    private val whitePlayer = Player(isWhite = true, playerType = PlayerType.HUMAN)
    private val blackPlayer = Player(isWhite = false, playerType = PlayerType.HUMAN)

    @Before
    fun setup() {
        board = Board()
        val mockOpeningBook = Mockito.mock(OpeningBookRepository::class.java)
        val mockStockfish = Mockito.mock(StockfishEngine::class.java)
        game = GamePlay(board, mockStockfish, mockOpeningBook)
        game.init(whitePlayer, blackPlayer, true)
        // Clear default board pieces after init for targeted test setups
        for (r in 0..7) {
            for (c in 0..7) {
                board.getBox(r, c).chessPiece = null
            }
        }
        recognizer = TacticsRecognizer(game)
    }

    @Test
    fun testKnightForkOnQueenAndRook() {
        // White Knight on c3 (5, 2), moves to c7 (1, 2)
        // Black King on e8 (0, 4), Queen on d5 (3, 3), Rook on a8 (0, 0)
        val whiteKnight = Knight(color = Color.WHITE, position = Position(5, 2), isKilled = false)
        val blackQueen = Queen(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        val blackRook = Rook(color = Color.BLACK, position = Position(0, 0), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)

        board.getBox(5, 2).chessPiece = whiteKnight
        board.getBox(3, 3).chessPiece = blackQueen
        board.getBox(0, 0).chessPiece = blackRook
        board.getBox(0, 4).chessPiece = blackKing

        val move = Move(
            player = whitePlayer,
            from = board.getBox(5, 2),
            to = board.getBox(1, 2), // Nc7 attacks a8 Rook and d5 Queen
            pieceMoved = whiteKnight
        )

        val tactic = recognizer.isFork(board, move, whitePlayer)
        assertNotNull("Expected fork tactic", tactic)
        assertEquals("Fork", tactic?.name)
    }

    @Test
    fun testBishopPinOnKnightAgainstKing() {
        // White Bishop on e2 (6, 4), moves to h5 (3, 7)
        // Black Knight on f7 (1, 5), Black King on e8 (0, 4)
        // White Pawn on g4 (4, 6) defends h5
        val whiteBishop = Bishop(color = Color.WHITE, position = Position(6, 4), isKilled = false)
        val whitePawn = Pawn(color = Color.WHITE, position = Position(4, 6), isKilled = false)
        val blackKnight = Knight(color = Color.BLACK, position = Position(1, 5), isKilled = false)
        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)

        board.getBox(6, 4).chessPiece = whiteBishop
        board.getBox(4, 6).chessPiece = whitePawn
        board.getBox(1, 5).chessPiece = blackKnight
        board.getBox(0, 4).chessPiece = blackKing

        val move = Move(
            player = whitePlayer,
            from = board.getBox(6, 4),
            to = board.getBox(3, 7), // Bh5 pins Nf7 against Ke8 along (3,7)-(2,6)-(1,5)-(0,4)
            pieceMoved = whiteBishop
        )

        val tactic = recognizer.isPin(board, move, whitePlayer)
        assertNotNull("Expected pin tactic", tactic)
        assertEquals("Pin", tactic?.name)
    }

    @Test
    fun testSkewerQueenAgainstRook() {
        // White Bishop on e2 (6, 4), moves to h1 (7, 7)
        // White Rook on h8 (0, 7) defends h1
        // Black Queen on d5 (3, 3), Black Rook on b7 (1, 1)
        // Diagonal: h1 (7,7) -> d5 (3,3) -> b7 (1,1)
        val whiteBishop = Bishop(color = Color.WHITE, position = Position(6, 4), isKilled = false)
        val whiteRook = Rook(color = Color.WHITE, position = Position(0, 7), isKilled = false)
        val blackQueen = Queen(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        val blackRook = Rook(color = Color.BLACK, position = Position(1, 1), isKilled = false)

        board.getBox(6, 4).chessPiece = whiteBishop
        board.getBox(0, 7).chessPiece = whiteRook
        board.getBox(3, 3).chessPiece = blackQueen
        board.getBox(1, 1).chessPiece = blackRook

        val move = Move(
            player = whitePlayer,
            from = board.getBox(6, 4),
            to = board.getBox(7, 7), // Bh1 skewering Queen on d5 against Rook on b7
            pieceMoved = whiteBishop
        )

        val tactic = recognizer.isSkewer(board, move)
        assertNotNull("Expected skewer tactic", tactic)
        assertEquals("Skewer", tactic?.name)
    }

    @Test
    fun testNoTacticOnQuietMove() {
        val whitePawn = Pawn(color = Color.WHITE, position = Position(6, 4), isKilled = false)
        board.getBox(6, 4).chessPiece = whitePawn

        val move = Move(
            player = whitePlayer,
            from = board.getBox(6, 4),
            to = board.getBox(5, 4),
            pieceMoved = whitePawn
        )

        assertNull(recognizer.isFork(board, move, whitePlayer))
        assertNull(recognizer.isPin(board, move, whitePlayer))
        assertNull(recognizer.isSkewer(board, move))
    }
}
