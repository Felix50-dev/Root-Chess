package com.example.chess.presentation

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.data.model.AILevel
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.GameMode
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Spot
import com.example.chess.data.repository.GamePlay
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChessGameViewModel"

@HiltViewModel
class ChessGameViewModel @Inject constructor(
    private val game: GamePlay,
    @ApplicationContext context: Context
) : ViewModel() {

    private val playerWhite = Player(isWhite = true, playerType = PlayerType.HUMAN)
    private val playerDark = Player(isWhite = false, playerType = PlayerType.HUMAN)

    //boardState
    private val _boardState: MutableStateFlow<BoardState> = MutableStateFlow(BoardState(null, null))
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()

    //detectCheck
    private val _isInCheck = MutableStateFlow(false)
    val isInCheck: StateFlow<Boolean> = _isInCheck.asStateFlow()

    //detectCheck position
    private val _checkPosition = MutableStateFlow(Position(0,0))
    val checkPosition = _checkPosition.asStateFlow()

    //color
    private val _color = MutableStateFlow(true)
    val color: StateFlow<Boolean> = _color.asStateFlow()

    //level selected
    private val _level = MutableStateFlow(AILevel.LEVEL1)
    val level: StateFlow<AILevel> = _level.asStateFlow()

    //game mode
    private val _gameMode = MutableStateFlow(GameMode.FRIEND)

    //valid moves
    private val _validMoves = MutableStateFlow<List<Position>>(emptyList())
    val validMoves: StateFlow<List<Position>> = _validMoves.asStateFlow()

    //selectedStartSpot
    private val _selectedPiecePosition = MutableStateFlow<Spot?>(null)
    val selectedStartSpot: StateFlow<Spot?> = _selectedPiecePosition.asStateFlow()

    //promotion Piece
    private var _promotionPiece = MutableStateFlow<ChessPiece?>(null)
    var promotionPiece: StateFlow<ChessPiece?> = _promotionPiece.asStateFlow()


    val promotionPieceAI: StateFlow<ChessPiece?> = game.promotionPieceFlow

    init {
        viewModelScope.launch {
            game.stockfish.startStockfish("${context.applicationInfo.nativeLibraryDir}/lib_chess.so")
            game.stockfish.sendCommand("uci")
        }
    }

    init {
        viewModelScope.launch {
            game.init(playerWhite, playerDark)
            game.currentTurn.collect {
                if (it != null) {
                    if (it.playerType  == PlayerType.AI) {
                        //val opponent = if (it == playerWhite) playerDark else playerWhite
                        Log.d(TAG, "collection successful: ")
                        game.handleAIMove(it)
                        _isInCheck.value = detectCheck() == true
                        getKingPosition()
                        _boardState.value = BoardState(game.board, game.currentTurn.value, game.status)
                    }
                }
            }
        }
    }



    fun setGameMode(gameMode: GameMode) {
        viewModelScope.launch {
            _gameMode.value = gameMode
            if (gameMode == GameMode.FRIEND) {
                playerWhite.playerType = PlayerType.HUMAN
                playerDark.playerType = PlayerType.HUMAN
                _boardState.value = BoardState(game.board, game.currentTurn.value, game.status)
            } else {
                Log.d(TAG, "setGameMode: running")
                game.stockfish.sendCommand("ucinewgame")
            }
        }
    }


    fun setAILevel(aiLevel: AILevel) {
        _level.value = aiLevel
    }

    fun setColor(isWhite: Boolean) {
        viewModelScope.launch {
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
            _boardState.value = BoardState(game.board, game.currentTurn.value, game.status)
        }
    }

    fun onSelectSpot(spot: Spot) {
        Log.d(
            TAG,
            "onSelectSpot: spot selected ${spot.position.toAlgebraicNotation()} position is ${spot.position}"
        )
        _selectedPiecePosition.value = spot
        updateValidMoves(spot.position)
    }

    fun setPromotedPiece(promotedPiece: ChessPiece) {
        _promotionPiece.value = promotedPiece
    }

    fun startNewGame() {
        viewModelScope.launch {
            game.init(playerWhite, playerDark)
            game.isGameOver = false
            _selectedPiecePosition.value = null
            _boardState.value.gameStatus = GameStatus.ACTIVE
            _promotionPiece.value = null
            _boardState.value = BoardState(game.board, game.currentTurn.value, game.status)
        }
    }

    fun quitGame() {
        game.isGameOver = true
        game.movesPlayed.clear()
        _boardState.value = BoardState(null, null)
        _selectedPiecePosition.value = null
        _promotionPiece.value = null
    }

    private fun detectCheck(): Boolean? {
        return game.currentTurn.value?.let {
            game.detectCheck(it)
        }
    }

    private fun getKingPosition() {
        val playerColor = if (game.currentTurn.value == playerWhite) Color.WHITE else Color.BLACK
        if (game.currentTurn.value?.let { game.detectCheck(it) } == true) {
            if (game.findKing(game.board, playerColor) != null) {
                _checkPosition.value = game.findKing(game.board, playerColor)?.position!!
            }
        }
    }

    fun movePieceTo(targetPosition: Position) {
        Log.d(TAG, "movePieceTo: piece moved")
        val selectedSpot = _selectedPiecePosition.value ?: return
        val targetSpot = game.board.getBox(targetPosition.row, targetPosition.column)
        playerMove(selectedSpot, targetSpot)
        Log.d(TAG, "movePieceTo: ui recomposed")
        _isInCheck.value = detectCheck() == true
        getKingPosition()
        _boardState.value = BoardState(game.board, game.currentTurn.value, game.status)
        _validMoves.value = emptyList()
    }

    private fun updateValidMoves(position: Position) {
        _validMoves.value = game.currentTurn.value?.let {
            game.getRemainingValidMoves(position,
                it
            )
        }!!
    }

    private fun playerMove(start: Spot, end: Spot): Boolean {
        var playerMove = false
        viewModelScope.launch {
            playerMove = game.currentTurn.value?.let { makeMove(it, start, end) } == true
        }
        return playerMove
    }

    private suspend fun makeMove(player: Player, start: Spot, end: Spot): Boolean {
        return if (player.playerType == PlayerType.HUMAN) {
            // Handle the human player's move synchronously
            val value = game.handleHumanMove(player, start, end)
            _boardState.value = BoardState(game.board, game.currentTurn.value, game.status)
            value
        } else {
            // Handle the AI move asynchronously using the result from handleAIMove
            val value = game.handleAIMove(player)
            _boardState.value = BoardState(game.board, game.currentTurn.value, game.status)
            value
        }
    }

    fun undoMove() {
        if (game.movesPlayed.isNotEmpty()) {
            Log.d(TAG, "undoMove: movesMadeAre: ${game.movesPlayed}")
            if (playerWhite.playerType == PlayerType.AI || playerDark.playerType == PlayerType.AI) {
                game.undoMoveByPlayer(game.movesPlayed.last(), game.board)
                game.undoMoveByPlayer(game.movesPlayed.last(), game.board)
            } else {
                game.undoMoveByPlayer(game.movesPlayed.last(), game.board)
                val turn = if (game.currentTurn.value == playerDark) playerWhite else playerDark
                game.currentTurn.value = turn
            }
            _boardState.value.board = game.board
        }
    }

    fun redoMove() {
        if (game.movesUndone.isNotEmpty()) {
            Log.d(
                TAG,
                "redoMove: movesUndone are: ${game.movesUndone}: total number is: ${game.movesUndone.size}"
            )
            if (playerWhite.playerType == PlayerType.AI || playerDark.playerType == PlayerType.AI) {
                game.redoMove(game.movesUndone.last(), game.board)
                game.currentTurn.value = if (game.currentTurn.value == playerDark) playerWhite else playerDark
                game.redoMove(game.movesUndone.last(), game.board)
                game.currentTurn.value = if (game.currentTurn.value == playerDark) playerWhite else playerDark
                _boardState.value.board = game.board
            } else {
                game.redoMove(game.movesUndone.last(), game.board)
                _boardState.value.board = game.board
            }
        }
    }

}

data class BoardState(
    var board: Board?,
    val currentPlayer: Player?,
    var gameStatus: GameStatus = GameStatus.ACTIVE
)