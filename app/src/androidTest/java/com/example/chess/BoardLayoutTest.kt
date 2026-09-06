package com.example.chess

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.chess.data.model.Board
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.presentation.BoardState
import com.example.chess.presentation.ChessGrid
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test

class BoardLayoutTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun chessGrid_analyzeButtonIsDisplayed() {
        val board = Board()
        val currentPlayer = Player(true, PlayerType.HUMAN)
        val boardState = BoardState(
            board = board,
            currentPlayer = currentPlayer,
            lastMovedFrom = null
        )

        composeTestRule.setContent {
            ChessGrid(
                currentPlayer = currentPlayer,
                boardState = boardState,
                isInCheck = false,
                lastMove = null,
                validMoves = emptyList(),
                onSelectSpot = {},
                onMovePieceTo = {},
                onSetPromotionPiece = {},
                onUndoMove = {},
                onRedoMove = {},
                onStartNewGame = {},
                onQuitGame = {},
                onExitGame = {},
                onAnalzeGame = {},
                checkPosition = Position(0, 0),
                piecesKilled = mutableStateListOf(),
                movesMadeSan = mutableStateListOf(),
                showPromotionDialog = false,
                aiLastMove = null,
                visualPieces = persistentListOf()
            )
        }

        // Verify that "Analyze Game" button is visible
        composeTestRule.onNodeWithText("Analyze Game").assertIsDisplayed()
    }
}
