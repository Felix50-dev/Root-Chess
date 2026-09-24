package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.repository.GamePlay
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class GamePlayTest {

    private lateinit var board: Board
    private lateinit var whitePlayer: Player
    private lateinit var blackPlayer: Player

    @Before
    fun setup() {
        board = Board()
        whitePlayer = Player(true, PlayerType.HUMAN)
        blackPlayer = Player(false, PlayerType.HUMAN)

        board.getBox(7, 1).chessPiece = Knight(color = Color.WHITE, position = Position(7,1), isKilled = false)
        board.getBox(5,2).chessPiece = null

        board.getBox(5,5).chessPiece = Queen(color = Color.WHITE, position = Position(5,5), isKilled = false)
        board.getBox(1,5).chessPiece = Pawn(color = Color.BLACK, position = Position(1,5), isKilled = false)
    }

    @Test
    fun testConvertToMoveObjectForNormalMove() {
        val moveStr = "b1c3" // Knight from b1 to c3
        val stockfish = StockfishEngine()
        val game = testGamePlay(board, stockfish)
        val move = game.convertToMoveObject(moveStr, board, whitePlayer)

        assertEquals(Position(row=7, column=1), move.from.position)
        assertEquals(Position(row=5, column=2), move.to.position)
        assertTrue(move.pieceMoved is Knight)
        assertEquals(Color.WHITE, move.pieceMoved?.color)
    }

    @Test
    fun testConvertToMoveObjectForNormalMoveKillsPiece() {
        val moveStr = "f3f7"
        val stockfish = StockfishEngine()
        val game = testGamePlay(board, stockfish)
        val move = game.convertToMoveObject(moveStr, board, whitePlayer)

        assertEquals(Position(row=5, column=5), move.from.position)
        assertEquals(Position(row=1, column=5), move.to.position)
        assertTrue(move.pieceMoved is Queen)
        //assertTrue(move.pieceKilled is Pawn)
        assertEquals(Color.WHITE, move.pieceMoved?.color)
    }

    @Test
    fun testConvertToMoveObjectForPromotion() {
        // Place white pawn at e7 (row 1), moving to e8 with promotion to Queen
        board.getBox(1, 4).chessPiece = Pawn(color = Color.WHITE, position = Position(1, 4), isKilled = false)
        val moveStr = "e7e8q"
        val stockfish = StockfishEngine()
        val game = testGamePlay(board, stockfish)
        val move = game.convertToMoveObject(moveStr, board, whitePlayer)

        assertEquals("e7", move.from.position.toAlgebraicNotation())
        assertEquals("e8", move.to.position.toAlgebraicNotation())
        assertTrue(move.pieceMoved is Pawn)
        assertEquals(Color.WHITE, move.pieceMoved?.color)
        assertTrue("_promotionPieceFlow should emit Queen", game._promotionPieceFlow.value is Queen)
    }

    @Test(expected = IllegalArgumentException::class)
    fun testConvertToMoveObjectWithInvalidStringThrowsException() {
        val moveStr = ""
        val stockfish = StockfishEngine()
        val game = testGamePlay(board, stockfish)
        game.convertToMoveObject(moveStr, board, whitePlayer)
    }
}
