package com.example.chess.presentation

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chess.R
import com.example.chess.data.local.PuzzleDao
import com.example.chess.data.local.PuzzleEntity
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
import com.example.chess.data.repository.MoveExplainer
import com.example.chess.domain.model.GameSummary
import com.example.chess.domain.model.MoveExplanationRequest
import com.example.chess.domain.model.MoveInsight
import com.example.chess.presentation.model.VisualPiece
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
 
private const val TAG = "ChessGameViewModel"

@HiltViewModel
class ChessGameViewModel @Inject constructor(
    private val game: GamePlay,
    private val moveExplainer: MoveExplainer,
    private val puzzleDao: PuzzleDao,
    @ApplicationContext context: Context
) : ViewModel() {

    private val _lastMove = MutableStateFlow<Move?>(null)
    val lastMove: StateFlow<Move?> = _lastMove.asStateFlow()

    private val _analysisProgress = MutableStateFlow(0f)
    val analysisProgress: StateFlow<Float> = _analysisProgress.asStateFlow()

    // Add loading state for explanation
    private val _explanationLoading = MutableStateFlow(false)
    val explanationLoading: StateFlow<Boolean> = _explanationLoading.asStateFlow()

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
    private val _movesMade = MutableStateFlow<List<String>>(listOf())
    val movesMade: StateFlow<List<String>> = _movesMade.asStateFlow()

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

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private var autoReviewJob: Job? = null
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying

    // Guards nextMove()/previousMove() against overlapping invocations. _currentMoveIndex
    // advances partway through each of those functions, well before their closing
    // delay(MOVE_ANIMATION_TIME) — so a second tap on Next/Previous during that window used
    // to launch a second coroutine that mutated the same game.board/_visualPieces mid-animation,
    // making pieces jump instead of animating smoothly.
    private val _isTransitioning = MutableStateFlow(false)
    val isTransitioning: StateFlow<Boolean> = _isTransitioning.asStateFlow()

    // Tracks the in-flight AI move-explanation coroutine so a new Next/Previous tap
    // cancels the previous one instead of letting concurrent Gemini calls pile up.
    private var explanationJob: Job? = null

    // Set once a Gemini call fails with a quota/rate-limit error (HTTP 429), so we stop
    // firing a doomed network request (and its CPU/IO cost) on every remaining move step
    // for the rest of this session.
    private var aiExplanationDisabled = false

    fun totalMovesCount(): Int = _movesToSimulate.size
    private var aiJob: Job? = null
    private var isPromotionPending = false

    private lateinit var soundPool: SoundPool
    private var moveSoundId: Int = 0
    private var captureSoundId: Int = 0

    private val _visualPieces = MutableStateFlow<ImmutableList<VisualPiece>>(persistentListOf())
    val visualPieces = _visualPieces.asStateFlow()

    private val _currentInsight = MutableStateFlow<MoveInsight?>(null)
    val currentInsight: StateFlow<MoveInsight?> = _currentInsight.asStateFlow()

    private val _gameSummary = MutableStateFlow<GameSummary?>(null)
    val gameSummary: StateFlow<GameSummary?> = _gameSummary.asStateFlow()

    private val _bestMoveArrow = MutableStateFlow<String?>(null)
    val bestMoveArrow: StateFlow<String?> = _bestMoveArrow.asStateFlow()

    private val _playerMoveArrow = MutableStateFlow<String?>(null)
    val playerMoveArrow: StateFlow<String?> = _playerMoveArrow.asStateFlow()

    private fun buildPGN(upToIndex: Int): String {
        return _movesToSimulateSAN
            .take(upToIndex)
            .chunked(2)
            .mapIndexed { i, pair ->
                "${i + 1}. ${pair[0]}${if (pair.size > 1) " ${pair[1]}" else ""}"
            }
            .joinToString(" ")
    }

    init {
        viewModelScope.launch {
            game.stockfish.startStockfish("${context.applicationInfo.nativeLibraryDir}/lib_chess.so")
            game.stockfish.sendCommand("uci")
        }
    }

    fun initializeSounds(context: Context) {
        // Re-entering the game screen re-triggers this (LaunchedEffect(Unit) fires on every
        // fresh composition), so release any previously-loaded SoundPool first — otherwise
        // each Home -> Play -> Quit -> Play cycle leaks a native SoundPool instance.
        if (::soundPool.isInitialized) {
            soundPool.release()
        }
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
        val explanation =
            generateExplanation(category, bestMove, chessPiece, mateIn, matingPiece, tactic)
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
            isPromotionPending = false
            if (gameMode == GameMode.FRIEND) {
                val updatedWhitePlayer = playerWhite.copy(playerType = PlayerType.HUMAN)
                val updatedDarkPlayer = playerDark.copy(playerType = PlayerType.HUMAN)
                game.init(updatedWhitePlayer, updatedDarkPlayer, true)
                _visualPieces.value = createInitialVisualPieces(game.board)
                game.movesPlayed.clear()
                _boardState.value =
                    BoardState(game.board, game.currentTurn.value, game.status, null)
            } else {
                Log.d(TAG, "setGameMode: running")
                game.stockfish.sendCommand("ucinewgame")
            }
        }
    }

    private fun syncVisualPiecesWithBoard(
        board: Board,
        previousPositions: Map<String, Position> = _visualPieces.value.associate { it.id to it.position }
    ) {
        val updated = board.boxes.flatten().mapNotNull { spot ->
            spot.chessPiece?.let { piece ->
                VisualPiece(
                    id = piece.id,
                    asset = piece.vectorAsset,
                    color = piece.color,
                    position = spot.position,
                    previousPosition = previousPositions[piece.id]  // always the pre-move position
                )
            }
        }.toPersistentList()

        _visualPieces.value = updated
    }

    fun setAILevel(aiLevel: AILevel) {
        _level.value = aiLevel
    }

    // game.movesPlayed/piecesKilled are mutated from Dispatchers.Default (AI move handling)
    // while read here on the Main thread. The lists are individually thread-safe
    // (synchronizedList), but snapshotting/iterating them still needs an explicit
    // synchronized block — these two helpers make that consistent everywhere instead of
    // relying on each call site to remember it.
    private fun movesPlayedSnapshot(): MutableList<com.example.chess.data.model.Move> =
        synchronized(game.movesPlayed) { game.movesPlayed.toMutableList() }

    private fun piecesKilledSnapshot(): MutableList<ChessPiece> =
        synchronized(game.piecesKilled) { game.piecesKilled.toMutableList() }

    private fun playersForCurrentMode(): Pair<Player, Player> {
        if (_gameMode.value != GameMode.COMPUTER) {
            return playerWhite.copy(playerType = PlayerType.HUMAN) to
                    playerDark.copy(playerType = PlayerType.HUMAN)
        }

        val humanIsWhite = _color.value
        val white = playerWhite.copy(
            playerType = if (humanIsWhite) PlayerType.HUMAN else PlayerType.AI
        )
        val black = playerDark.copy(
            playerType = if (humanIsWhite) PlayerType.AI else PlayerType.HUMAN
        )
        return white to black
    }

    fun setColor(isWhite: Boolean) {
        _color.value = isWhite
        isPromotionPending = false

        if (_gameMode.value == GameMode.COMPUTER) {
            val (updatedWhitePlayer, updatedDarkPlayer) = playersForCurrentMode()
            game.init(updatedWhitePlayer, updatedDarkPlayer, true)
            _visualPieces.value = createInitialVisualPieces(game.board)
        }

        _boardState.value =
            BoardState(game.board.clone(), game.currentTurn.value, game.status, null)

        // Cancel any existing AI job and start new collector
        aiJob?.cancel()
        aiJob = viewModelScope.launch(Dispatchers.Main) {
            game.currentTurn
                .collect { currentPlayer ->
                    if (currentPlayer?.playerType == PlayerType.AI && !isPromotionPending &&
                        !game.isGameOver
                    ) {
                        handleAIMoveAndUpdateState(currentPlayer)
                    }
                }
        }
    }

    fun analyzeGame() {
        viewModelScope.launch {
            _isAnalyzing.value = true
            val result = game.analyzeMoveScores(
                movesPlayed = _movesMade.value,
                onProgress = { current, total ->
                    // drive the button fill animation with real progress
                    _analysisProgress.value = current.toFloat() / total.toFloat()
                }
            )
            _analysisResult.value = result
            _gameSummary.value = result.gameSummary
            _isAnalyzing.value = false
            savePuzzlesFromBlunders(result.insights)
        }
    }

    // Turns this game's Blunders/Mistakes into replayable puzzles for later practice.
    // Runs after analysis so it never delays showing the review to the player.
    private fun savePuzzlesFromBlunders(insights: List<MoveInsight>) {
        viewModelScope.launch(Dispatchers.IO) {
            val candidates = game.extractPuzzleCandidates(_movesMade.value, insights)
            if (candidates.isEmpty()) return@launch
            val now = System.currentTimeMillis()
            puzzleDao.insertAll(
                candidates.map { candidate ->
                    PuzzleEntity(
                        fen = candidate.fen,
                        correctMoveUci = candidate.correctMoveUci,
                        playedMoveUci = candidate.playedMoveUci,
                        category = candidate.category,
                        sourceGameTimestamp = now,
                        createdAt = now,
                    )
                }
            )
        }
    }

    // Extracted reusable function for AI move handling and state updates
    private suspend fun handleAIMoveAndUpdateState(player: Player) {
        try {
            Log.d(TAG, "AI turn started: ${player.playerType}")
            delay(MOVE_ANIMATION_TIME)
            if (isPromotionPending) return
            game.handleAIMove(player, _level.value)
            syncVisualPiecesWithBoard(game.board)
            val aiLastMove = game.aiLastMove
            val isCapture = aiLastMove?.pieceKilled != null
            if (aiLastMove != null) {
                _aiLastMove.value = Move(aiLastMove.from.position, aiLastMove.to.position)
                Log.d(TAG, "handleAIMoveAndUpdateState: aiLastMove is $aiLastMove")
            }

            // Synchronize access to shared collections
            val movesSnapshot = movesPlayedSnapshot()
            _piecesKilled.value = piecesKilledSnapshot()
            _movesMade.value = game.convertMovesToNotation(movesSnapshot).toMutableList()
            _movesMadeSAN.value = game.convertMovesToSANNotation(movesSnapshot)



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
        _showPawnPromotion.value = false

        // ✅ Get the actual Move from movesPlayed
        val lastMove = game.movesPlayed.lastOrNull()
        if (lastMove == null) {
            isPromotionPending = false
            return
        }
        val targetSpot = game.board.getBox(lastMove.to.position.row, lastMove.to.position.column)

        targetSpot.chessPiece = promotedPiece
        promotedPiece.position = targetSpot.position

        // ✅ Set promotion on the Move that's already in movesPlayed
        lastMove.promotionPiece = promotedPiece

        game._promotionPieceFlow.value = null
        _promotionPiece.value = null
        isPromotionPending = false

        syncAfterBoardChange()
        // syncAfterBoardChange() clones the board so Compose sees a fresh reference and
        // recomposes — but that leaves _boardState.board pointed at an orphaned clone.
        // The next square tap would resolve its Spot (and thus its ChessPiece) from that
        // clone instead of the live board, corrupting things like hasMoved. Restore the
        // live reference, same as movePieceTo already does after its own move.
        _boardState.value = _boardState.value.copy(board = game.board)

        val currentPlayer = game.currentTurn.value
        if (currentPlayer?.playerType == PlayerType.AI && !game.isGameOver) {
            viewModelScope.launch(Dispatchers.Main) {
                handleAIMoveAndUpdateState(currentPlayer)
            }
        }
    }

    private fun createInitialVisualPieces(board: Board): ImmutableList<VisualPiece> {
        return board.boxes.flatten().mapNotNull { spot ->
            spot.chessPiece?.let { piece ->
                VisualPiece(
                    id = piece.id,
                    asset = piece.vectorAsset,
                    color = piece.color,
                    position = spot.position
                )
            }
        }.toPersistentList()
    }

    fun startNewGame() {
        viewModelScope.launch {
            val (whitePlayer, blackPlayer) = playersForCurrentMode()
            game.init(whitePlayer, blackPlayer, true)
            _visualPieces.value = createInitialVisualPieces(game.board)
//            game.isGameOver = false
            game.movesPlayed.clear()
            _movesMade.value = emptyList<String>().toMutableList()
            _movesMadeSAN.value = emptyList<SANMovePair>().toMutableList()
            //game.piecesKilled.clear()
            _piecesKilled.value = emptyList<ChessPiece>().toMutableList()
            _selectedPiecePosition.value = null
            _validMoves.value = emptyList()
            _lastMove.value = null
            _aiLastMove.value = null
            _boardState.value = _boardState.value.copy(
                gameStatus = GameStatus.ACTIVE
            )
            _promotionPiece.value = null
            isPromotionPending = false
            _boardState.value = BoardState(game.board, game.currentTurn.value, game.status, null)
            // Cancel any existing AI job and start new collector
            aiJob?.cancel()
            aiJob = viewModelScope.launch(Dispatchers.Main) {
                game.currentTurn
                    .collect { currentPlayer ->
                        if (currentPlayer?.playerType == PlayerType.AI && !isPromotionPending &&
                            !game.isGameOver
                        ) {
                            handleAIMoveAndUpdateState(currentPlayer)
                        }
                    }
            }
            Log.d(TAG, "startNewGame: ${game.board.boxes}")
        }
    }

    fun resetGameStatus() {
        // "Show Board" dismisses the checkmate/stalemate dialog without starting a new
        // game — game.isGameOver must be cleared too, or handleHumanMove keeps rejecting
        // every subsequent move attempt with no visible feedback.
        game.isGameOver = false
        game.status = GameStatus.ACTIVE
        _boardState.value = _boardState.value.copy(
            gameStatus = GameStatus.ACTIVE
        )
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
        isPromotionPending = false
        aiJob?.cancel()
    }

    private fun detectCheck(): Boolean? {
        return game.currentTurn.value?.let {
            game.detectCheck(it)
        }
    }

    private fun getKingPosition() {
        // Compare only isWhite, not the whole Player — Player is a data class, so comparing
        // against the playerWhite/playerDark templates (always playerType = HUMAN) would
        // structurally mismatch whenever the mover is actually an AI, highlighting the
        // wrong king's check indicator in COMPUTER mode.
        val playerColor = if (game.currentTurn.value?.isWhite == true) Color.WHITE else Color.BLACK
        if (game.currentTurn.value?.let { game.detectCheck(it) } == true) {
            game.findKing(game.board, playerColor)?.position?.let {
                _checkPosition.value = it
            }
        }
    }

    fun checkForPawnPromotion(targetSpot: Spot) {
        val piece = targetSpot.chessPiece ?: return

        val isPromotion = piece is Pawn && (
                (piece.color == Color.WHITE && targetSpot.position.row == 0) ||
                        (piece.color == Color.BLACK && targetSpot.position.row == 7)
                )

        if (!isPromotion) return

        // Check the piece color, not the current turn
        // Turn has already switched by the time this runs
        val (whitePlayer, blackPlayer) = playersForCurrentMode()
        val isHumanPiece = if (piece.color == Color.WHITE) {
            whitePlayer.playerType == PlayerType.HUMAN
        } else {
            blackPlayer.playerType == PlayerType.HUMAN
        }

        if (isHumanPiece) {
            isPromotionPending = true
            _showPawnPromotion.value = true
            _promotionPiece.value = null
        }
    }

    fun movePieceTo(targetPosition: Position) {
        Log.d(TAG, "movePieceTo: piece selected for move")
        val selectedSpot = _selectedPiecePosition.value ?: return
        val targetSpot = game.board.getBox(targetPosition.row, targetPosition.column)

        _lastMove.value = Move(selectedSpot.position, targetPosition)
        _validMoves.value = emptyList()


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
        } ?: emptyList()
    }

    private fun playerMove(start: Spot, end: Spot, isReviewMode: Boolean = false): Boolean {
        val currentPlayer = game.currentTurn.value
        if (currentPlayer == null) {
            Log.e(TAG, "playerMove: no current player — ignoring move attempt")
            return false
        }
        Log.d(TAG, "playerMove: current is $currentPlayer")
        val moved = makeMove(currentPlayer, start, end, game.board, isReviewMode)
        syncVisualPiecesWithBoard(game.board)
        return moved
    }

    private fun makeMove(
        player: Player,
        start: Spot,
        end: Spot,
        board: Board,
        isReviewMode: Boolean = false
    ): Boolean {
        val value = game.handleHumanMove(player, start, end, isReviewMode, board)
        if (value) {
            // Only check promotion if move actually succeeded
            checkForPawnPromotion(end)
        }
        _lastMove.value = Move(start.position, end.position)
        syncAfterBoardChange()
        return value
    }

    fun initReviewMode() {
        val movesSnapshot = movesPlayedSnapshot()
        _movesToSimulate = movesSnapshot
        _movesToSimulateSAN = game.convertMovesToNotation(movesSnapshot)
        Log.d(TAG, "reviewGame: movesToSimulateSAN are $_movesToSimulateSAN")
        val updatedWhitePlayer = playerWhite.copy(playerType = PlayerType.HUMAN)
        val updatedDarkPlayer = playerDark.copy(playerType = PlayerType.HUMAN)
        Log.d(TAG, "reviewGame: moves to simulate are: $_movesToSimulate")
        game.init(updatedWhitePlayer, updatedDarkPlayer, true)
        _movesMade.value = emptyList<String>().toMutableList()
        _currentMoveIndex.value = 0
        _currentMoveCategory.value = null
        // game.init() resets the board in place, but the UI renders _boardState/
        // _visualPieces, not game.board directly — without this, the review screen
        // keeps showing the live game's final position until the first Next tap,
        // and that tap animates pieces from their stale end-game positions.
        _boardState.value = BoardState(game.board, game.currentTurn.value, game.status, null)
        _visualPieces.value = createInitialVisualPieces(game.board)
        _feedback.value = null
        _currentInsight.value = null
        _bestMoveArrow.value = null
        _playerMoveArrow.value = null
    }

    suspend fun nextMove() {
        if (_isTransitioning.value || _currentMoveIndex.value >= _movesToSimulate.size) return
        _isTransitioning.value = true
        try {
            nextMoveInternal()
        } finally {
            _isTransitioning.value = false
        }
    }

    private suspend fun nextMoveInternal() {
        val index = _currentMoveIndex.value
        val move = _movesToSimulate[index]

        // 1. Snapshot positions BEFORE the board changes
        val previousPositions = _visualPieces.value.associate { it.id to it.position }

        // 2. Apply the move to the board
        applyReviewMove(move)

        // 3. Update board state first
        _boardState.value = _boardState.value.copy(
            board = game.board.clone(),
            currentPlayer = game.currentTurn.value,
            gameStatus = game.status,
        )

        // 4. Yield one frame so Compose settles the board state recomposition
        delay(16L)

        // 5. NOW update visual pieces with correct previousPositions — animation fires from here
        syncVisualPiecesWithBoard(game.board, previousPositions)

        val insight = _analysisResult.value?.insights?.getOrNull(index)
        _currentInsight.value = insight

        val sanMove = _movesToSimulateSAN.getOrNull(index) ?: ""
        val category = insight?.category ?: "Best"
        val bestMove = insight?.bestMove
        val played = _movesToSimulateSAN.getOrNull(index)

        if (bestMove != null && played != null && bestMove != played) {
            _bestMoveArrow.value = bestMove
            _playerMoveArrow.value = played
        } else {
            _bestMoveArrow.value = null
            _playerMoveArrow.value = null
        }

        _feedback.value = Feedback(
            move = sanMove,
            type = category,
            explanation = insight?.insightDetail ?: "",
            bestMove = if (category in listOf("Inaccurate", "Mistake", "Blunder"))
                bestMove else null
        )

        insight?.let { launchAiExplanation(it, sanMove, index) }

        _currentMoveIndex.value = index + 1
        _currentMoveCategory.value = category

        // 6. Wait for the animation to complete before returning
        delay(MOVE_ANIMATION_TIME)
    }

    suspend fun previousMove() {
        if (_isTransitioning.value || _currentMoveIndex.value <= 0 || _isPlaying.value) return
        _isTransitioning.value = true
        try {
            previousMoveInternal()
        } finally {
            _isTransitioning.value = false
        }
    }

    private suspend fun previousMoveInternal() {
        val prevIndex = _currentMoveIndex.value - 1
        val move = _movesToSimulate[prevIndex]
        val previousPositions =
            _visualPieces.value.associate { it.id to it.position }// ← explicit Main dispatcher
        undoReviewMove(move)
        syncVisualPiecesWithBoard(game.board, previousPositions)

        val insight = _analysisResult.value?.insights?.getOrNull(prevIndex)
        val sanMove = _movesToSimulateSAN.getOrNull(prevIndex) ?: ""
        val category = insight?.category ?: "Best"
        val bestMove = _analysisResult.value?.bestMoves?.getOrNull(prevIndex)
        val played = _movesToSimulateSAN.getOrNull(prevIndex)

        _currentInsight.value = insight
        _currentMoveIndex.value = prevIndex
        _currentMoveCategory.value = category

        _bestMoveArrow.value = if (played != bestMove) bestMove else null
        _playerMoveArrow.value = if (played != bestMove) played else null

        _feedback.value = Feedback(
            move = sanMove,
            type = category,
            explanation = insight?.insightDetail ?: "",
            bestMove = if (category in listOf("Inaccurate", "Mistake", "Blunder"))
                insight?.bestMove else null
        )

        insight?.let { launchAiExplanation(it, sanMove, prevIndex) }

        _boardState.value = _boardState.value.copy(
            board = game.board.clone(),
            currentPlayer = game.currentTurn.value,
            gameStatus = game.status,
        )
        delay(MOVE_ANIMATION_TIME)
    }

    private fun launchAiExplanation(
        insight: MoveInsight,
        sanMove: String,
        index: Int,
    ) {
        if (aiExplanationDisabled) return
        explanationJob?.cancel()
        explanationJob = viewModelScope.launch {
            _explanationLoading.value = true
            try {
                val request = MoveExplanationRequest(
                    type = insight.category,
                    movePlayed = sanMove,
                    bestMove = insight.bestMove,
                    chessPiece = null,
                    evalDelta = insight.evalLost.toFloat(),
                    tactic = _analysisResult.value?.tactics?.getOrNull(index),
                    mateIn = _analysisResult.value?.missedMates
                        ?.getOrNull(index)?.mateInBefore,
                    matingPiece = null,
                    pgn = buildPGN(index),
                    playerElo = 1200,
                    moveNumber = index + 1,
                    playerColor = if (index % 2 == 0) "White" else "Black",
                )
                val aiExplanation = withTimeout(4000) {
                    moveExplainer.explain(request)
                }
                // Only update if still on the same move
                if (_currentMoveIndex.value == index + 1) {
                    _feedback.value = _feedback.value?.copy(
                        explanation = aiExplanation
                    )
                }
            } catch (e: Exception) {
                // Deterministic insight already shown — fall back to it silently from the
                // user's perspective, but still log so a real failure is diagnosable.
                Log.e(TAG, "launchAiExplanation: AI explanation failed, using deterministic insight", e)
                if (e.message?.contains("429") == true) {
                    aiExplanationDisabled = true
                }
            } finally {
                _explanationLoading.value = false
            }
        }
    }


    private fun applyReviewMove(move: com.example.chess.data.model.Move) {
        val piece = move.pieceMoved ?: return
        val fromBox = game.board.getBox(move.from.position.row, move.from.position.column)
        val toBox = game.board.getBox(move.to.position.row, move.to.position.column)

        when {
            move.isCastlingMove -> {
                (piece as? com.example.chess.data.model.King)
                    ?.castlingMove(fromBox, toBox, game.board)
            }

            else -> {
                fromBox.chessPiece = null
                toBox.chessPiece = piece
                piece.position = toBox.position
            }
        }

        // Advance turn manually
        game.changeCurrentTurn()
    }

    // Clean move undo for review — never mutates the move object
    private fun undoReviewMove(move: com.example.chess.data.model.Move) {
        val fromBox = game.board.getBox(move.from.position.row, move.from.position.column)
        val toBox = game.board.getBox(move.to.position.row, move.to.position.column)

        when {
            move.isCastlingMove -> {
                // Reverse castling
                game.undoMove(move, game.board)
            }

            else -> {
                // Restore pieces to original squares
                fromBox.chessPiece = move.pieceMoved
                move.pieceMoved?.position = fromBox.position
                toBox.chessPiece = move.pieceKilled  // restores captured piece if any
            }
        }

        game.changeCurrentTurn()
    }

    fun launchNextMove() {
        viewModelScope.launch(Dispatchers.Main) { nextMove() }
    }

    fun launchPreviousMove() {
        viewModelScope.launch(Dispatchers.Main) { previousMove() }
    }

    fun startAutoReview() {
        autoReviewJob?.cancel()
        _isPlaying.value = true
        autoReviewJob = viewModelScope.launch(Dispatchers.Main) {
            while (_currentMoveIndex.value < _movesToSimulate.size) {
                if (!_isPlaying.value) break
                nextMove()
                val displayTime = if (_currentMoveCategory.value in
                    listOf("Brilliant", "Mistake", "Blunder")
                ) 5000L else 3000L
                delay(displayTime)  // 450ms matches the animation duration
            }
            _isPlaying.value = false
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
        if (game.movesPlayed.isEmpty()) return
        Log.d(TAG, "undoMove: movesMade are: ${game.movesPlayed}")

        // playerWhite/playerDark are hard-coded HUMAN templates — never mutated — so
        // comparing their playerType here was always false. What actually determines
        // whether the AI made the last move is the current game mode.
        if (_gameMode.value == GameMode.COMPUTER) {
            // Undo both AI move and player move together
            if (game.movesPlayed.isNotEmpty()) {
                game.undoMoveByPlayer(game.movesPlayed.last())
            }
            if (game.movesPlayed.isNotEmpty()) {
                game.undoMoveByPlayer(game.movesPlayed.last())
            }
            // After undoing 2 moves turn is back to the human player — no manual switch needed
        } else {
            game.undoMoveByPlayer(game.movesPlayed.last())
            // Switch turn back to the player who just had their move undone
            game.changeCurrentTurn()
        }

        refreshLastMoveHighlight()
        syncAfterBoardChange()
        // Restore the live board reference — see the comment in setPromotedPiece() for why.
        _boardState.value = _boardState.value.copy(board = game.board)
    }

    fun redoMove() {
        if (game.movesUndone.isEmpty()) return
        Log.d(TAG, "redoMove: movesUndone are: ${game.movesUndone}")

        if (_gameMode.value == GameMode.COMPUTER) {
            // Redo player move — makeMove inside redoMove handles turn switch
            game.redoMove(game.movesUndone.last(), game.board)
            // Redo AI move if available
            if (game.movesUndone.isNotEmpty()) {
                game.redoMove(game.movesUndone.last(), game.board)
            }
            // makeMove already called changeCurrentTurn twice — turn is correct
        } else {
            game.redoMove(game.movesUndone.last(), game.board)
        }

        refreshLastMoveHighlight()
        syncAfterBoardChange()
        // Restore the live board reference — see the comment in setPromotedPiece() for why.
        _boardState.value = _boardState.value.copy(board = game.board)
    }

    // lastMove/aiLastMove both feed the same square-highlight state (see
    // buildSquareUiStates), so after an Undo/Redo just recompute a single source of
    // truth from the real move history instead of leaving whichever field was set by
    // the move that's now been undone/replayed.
    private fun refreshLastMoveHighlight() {
        val newLastMove = game.movesPlayed.lastOrNull()
        _lastMove.value = newLastMove?.let { Move(it.from.position, it.to.position) }
        _aiLastMove.value = null
    }

    // Single source of truth for post-move UI sync
    private fun syncAfterBoardChange() {
        val updatedBoard = game.board.clone()
        syncVisualPiecesWithBoard(updatedBoard)

        val movesSnapshot = movesPlayedSnapshot()
        _piecesKilled.value = piecesKilledSnapshot()     // snapshot, not reference
        _movesMade.value = game.convertMovesToNotation(movesSnapshot)
        _movesMadeSAN.value = game.convertMovesToSANNotation(movesSnapshot)
        _isInCheck.value = game.currentTurn.value?.let { game.detectCheck(it) } ?: false

        if (_isInCheck.value) getKingPosition()

        _boardState.value = _boardState.value.copy(
            board = updatedBoard,
            currentPlayer = game.currentTurn.value,
            gameStatus = game.status,
        )

        Log.d(TAG, "syncAfterBoardChange: turn is ${game.currentTurn.value}")
    }

    override fun onCleared() {
        super.onCleared()
        if (::soundPool.isInitialized) {
            soundPool.release()
        }
        game.stockfish.stopStockfish()
    }

    companion object {
        private const val MOVE_ANIMATION_TIME = 450L
    }

}

data class Move(
    val from: Position,
    val to: Position,
    val moveId: Long = System.currentTimeMillis() // Unique identifier for each move
)

data class BoardState(
    val board: Board?,
    val currentPlayer: Player?,
    val gameStatus: GameStatus = GameStatus.ACTIVE,
    val lastMovedFrom: Position? = Position(0, 0)
)
