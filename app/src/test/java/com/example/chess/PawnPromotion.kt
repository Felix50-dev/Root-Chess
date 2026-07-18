package com.example.chess

import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.repository.GamePlay
import com.example.chess.presentation.ChessGameViewModel
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.mockito.Mockito.mock
import android.content.Context
import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Player
import com.example.chess.data.repository.MoveExplainer
import org.junit.Test

//class PawnPromotion {
//    @Test
//    fun `checkForPawnPromotion should trigger promotion UI for white pawn at row 0`() {
//        // Arrange
//        val board = Board()
//        val stockfish = StockfishEngine()
//        val game = GamePlay(board, stockfish)
//        val mockContext = mock(Context::class.java)
//
//        //val viewModel = ChessGameViewModel(game, mockContext)
//
//        val pawn = Pawn(color = Color.BLACK, position = Position(7, 3), isKilled = false)
//        val spot = board.getBox(7, 3)
//        spot.chessPiece = pawn
//
//        // Set the turn to white human
//        game.currentTurn.value = Player( false,PlayerType.HUMAN)
//
//        // Act
//        viewModel.checkForPawnPromotion(spot)
//
//        // Assert
//        assertTrue(viewModel.showPawnPromotion.value)
//        //assertNull(viewModel.promotionPiece.value)
//    }
//
//
//}
