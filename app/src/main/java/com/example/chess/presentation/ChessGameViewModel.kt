package com.example.chess.presentation

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.R
import com.example.chess.data.model.AILevel
import com.example.chess.data.model.AnalysisResult
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.Feedback
import com.example.chess.data.model.GameMode
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.SANMovePair
import com.example.chess.data.model.Spot
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.repository.TacticsRecognizer
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    private val _lastMove = MutableStateFlow<Move?>(null)
    val lastMove: StateFlow<Move?> = _lastMove.asStateFlow()

    private val _aiLastMove = MutableStateFlow<Move?>(null)
    val aiLastMove: StateFlow<Move?> = _aiLastMove.asStateFlow()

    // Add a StateFlow to control promotion dialog visibility
    private val _showPawnPromotion = MutableStateFlow(false)
    val showPawnPromotion: StateFlow<Boolean> = _showPawnPromotion.asStateFlow()

    private val playerWhite = Player(isWhite = true, playerType = PlayerType.HUMAN)
    private val playerDark = Player(isWhite = false, playerType = PlayerType.HUMAN)

    //boardState
    private val _boardState: MutableStateFlow<BoardState> =
        MutableStateFlow(BoardState(null, null, lastMovedFrom = null))
    val boardState: StateFlow<BoardState> = _boardState.asStateFlow()

    //detectCheck
    private val _isInCheck = MutableStateFlow(false)
    val isInCheck: StateFlow<Boolean> = _isInCheck.asStateFlow()

    //detectCheck position
    private val _checkPosition = MutableStateFlow(Position(0, 0))
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

    //promotion Piece
    private var _promotionPiece = MutableStateFlow<ChessPiece?>(null)
    var promotionPiece: StateFlow<ChessPiece?> = _promotionPiece.asStateFlow()

    //pieces killed
    private var _piecesKilled = MutableStateFlow<MutableList<ChessPiece>>(mutableListOf())
    var piecesKilled: StateFlow<MutableList<ChessPiece>> = _piecesKilled.asStateFlow()

    //moves made
    private val _movesMade = MutableStateFlow<MutableList<String>>(mutableListOf())
    val movesMade: StateFlow<MutableList<String>> = _movesMade.asStateFlow()

    private val _movesMadeSAN = MutableStateFlow<MutableList<SANMovePair>>(mutableListOf())
    val movesMadeSAN: StateFlow<MutableList<SANMovePair>> = _movesMadeSAN.asStateFlow()

    private val _analysisResult = MutableStateFlow<AnalysisResult?>(value = null)
    val analysisResult: StateFlow<AnalysisResult?> = _analysisResult.asStateFlow()

    private val _currentMoveIndex = MutableStateFlow(0)
    val currentMoveIndex: StateFlow<Int> = _currentMoveIndex.asStateFlow()
    private val _currentMoveCategory = MutableStateFlow<String?>(null)
    val currentMoveCategory: StateFlow<String?> = _currentMoveCategory.asStateFlow()
    private var _movesToSimulate = emptyList<com.example.chess.data.model.Move>()
    private var _movesToSimulateSAN = emptyList<String>()

    private val _feedback = MutableStateFlow<Feedback?>(null)
    val feedback: StateFlow<Feedback?> = _feedback.asStateFlow()

    private var autoReviewJob: Job? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying


    private var aiJob: Job? = null

    private lateinit var soundPool: SoundPool
    private var moveSoundId: Int = 0
    private var captureSoundId: Int = 0

    init {
        viewModelScope.launch {
            game.stockfish.startStockfish("${context.applicationInfo.nativeLibraryDir}/lib_chess.so")
            game.stockfish.sendCommand("uci")
        }
    }

    fun initializeSounds(context: Context) {
        soundPool = SoundPool.Builder()
            .setMaxStreams(2) // Allow 2 sounds at once
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .build()
        moveSoundId = soundPool.load(context, R.raw.move_self, 1)
        captureSoundId = soundPool.load(context, R.raw.capture, 1)
    }

    private fun generateExplanation(
        type: String,
        bestMove: String?,
        chessPiece: String,
        numOfMoves: Int? = 0,
        matingPiece: ChessPiece?,
        tactic: List<String>? = null
    ): String {
        Log.d(TAG, "generateExplanation: move category is $type")
        return when (type) {
            "Best" -> "This is the optimal move in this position."
            "Great" -> "This is a solid move that maintains your position."
            "Inaccurate" -> "This move is slightly suboptimal. Consider $bestMove."
            "Miss" -> "You missed a material advantage by capturing $chessPiece at $bestMove ."
            "MissedMate" -> "You missed a mate in $numOfMoves move(s) by moving $matingPiece to ${
                bestMove?.takeLast(
                    2
                )
            }."

            "Mistake" -> "This move weakens your position. Better was $bestMove."
            "Blunder" -> "This move loses material or position. Better was $bestMove."
            "Brilliant" -> "This move is exceptionally good, a $tactic in chess."
            "Fork" -> "This move attacks two or more valuable enemy pieces at once — a fork!"
            "Pin" -> "This move pins an enemy piece to a more valuable one, such as their king or queen."
            "Discovered Attack" -> "This move reveals an attack by another piece — a powerful discovered tactic!"
            "Double Check" -> "A rare double check! Two pieces are attacking the king simultaneously."
            "Skewer" -> "A skewer tactic! The more valuable piece must move, exposing a weaker one."
            else -> "Move analysis."
        }
    }

    // Example usage after analyzing a move
    private fun createFeedbackForMove(
        move: String,
        category: String,
        bestMove: String?,
        chessPiece: String,
        mateIn: Int?,
        matingPiece: ChessPiece?,
        tactic: List<String>? = null
    ): Feedback {
        val explanation = generateExplanation(category, bestMove, chessPiece, mateIn, matingPiece, tactic)
        val feedback = Feedback(
            move = move,
            type = category,
            explanation = explanation,
            bestMove = if (category in listOf(
                    "Inaccurate",
                    "Mistake",
                    "Blunder",
                )
            ) bestMove else null
        )
        Log.d(TAG, "createFeedbackForMove: move feedback is $feedback")
        return feedback
    }

    fun setGameMode(gameMode: GameMode) {
        viewModelScope.launch {
            _gameMode.value = gameMode
            if (gameMode == GameMode.FRIEND) {
                playerWhite.playerType = PlayerType.HUMAN
                playerDark.playerType = PlayerType.HUMAN
                game.init(playerWhite, playerDark, true)
                game.movesPlayed.clear()
                _boardState.value =
                    BoardState(game.board, game.currentTurn.value, game.status, null)
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
        _color.value = isWhite

        if (_gameMode.value == GameMode.COMPUTER) {
            if (isWhite) {
                playerWhite.playerType = PlayerType.HUMAN
                playerDark.playerType = PlayerType.AI
            } else {
                playerWhite.playerType = PlayerType.AI
                playerDark.playerType = PlayerType.HUMAN
            }
            game.init(playerWhite, playerDark, true)
        }

        _boardState.value =
            BoardState(game.board.clone(), game.currentTurn.value, game.status, null)

        // Cancel any existing AI job and start new collector
        aiJob?.cancel()
        aiJob = viewModelScope.launch(Dispatchers.Main) {
            game.currentTurn
                .collect { currentPlayer ->
                    if (currentPlayer?.playerType == PlayerType.AI) {
                        handleAIMoveAndUpdateState(currentPlayer)
                    }
                }
        }
    }

    fun analyzeGame() {
        viewModelScope.launch {
            _analysisResult.value = game.analyzeMoveScores(_movesMade.value)
        }
    }

    // Extracted reusable function for AI move handling and state updates
    private suspend fun handleAIMoveAndUpdateState(player: Player) {
        try {
            Log.d(TAG, "AI turn started: ${player.playerType}")
            game.handleAIMove(player, _level.value)
            val aiLastMove = game.aiLastMove
            val isCapture = aiLastMove?.pieceKilled != null
            if (aiLastMove != null) {
                _aiLastMove.value = Move(aiLastMove.from.position, aiLastMove.to.position)
                Log.d(TAG, "handleAIMoveAndUpdateState: aiLastMove is $aiLastMove")
            }

            // Synchronize access to shared collections
            _piecesKilled.value = synchronized(game.piecesKilled) {
                game.piecesKilled.toMutableList()
            }
            _movesMade.value = synchronized(game.movesPlayed) {
                game.convertMovesToNotation(game.movesPlayed.toMutableList())
            }
            _movesMadeSAN.value = game.convertMovesToSANNotation(game.movesPlayed)



            _isInCheck.value = detectCheck() == true
            getKingPosition()
            _boardState.value = BoardState(
                game.board.clone(),
                game.currentTurn.value,
                game.status,
                game.aiStartPosition
            )

            if (isCapture) soundPool.play(captureSoundId, 0.5f, 0.5f, 1, 0, 1f) else
                soundPool.play(moveSoundId, 0.5f, 0.5f, 1, 0, 1f)

        } catch (e: Exception) {
            Log.e(TAG, "Error in AI move: ${e.message}", e)
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
        _showPawnPromotion.value = false // Hide dialog after selection
        val lastMoveValue = _lastMove.value ?: return
        val targetSpot = game.board.getBox(lastMoveValue.to.row, lastMoveValue.to.column)
        applyPromotion(targetSpot)
    }

    fun startNewGame() {
        viewModelScope.launch {
            game.init(playerWhite, playerDark, true)
//            game.isGameOver = false
            game.movesPlayed.clear()
            _movesMade.value = emptyList<String>().toMutableList()
            _movesMadeSAN.value = emptyList<SANMovePair>().toMutableList()
            //game.piecesKilled.clear()
            _piecesKilled.value = emptyList<ChessPiece>().toMutableList()
            _selectedPiecePosition.value = null
            _boardState.value.gameStatus = GameStatus.ACTIVE
            _promotionPiece.value = null
            _boardState.value = BoardState(game.board, game.currentTurn.value, game.status, null)
            // Cancel any existing AI job and start new collector
            aiJob?.cancel()
            aiJob = viewModelScope.launch(Dispatchers.Main) {
                game.currentTurn
                    .collect { currentPlayer ->
                        if (currentPlayer?.playerType == PlayerType.AI) {
                            handleAIMoveAndUpdateState(currentPlayer)
                        }
                    }
            }
            Log.d(TAG, "startNewGame: ${game.board.boxes}")
        }
    }

    fun quitGame() {
        game.isGameOver = true
        game.movesPlayed.clear()
        _movesMade.value = emptyList<String>().toMutableList()
        _movesMadeSAN.value = emptyList<SANMovePair>().toMutableList()
        game.piecesKilled.clear()
        _piecesKilled.value = emptyList<ChessPiece>().toMutableList()
        _boardState.value = BoardState(null, null, game.status, null)
        _selectedPiecePosition.value = null
        _promotionPiece.value = null
        aiJob?.cancel()
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

    fun checkForPawnPromotion(targetSpot: Spot) {
        val piece = targetSpot.chessPiece
        if (piece is Pawn && ((piece.color == Color.WHITE && targetSpot.position.row == 0) || (piece.color == Color.BLACK && targetSpot.position.row == 7))
        ) {
            if (game.currentTurn.value?.playerType == PlayerType.HUMAN) {
                _showPawnPromotion.value = true
                _promotionPiece.value = null
            }
        }
    }

    private fun applyPromotion(targetSpot: Spot) {
        _promotionPiece.value?.let { promotedPiece ->
            targetSpot.chessPiece = promotedPiece
            _boardState.value = _boardState.value.copy(board = game.board.clone())
            Log.d(
                TAG,
                "Promotion applied: ${promotedPiece.javaClass.simpleName} at ${targetSpot.position}"
            )
        }
    }

    fun movePieceTo(targetPosition: Position) {
        Log.d(TAG, "movePieceTo: piece selected for move")
        val selectedSpot = _selectedPiecePosition.value ?: return
        val targetSpot = game.board.getBox(targetPosition.row, targetPosition.column)

        _lastMove.value = Move(selectedSpot.position, targetPosition)
        _validMoves.value = emptyList()

        // Delay the actual move and board update until animation completes
        viewModelScope.launch {
            //delay(400) // Match animation duration
            playerMove(selectedSpot, targetSpot)
            _isInCheck.value = detectCheck() == true
            getKingPosition()
            _boardState.value =
                BoardState(game.board, game.currentTurn.value, game.status, selectedSpot.position)
            checkForPawnPromotion(targetSpot)
        }
    }

    private fun updateValidMoves(position: Position) {
        _validMoves.value = game.currentTurn.value?.let {
            game.getRemainingValidMoves(
                position,
                it,
                game.board
            )
        }!!
    }

    private fun playerMove(start: Spot, end: Spot, isReviewMode: Boolean = false): Boolean {
        var playerMove = false
        viewModelScope.launch {
            Log.d(TAG, "playerMove: current is ${game.currentTurn.value!!}")
            playerMove = makeMove(game.currentTurn.value!!, start, end, game.board, isReviewMode)
        }
        return playerMove
    }

    private fun makeMove(
        player: Player,
        start: Spot,
        end: Spot,
        board: Board,
        isReviewMode: Boolean = false
    ): Boolean {
        val value = game.handleHumanMove(player, start, end, isReviewMode, board)
        val isCaptureMove = end.chessPiece != null
        checkForPawnPromotion(end)
        _piecesKilled.value = game.piecesKilled.toMutableList()
        _movesMade.value = game.convertMovesToNotation(game.movesPlayed)
        Log.d(TAG, "makeMove: movesMade are: ${_movesMade.value}")
        _movesMadeSAN.value = game.convertMovesToSANNotation(game.movesPlayed)
        Log.d(TAG, "makeMove: SAN are: ${_movesMadeSAN.value}")
        _boardState.value = _boardState.value.copy(
            board = game.board,
            currentPlayer = game.currentTurn.value,
            gameStatus = game.status,
            lastMovedFrom = start.position
        )
        _lastMove.value = Move(start.position, end.position)
        Log.d(TAG, "makeMove last move is: ${_boardState.value.lastMovedFrom}")
        checkForPawnPromotion(end)

        if (isCaptureMove) soundPool.play(captureSoundId, 0.5f, 0.5f, 1, 0, 1f) else
            soundPool.play(moveSoundId, 0.5f, 0.5f, 1, 0, 1f)
        return value
    }

    fun initReviewMode() {
        _movesToSimulate = game.movesPlayed.toMutableList()
        _movesToSimulateSAN = game.convertMovesToNotation(game.movesPlayed)
        Log.d(TAG, "reviewGame: movesToSimulateSAN are $_movesToSimulateSAN")
        if (playerWhite.playerType == PlayerType.AI) {
            playerWhite.playerType = PlayerType.HUMAN
        }
        if (playerDark.playerType == PlayerType.AI) {
            playerDark.playerType = PlayerType.HUMAN
        }
        Log.d(TAG, "reviewGame: moves to simulate are: $_movesToSimulate")
        game.init(playerWhite, playerDark, true)
        _movesMade.value = emptyList<String>().toMutableList()
        _currentMoveIndex.value = 0
        _currentMoveCategory.value = null
    }

    fun nextMove(isReviewMode: Boolean = false) {
        if (_currentMoveIndex.value < _movesToSimulate.size) {
            if (_currentMoveIndex.value == 0) game.currentTurn.value = playerWhite
            val move = _movesToSimulate[_currentMoveIndex.value]
            val tactic = _analysisResult.value?.tactics?.getOrNull(_currentMoveIndex.value)
            Log.d(TAG, "nextMove: current move is $move tactic is $tactic")
            if (!isReviewMode) {
                move.from.chessPiece = move.to.chessPiece
                move.to.chessPiece =
                    game.board.getBox(move.to.position.row, move.from.position.column).chessPiece
            }
            playerMove(move.from, move.to, true)
            Log.d(TAG, "nextMove: current move is $move")
            Log.d(TAG, "previousMove: board has ${game.board.getBox(5, 5)}")
            val sanMove = _movesToSimulateSAN[_currentMoveIndex.value]
            val category = _analysisResult.value?.moveCategories?.getOrNull(_currentMoveIndex.value)
            val bestMove = _analysisResult.value?.bestMoves?.getOrNull(_currentMoveIndex.value)
            val captureOpportunity =
                _analysisResult.value?.captureOpportunity?.getOrNull(_currentMoveIndex.value)
            val missedMate = _analysisResult.value?.missedMates?.getOrNull(_currentMoveIndex.value)
            if (tactic?.isNotEmpty() == true) {
                _feedback.value = createFeedbackForMove(
                    sanMove,
                    category.toString(),
                    bestMove,
                    captureOpportunity?.target.toString(),
                    missedMate?.mateInBefore,
                    missedMate?.moveObject?.pieceMoved,
                    tactic
                )
            } else {
                _feedback.value = createFeedbackForMove(
                    sanMove,
                    category.toString(),
                    bestMove,
                    captureOpportunity?.target.toString(),
                    missedMate?.mateInBefore,
                    missedMate?.moveObject?.pieceMoved,
                    tactic
                )
            }
            _currentMoveIndex.value++
            _currentMoveCategory.value =
                analysisResult.value?.moveCategories?.getOrNull(_currentMoveIndex.value - 1)
            Log.d(
                TAG,
                "nextMove: Move ${_currentMoveIndex.value} made: $move, category: ${_currentMoveCategory.value}"
            )
            _boardState.value = _boardState.value.copy(
                board = game.board,
                currentPlayer = game.currentTurn.value,
                gameStatus = game.status,
            )
        } else {
            Log.e(TAG, "nextMove: No more moves to simulate")
        }
    }

    fun previousMove() {
        if (_currentMoveIndex.value > 0 && !_isPlaying.value) {
            val prevIndex = _currentMoveIndex.value - 1
            val move = _movesToSimulate[prevIndex]

            Log.d(TAG, "previousMove: reverting move at index $prevIndex: $move")
            move.to.chessPiece = move.from.chessPiece
            move.from.chessPiece = null
            game.undoMove(move, game.board)

            val tactic = _analysisResult.value?.tactics?.getOrNull(_currentMoveIndex.value)
            val sanMove = _movesToSimulateSAN[prevIndex]
            val category = _analysisResult.value?.moveCategories?.getOrNull(prevIndex)
            val bestMove = _analysisResult.value?.bestMoves?.getOrNull(prevIndex)
            val captureOpportunity = _analysisResult.value?.captureOpportunity?.getOrNull(prevIndex)
            val missedMate = _analysisResult.value?.missedMates?.getOrNull(prevIndex)?.mateInBefore
            val matingPiece =
                _analysisResult.value?.missedMates?.getOrNull(prevIndex)?.moveObject?.pieceMoved
            _feedback.value = createFeedbackForMove(
                sanMove,
                category.toString(),
                bestMove,
                captureOpportunity?.target.toString(),
                missedMate,
                matingPiece,
                tactic
            )
            _currentMoveCategory.value = category

            _currentMoveIndex.value = prevIndex

            _boardState.value = _boardState.value.copy(
                board = game.board.clone(),
                currentPlayer = game.currentTurn.value,
                gameStatus = game.status,
            )
            game.changeCurrentTurn()
        } else {
            Log.e(TAG, "previousMove: Cannot revert move (either index too low or playing)")
        }
    }


    fun startAutoReview() {
        autoReviewJob?.cancel()
        _isPlaying.value = true
        autoReviewJob = viewModelScope.launch {
            while (_currentMoveIndex.value < _movesToSimulate.size) {
                if (!_isPlaying.value) break

                Log.d(
                    TAG,
                    "startAutoReview: index is ${_currentMoveIndex.value} total size is ${_movesToSimulate.size}"
                )
                Log.d(TAG, "startAutoReview: current turn is ${game.currentTurn.value}")

                nextMove()
                val delayTime = if (_currentMoveCategory.value in listOf(
                        "Brilliant",
                        "Mistake",
                        "Blunder"
                    )
                ) 5000L else 3000L
                delay(delayTime)
            }
            _isPlaying.value = false
            Log.d(TAG, "startAutoReview: boardState is ${game.board.boxes}")
        }
    }


    fun onPlayPause() {
        if (_isPlaying.value) {
            _isPlaying.value = false
            autoReviewJob?.cancel()
        } else {
            startAutoReview()
        }
    }

    fun undoMove() {
        if (game.movesPlayed.isNotEmpty()) {
            Log.d(TAG, "undoMove: movesMadeAre: ${game.movesPlayed}")
            if (playerWhite.playerType == PlayerType.AI || playerDark.playerType == PlayerType.AI) {
                game.undoMoveByPlayer(game.movesPlayed.last())
                game.undoMoveByPlayer(game.movesPlayed.last())
            } else {
                game.undoMoveByPlayer(game.movesPlayed.last())
                val turn = if (game.currentTurn.value == playerDark) playerWhite else playerDark
                game.currentTurn.value = turn
            }
            val updatedBoard = game.board.clone()
            _piecesKilled.value = game.piecesKilled.toMutableList()
            _movesMadeSAN.value = game.convertMovesToSANNotation(game.movesPlayed)
            Log.d(TAG, "undoMove: ${_piecesKilled.value}")
            _boardState.value =
                _boardState.value.copy(board = updatedBoard, currentPlayer = game.currentTurn.value)
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
                game.currentTurn.value =
                    if (game.currentTurn.value == playerDark) playerWhite else playerDark
                game.redoMove(game.movesUndone.last(), game.board)
                game.currentTurn.value =
                    if (game.currentTurn.value == playerDark) playerWhite else playerDark
            } else {
                game.redoMove(game.movesUndone.last(), game.board)
            }
            _piecesKilled.value = game.piecesKilled
            val updatedBoard = game.board.clone()
            _boardState.value =
                _boardState.value.copy(board = updatedBoard, currentPlayer = game.currentTurn.value)
        }
    }

}

data class Move(
    val from: Position,
    val to: Position,
    val moveId: Long = System.currentTimeMillis() // Unique identifier for each move
)

data class BoardState(
    var board: Board?,
    val currentPlayer: Player?,
    var gameStatus: GameStatus = GameStatus.ACTIVE,
    var lastMovedFrom: Position? = Position(0, 0)
)