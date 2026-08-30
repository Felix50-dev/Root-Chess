package com.example.chess.data.repository

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class GamePlayTest {

    private lateinit var board: Board
    private lateinit var gamePlay: GamePlay
    private val player1 = Player(name = "WhitePlayer", isWhite = true, playerType = PlayerType.HUMAN)
    private val player2 = Player(name = "BlackPlayer", isWhite = false, playerType = PlayerType.HUMAN)

    @Before
    fun setUp() {
        board = Board()
        gamePlay = GamePlay(board, StockfishEngine())
        gamePlay.init(player1, player2)
    }

    @Test
    fun findKing_findsCorrectKingSpotsOnInitialBoard() {
        val whiteKingSpot = gamePlay.findKing(board, Color.WHITE)
        val blackKingSpot = gamePlay.findKing(board, Color.BLACK)

        assertNotNull(whiteKingSpot)
        assertNotNull(blackKingSpot)

        assertEquals(Position(7, 4), whiteKingSpot?.position)
        assertEquals(Position(0, 4), blackKingSpot?.position)
        assert(whiteKingSpot?.chessPiece is King)
        assert(blackKingSpot?.chessPiece is King)
    }

    @Test
    fun getRemainingValidMoves_returnsValidPawnMovesFromStartingRow() {
        val e2Position = Position(6, 4) // White e2 pawn
        val validMoves = gamePlay.getRemainingValidMoves(e2Position, player1)

        assertEquals(2, validMoves.size)
        assert(validMoves.contains(Position(5, 4)))
        assert(validMoves.contains(Position(4, 4)))
    }
}
