package com.example.chess.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.chess.data.model.AILevel
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.GameMode
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Spot
import com.example.chess.data.repository.GamePlay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

private const val TAG = "ChessGameViewModel"

@HiltViewModel
class ChessGameViewModel @Inject constructor(
    private val game: GamePlay
) : ViewModel() {

    private val playerWhite = Player(isWhite = true, playerType = PlayerType.HUMAN)
    private val playerDark = Player(isWhite = false, playerType = PlayerType.HUMAN)

    //boardState
    private val _boardState: MutableStateFlow<BoardState> = MutableStateFlow(BoardState(null, null))
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()

    //color
    private val _color = MutableStateFlow(true)
    val color: StateFlow<Boolean> = _color.asStateFlow()

    //level selected
    private val _level = MutableStateFlow(AILevel.LEVEL1)
    val level: StateFlow<AILevel> = _level.asStateFlow()

    //game mode
    private val _gameMode = MutableStateFlow(GameMode.FRIEND)
    val gameMode: StateFlow<GameMode> = _gameMode.asStateFlow()

    //valid moves
    private val _validMoves = MutableStateFlow<List<Position>>(emptyList())
    val validMoves: StateFlow<List<Position>> = _validMoves.asStateFlow()

    //selectedStartSpot
    private val _selectedPiecePosition = MutableStateFlow<Spot?>(null)
    val selectedStartSpot: StateFlow<Spot?> = _selectedPiecePosition.asStateFlow()

    //promotion Piece
    private var _promotionPiece = MutableStateFlow<ChessPiece?>(null)
    var promotionPiece: StateFlow<ChessPiece?> = _promotionPiece.asStateFlow()

    fun setGameMode(gameMode: GameMode) {
        _gameMode.value = gameMode
        if (gameMode == GameMode.FRIEND) {
            playerWhite.playerType = PlayerType.HUMAN
            playerDark.playerType = PlayerType.HUMAN
            game.init(playerWhite, playerDark, _level.value)
            _boardState.value = BoardState(game.board, game.currentTurn, game.status)
        }
    }

    fun setAILevel(aiLevel: AILevel) {
        _level.value = aiLevel
    }

    fun setColor(isWhite: Boolean) {
        _color.value = isWhite
        if (_gameMode.value == GameMode.COMPUTER) {
            if (isWhite) {
                playerWhite.playerType = PlayerType.HUMAN
                playerDark.playerType = PlayerType.AI
            } else {
                playerWhite.playerType = PlayerType.AI
                playerDark.playerType = PlayerType.HUMAN
            }
        }
        game.init(playerWhite, playerDark, _level.value)
        _boardState.value = BoardState(game.board, game.currentTurn, game.status)
    }

    fun onSelectSpot(spot: Spot) {
        Log.d(TAG, "onSelectSpot: spot selected")
        _selectedPiecePosition.value = spot
        updateValidMoves(spot.position)
    }

    fun setPromotedPiece(promotedPiece: ChessPiece) {
        _promotionPiece.value = promotedPiece
    }

    fun startNewGame() {
        game.init(playerWhite, playerDark, _level.value)
        game.isGameOver = false
        _selectedPiecePosition.value = null
        _boardState.value.gameStatus = GameStatus.ACTIVE
        _promotionPiece.value = null
        _boardState.value = BoardState(game.board, game.currentTurn, game.status)
    }

    fun quitGame() {
        game.isGameOver = true
        game.movesPlayed.clear()
        _boardState.value = BoardState(null, null)
        _selectedPiecePosition.value = null
        _promotionPiece.value = null
    }

    fun movePieceTo(targetPosition: Position) {
        Log.d(TAG, "movePieceTo: piece moved")
        val selectedSpot = _selectedPiecePosition.value ?: return
        val targetSpot = game.board.getBox(targetPosition.row, targetPosition.column)
        val level = _level.value
        playerMove(selectedSpot, targetSpot, level)
        _boardState.value = BoardState(game.board, game.currentTurn, game.status)
        _validMoves.value = emptyList()
    }

    private fun updateValidMoves(position: Position) {
        _validMoves.value = game.getRemainingValidMoves(position, game.currentTurn)
    }

    private fun playerMove(start: Spot, end: Spot, aiLevel: AILevel): Boolean {
        return game.playerMove(game.currentTurn, start, end, aiLevel)
    }

    fun undoMove() {
        if (game.movesPlayed.isNotEmpty()) {
            Log.d(TAG, "undoMove: movesMadeAre: ${game.movesPlayed}")
            game.undoMoveByPlayer(game.movesPlayed.last(), game.board)
            val turn = if (game.currentTurn == playerDark) playerWhite else playerDark
            game.currentTurn = turn
            _boardState.value = BoardState(game.board, game.currentTurn, game.status)
        }
    }

    fun redoMove() {
        if (game.movesUndone.isNotEmpty()) {
            Log.d(
                TAG,
                "redoMove: movesUndone are: ${game.movesUndone}: total number is: ${game.movesUndone.size}"
            )
            game.redoMove(game.movesUndone.last(), game.board)
            _boardState.value = BoardState(game.board, game.currentTurn, game.status)
        }
    }

}

data class BoardState(
    val board: Board?,
    val currentPlayer: Player?,
    var gameStatus: GameStatus = GameStatus.ACTIVE
)