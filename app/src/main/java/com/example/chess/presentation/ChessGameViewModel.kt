package com.example.chess.presentation

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.Player
import com.example.chess.data.model.Position
import com.example.chess.data.model.Spot
import com.example.chess.data.repository.GamePlay
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "ChessGameViewModel"

@HiltViewModel
class ChessGameViewModel @Inject constructor(
    private val game: GamePlay
) : ViewModel() {

    private val playerWhite = Player(isWhite = true)
    private val playerDark = Player(isWhite = false)

    //boardState
    private val _boardState: MutableStateFlow<BoardState> = MutableStateFlow(BoardState(null, null))
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()

    //valid moves
    private val _validMoves = MutableStateFlow<List<Position>>(emptyList())
    val validMoves: StateFlow<List<Position>> = _validMoves.asStateFlow()

    //selectedStartSpot
    private val _selectedPiecePosition = MutableStateFlow<Spot?>(null)
    val selectedStartSpot: StateFlow<Spot?> = _selectedPiecePosition.asStateFlow()

    init {
        viewModelScope.launch {
            game.init(playerWhite, playerDark)
            _boardState.value = BoardState(game.board, game.currentTurn, game.status)
        }
    }

    fun onSelectSpot(spot: Spot) {
        Log.d(TAG, "onSelectSpot: spot selected")
        _selectedPiecePosition.value = spot
        updateValidMoves(spot.position)
    }

    fun movePieceTo(targetPosition: Position) {
        Log.d(TAG, "movePieceTo: piece moved")
        val selectedSpot = _selectedPiecePosition.value ?: return
        val targetSpot = game.board.getBox(targetPosition.row, targetPosition.column)
        playerMove(selectedSpot, targetSpot)
        _boardState.value = BoardState(game.board, game.currentTurn, game.status)
        _validMoves.value = emptyList()
    }

    private fun updateValidMoves(position: Position) {
        _validMoves.value = game.getRemainingValidMoves(position, game.currentTurn)
    }

    private fun playerMove(start: Spot, end: Spot): Boolean {
        return game.playerMove(game.currentTurn, start, end)
    }

}

data class BoardState(
    val board: Board?,
    val currentPlayer: Player?,
    val gameStatus: GameStatus = GameStatus.ACTIVE
)