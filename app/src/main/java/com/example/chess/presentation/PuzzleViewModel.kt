package com.example.chess.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.data.StockfishEngine
import com.example.chess.data.local.PuzzleDao
import com.example.chess.data.local.PuzzleEntity
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Spot
import com.example.chess.data.repository.FenConverter
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.repository.OpeningBookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class PuzzleFeedback { NONE, CORRECT, INCORRECT }

// Owns its own isolated GamePlay/Board pair — deliberately NOT the app-wide singleton
// GamePlay that ChessGameViewModel uses, so solving a puzzle can never disturb a live
// game in progress. StockfishEngine's constructor doesn't start its native subprocess
// on its own (that only happens on an explicit engine command), so this is cheap.
@HiltViewModel
class PuzzleViewModel @Inject constructor(
    private val puzzleDao: PuzzleDao,
    private val openingBookRepository: OpeningBookRepository,
) : ViewModel() {

    private val board = Board()
    private val game = GamePlay(board, StockfishEngine(), openingBookRepository)

    val puzzles: StateFlow<List<PuzzleEntity>> = puzzleDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var activePuzzle: PuzzleEntity? = null
    private var puzzlePlayer: Player = Player(isWhite = true, playerType = PlayerType.HUMAN)

    private val _boardState = MutableStateFlow(BoardState(board, null, GameStatus.ACTIVE, null))
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()

    private val _selectedPiecePosition = MutableStateFlow<Spot?>(null)
    val selectedPiecePosition: StateFlow<Spot?> = _selectedPiecePosition.asStateFlow()

    private val _validMoves = MutableStateFlow<List<Position>>(emptyList())
    val validMoves: StateFlow<List<Position>> = _validMoves.asStateFlow()

    private val _feedback = MutableStateFlow(PuzzleFeedback.NONE)
    val feedback: StateFlow<PuzzleFeedback> = _feedback.asStateFlow()

    fun loadPuzzleById(id: Long) {
        viewModelScope.launch {
            puzzleDao.getById(id)?.let { loadPuzzle(it) }
        }
    }

    fun loadPuzzle(puzzle: PuzzleEntity) {
        val (loadedBoard, activeColor) = FenConverter.fromFen(puzzle.fen)

        // game.init() populates the lateinit `players` list that detectCheck/
        // checkForWinner/etc. require (this GamePlay instance is never otherwise
        // init()'d, unlike the app-wide singleton) and resets the board to the
        // standard start — apply the puzzle's actual position after, not before.
        val whitePlayer = Player(isWhite = true, playerType = PlayerType.HUMAN)
        val blackPlayer = Player(isWhite = false, playerType = PlayerType.HUMAN)
        game.init(whitePlayer, blackPlayer, true)

        board.boxes = loadedBoard.boxes
        board.enPassantTarget = loadedBoard.enPassantTarget

        puzzlePlayer = if (activeColor == Color.WHITE) whitePlayer else blackPlayer
        game.currentTurn.value = puzzlePlayer
        game.isGameOver = false

        activePuzzle = puzzle
        _feedback.value = PuzzleFeedback.NONE
        _selectedPiecePosition.value = null
        _validMoves.value = emptyList()
        // A clone, not `board` itself — board.boxes was mutated in place above, so the
        // reference is unchanged and Compose's remember(boardState.board, ...) in
        // PuzzleScreen would never see a new key and would keep rendering stale squares.
        // Same fix as ChessGameViewModel.syncAfterBoardChange() uses for the live game.
        _boardState.value = BoardState(board.clone(), puzzlePlayer, GameStatus.ACTIVE, null)
    }

    fun onSelectSpot(spot: Spot) {
        // Clear a stale banner from a previous attempt the moment the player starts a
        // new one — otherwise "Not quite" (and the board-shifting layout it causes)
        // sticks around for the rest of the puzzle even while retrying successfully.
        _feedback.value = PuzzleFeedback.NONE
        _selectedPiecePosition.value = spot
        _validMoves.value = game.getRemainingValidMoves(spot.position, puzzlePlayer, board)
    }

    fun onMovePieceTo(target: Position) {
        val selected = _selectedPiecePosition.value ?: return
        val puzzle = activePuzzle ?: return
        val fromSpot = board.getBox(selected.position.row, selected.position.column)
        val toSpot = board.getBox(target.row, target.column)

        // Compare by from/to squares only — a puzzle whose correct move happens to be a
        // promotion accepts any promotion piece as correct in this v1.
        val playedUci = selected.position.toAlgebraicNotation() + target.toAlgebraicNotation()
        val moved = game.handleHumanMove(puzzlePlayer, fromSpot, toSpot, false, board)

        _selectedPiecePosition.value = null
        _validMoves.value = emptyList()
        if (!moved) {
            _boardState.value = _boardState.value.copy(board = board.clone())
            return
        }

        if (playedUci.equals(puzzle.correctMoveUci.take(4), ignoreCase = true)) {
            _feedback.value = PuzzleFeedback.CORRECT
            viewModelScope.launch { puzzleDao.update(puzzle.copy(solved = true)) }
        } else {
            _feedback.value = PuzzleFeedback.INCORRECT
            game.movesPlayed.lastOrNull()?.let { game.undoMoveByPlayer(it) }
            game.currentTurn.value = puzzlePlayer
            // checkForWinner() runs on every move, including the one just undone — if it
            // concluded checkmate/stalemate for that rejected move, isGameOver is left
            // stuck true and handleHumanMove's isGameOver gate (GamePlay.kt) silently
            // rejects every future move on this puzzle. The undone move never really
            // ended the game, so force it back open.
            game.isGameOver = false
        }

        _boardState.value =
            _boardState.value.copy(board = board.clone(), currentPlayer = game.currentTurn.value)
    }
}
