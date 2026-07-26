package com.example.chess.data.repository

import android.util.Log
import com.example.chess.R
import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.AILevel
import com.example.chess.data.model.AnalysisResult
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.CaptureOpportunity
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.MissedMate
import com.example.chess.data.model.Move
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.PuzzleCandidate
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.SANMovePair
import com.example.chess.data.model.Spot
import com.example.chess.domain.model.GamePhase
import com.example.chess.domain.model.GameSummary
import com.example.chess.domain.model.MoveInsight
import com.example.chess.domain.model.InsightType
import com.example.chess.domain.model.PositionEval
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Collections
import java.util.Locale
import javax.inject.Inject
import kotlin.math.abs

private const val TAG = "GamePlay"

// Ply index before which a move is always considered part of the opening, regardless
// of material (nothing's been traded yet either way at this point in a normal game).
private const val OPENING_PLY_CUTOFF = 12

// Combined non-pawn, non-king material (both sides; 62 at game start) at or below which
// a position counts as the endgame rather than the middlegame.
private const val ENDGAME_MATERIAL_THRESHOLD = 13

// Minimum eval-loss (cp) a missed profitable capture must cost before it's labeled
// "Miss" instead of whatever plain category tier the loss alone would produce.
private const val MIN_MISS_LOSS_CP = 50

// Minimum eval gap (cp) between the best and 2nd-best line required for a Blunder/
// Mistake to be considered "solution-unique" enough to export as a puzzle.
private const val MIN_UNIQUE_SOLUTION_GAP_CP = 100

class GamePlay @Inject constructor(
    var board: Board,
    val stockfish: StockfishEngine
) {
    private lateinit var players: List<Player>
    val currentTurn = MutableStateFlow<Player?>(null)
    var status: GameStatus = GameStatus.ACTIVE
    var isGameOver: Boolean = false
    // Mutated from Dispatchers.Default (AI move handling) and read from the Main thread
    // (ViewModel sync). synchronizedList makes individual add/remove/clear/etc. calls
    // thread-safe by default — callers still need `synchronized(list) { ... }` around any
    // compound operation (snapshotting/iterating), same as the Java docs require.
    val movesPlayed: MutableList<Move> = Collections.synchronizedList(mutableListOf())
    val movesUndone: MutableList<Move> = Collections.synchronizedList(mutableListOf())
    val piecesKilled: MutableList<ChessPiece> = Collections.synchronizedList(mutableListOf())
    lateinit var aiStartPosition: Position

    // StateFlow for the promotion piece
    val _promotionPieceFlow = MutableStateFlow<ChessPiece?>(null)
    private val promotionPieceFlow: StateFlow<ChessPiece?> = _promotionPieceFlow.asStateFlow()

    //AI last move
    var aiLastMove: Move? = null

    data class SingleMoveResult(
        val category: String,
        val loss: Int,
        val evalAfterFromWhite: Int,
        val bestMove: String,
        val captureOpportunity: CaptureOpportunity?,
        val missedMate: MissedMate?,
        val tactics: List<String>,
        val isWhiteMove: Boolean,
        val nonPawnMaterial: Int,
    )

    fun init(p1: Player, p2: Player, canClearMoves: Boolean) {
        isGameOver = false
        players = listOf(p1, p2)
        board.boxes = board.resetBoard()

        currentTurn.value = if (p1.isWhite) {
            p1
        } else {
            p2
        }
        Log.d(TAG, "init: current turn is ${currentTurn.value}")
        if (canClearMoves) {
            movesPlayed.clear()
            movesUndone.clear()
            piecesKilled.clear()
        }
        status = GameStatus.ACTIVE
    }

    fun findCriticalMoment(
        losses: List<Int>,
        categories: List<String>
    ): Int {
        // The move with the highest single-move swing
        return losses.indices.maxByOrNull { i ->
            val weight = when (categories.getOrNull(i)) {
                "Blunder" -> 3
                "Mistake" -> 2
                "MissedMate" -> 4
                else -> 1
            }
            losses[i] * weight
        } ?: -1
    }

    fun handleHumanMove(
        player: Player,
        start: Spot,
        end: Spot,
        isReviewMode: Boolean,
        board: Board
    ): Boolean {
        Log.d(TAG, "handleHumanMove: function started")
        if (isGameOver && !isReviewMode) return false
        Log.d(TAG, "handleHumanMove: function ended")
        val startBox = board.getBox(start.position.row, start.position.column)
        val endBox = board.getBox(end.position.row, end.position.column)
        val move: Move = if (isReviewMode) {
            Move(startBox, endBox, player, startBox.chessPiece)
        } else {
            Move(startBox, endBox, player, start.chessPiece)
        }
        Log.d(TAG, "handleHumanMove reached this point: $move")

        // Make the move
        if (currentTurn.value?.playerType == PlayerType.HUMAN && makeMove(board, move, player)) {
            Log.d(TAG, "handleHumanMove: passed this point")
            Log.d(TAG, "handleHumanMove: move made is $move board state is ${board.boxes}")
            // Validate the move
            if (isMoveValid(player)) {
                checkForWinner(player)
                if (isReviewMode) {
                    changeCurrentTurn()
                    return true
                } else {
                    // A freshly committed move invalidates whatever was previously undone —
                    // replaying it via Redo would insert a stale move into a now-divergent
                    // game history (e.g. two same-color moves in a row).
                    movesUndone.clear()
                    // Always switch the turn and report success, even when this move just
                    // ended the game — the isGameOver check at the top of this function
                    // already blocks any further moves once the game is over, so gating
                    // the turn switch here too serves no purpose. It used to leave
                    // currentTurn stuck on the mating player, which let the "Show Board"
                    // flow allow that same player to move again immediately.
                    changeCurrentTurn()
                    return true
                }
            } else {
                Log.d(TAG, "handleHumanMove: a move undone")
                undoMove(move, board, player)
            }
        }
        return false
    }

    private fun getCaptureOpportunity(
        board: Board,
        from: Position,
        to: Position,
        player: Player
    ): CaptureOpportunity? {
        val attacker = board.getBox(from.row, from.column).chessPiece ?: return null
        val target = board.getBox(to.row, to.column).chessPiece ?: return null

        val attackerValue = attacker.value
        val targetValue = target.value

        // Step 1: check if square is defended
        val opponent = if (player.isWhite) players[1] else players[0]
        val opponentPieces = getAlivePieces(opponent, board)

        val defended = opponentPieces.any {
            val validMoves = getRemainingValidMoves(it.position, opponent, board)
            to in validMoves
        }

        // Step 2: return info
        val isProfitable = !defended || (targetValue > attackerValue)
        return if (isProfitable) {
            val captureOpportunity = CaptureOpportunity(target.clone(), attacker.clone(), true)

            Log.d(
                TAG,
                "getCaptureOpportunity: capture opportunity at: ${captureOpportunity.target}"
            )
            captureOpportunity
        } else {
            null
        }
    }

    private fun ChessPiece.clone(): ChessPiece = when (this) {
        is Pawn -> Pawn(
            id,
            color,
            Position(position.row, position.column),
            isKilled,
            vectorAsset,
            hasMoved,
            value
        )

        is Queen -> Queen(
            id,
            color,
            Position(position.row, position.column),
            isKilled,
            vectorAsset,
            hasMoved,
            value
        )

        is Rook -> Rook(
            id,
            color,
            Position(position.row, position.column),
            isKilled,
            vectorAsset,
            hasMoved,
            value
        )

        is Bishop -> Bishop(
            id,
            color,
            Position(position.row, position.column),
            isKilled,
            vectorAsset,
            hasMoved,
            value
        )

        is Knight -> Knight(
            id,
            color,
            Position(position.row, position.column),
            isKilled,
            vectorAsset,
            hasMoved,
            value
        )

        is King -> King(
            id,
            color,
            Position(position.row, position.column),
            isKilled,
            vectorAsset,
            hasMoved,
            value
        )
    }

    fun convertToMoveObject(moveStr: String, board: Board, player: Player): Move {
        if (moveStr.length < 4) {
            Log.d(TAG, "convertToMoveObject: Invalid move string: '$moveStr'")
        }
        Log.d(TAG, "convertToMoveObject: board state is ${board.boxes}")

        val fromCol = moveStr[0] - 'a'
        val fromRow = 8 - (moveStr[1] - '0')
        val toCol = moveStr[2] - 'a'
        val toRow = 8 - (moveStr[3] - '0')

        val fromBox = board.getBox(fromRow, fromCol)
        Log.d(TAG, "convertToMoveObject: from: $fromBox")
        val toBox = board.getBox(toRow, toCol)
        Log.d(TAG, "convertToMoveObject: to: $toBox")
        val pieceMoved = fromBox.chessPiece
        var pieceKilled = toBox.chessPiece
        Log.d(TAG, "convertToMoveObject: piece is $pieceMoved")
        Log.d(TAG, "convertToMoveObject: piece is $pieceKilled")
        var promotionPiece: ChessPiece? = null

        if (moveStr.length == 5 && pieceMoved is Pawn) {
            val promotionChar = moveStr[4]
            promotionPiece = when (promotionChar.lowercaseChar()) {
                'q' -> Queen(color = pieceMoved.color, position = toBox.position, isKilled = false)
                'r' -> Rook(color = pieceMoved.color, position = toBox.position, isKilled = false)
                'b' -> Bishop(color = pieceMoved.color, position = toBox.position, isKilled = false)
                'n' -> Knight(color = pieceMoved.color, position = toBox.position, isKilled = false)
                else -> null
            }
        }

        _promotionPieceFlow.value = promotionPiece

        // A king moving two squares horizontally is, by the rules this engine enforces
        // elsewhere (King.canMove), only ever possible via castling.
        val isCastlingMove = pieceMoved is King && fromRow == toRow && abs(toCol - fromCol) == 2

        // The captured pawn sits beside the mover (same row as `from`, same column as
        // `to`), not on `to` — override pieceKilled to point at it instead of the
        // (empty) destination square.
        val isEnPassant = pieceMoved is Pawn && pieceMoved.isEnPassant(board, fromBox, toBox)
        if (isEnPassant) {
            pieceKilled = board.getBox(fromRow, toCol).chessPiece
        }

        return Move(
            from = fromBox,
            to = toBox,
            player = player,
            pieceMoved = pieceMoved,
            pieceKilled = pieceKilled,
            isCastlingMove = isCastlingMove,
            promotionPiece = promotionPiece,
            isEnPassant = isEnPassant
        )
    }

    // Replays a historical move onto an isolated analysis board (never the live game
    // board/state — no movesPlayed/piecesKilled/currentTurn bookkeeping here). Unlike the
    // old version, this correctly handles castling (moves the rook too), en passant
    // (removes the captured pawn from beside the destination, not the destination itself),
    // and promotion (places the promoted piece, not the original pawn) — previously any
    // game containing one of these silently corrupted the rest of the analysis replay.
    fun applyMove(move: Move, replayBoard: Board) {
        val piece = move.pieceMoved ?: return
        val fromBox = move.from
        val toBox = move.to

        when {
            move.isEnPassant && piece is Pawn -> {
                piece.performEnPassant(replayBoard, fromBox, toBox)
                replayBoard.enPassantTarget = null
            }

            move.isCastlingMove && piece is King -> {
                piece.castlingMove(fromBox, toBox, replayBoard)
                replayBoard.enPassantTarget = null
            }

            move.promotionPiece != null -> {
                move.promotionPiece?.position = toBox.position
                toBox.chessPiece = move.promotionPiece
                fromBox.chessPiece = null
                replayBoard.enPassantTarget = null
            }

            else -> {
                toBox.chessPiece = piece
                fromBox.chessPiece = null
                piece.position = toBox.position
                replayBoard.enPassantTarget =
                    if (piece is Pawn && abs(toBox.position.row - fromBox.position.row) == 2) {
                        Position(
                            (fromBox.position.row + toBox.position.row) / 2,
                            fromBox.position.column
                        )
                    } else {
                        null
                    }
            }
        }

        Log.d(TAG, "applyMove: applied move $move")
    }

    suspend fun analyzeMoveScores(
        movesPlayed: List<String>,
        onProgress: ((Int, Int) -> Unit)? = null
    ): AnalysisResult {

        val positionEvals = evaluateAllPositions(movesPlayed, onProgress)
        val firstBoard = Board()
        val recognizer = TacticsRecognizer(this)

        val rawResults = movesPlayed.indices.map { i ->
            analyzeSingleMove(i, movesPlayed, positionEvals, firstBoard, recognizer)
        }

        // Build insights from raw results
        val insights = rawResults.mapIndexed { i, raw ->
            generateInsight(
                moveIndex = i,
                playerMove = movesPlayed[i],
                bestMove = raw.bestMove,
                evalBefore = positionEvals[i],
                evalAfter = positionEvals[i + 1],
                tactics = raw.tactics,
                totalMoves = movesPlayed.size,
                category = raw.category,
                nonPawnMaterial = raw.nonPawnMaterial,
            )
        }

        // Mark the single most critical moment — weighted by category severity, not a
        // raw eval-swing max, so a smaller-cp MissedMate/Blunder can outrank a merely
        // larger-cp Inaccurate.
        val criticalIndex = findCriticalMoment(
            rawResults.map { it.loss },
            rawResults.map { it.category },
        )
        val markedInsights = insights.mapIndexed { i, insight ->
            insight.copy(isCriticalMoment = i == criticalIndex)
        }

        val summary = generateGameSummary(markedInsights, criticalIndex)
        val result = buildAnalysisResult(rawResults)

        return result.copy(
            insights = markedInsights,
            gameSummary = summary,
            criticalMoveIndex = criticalIndex,
        )
    }

    // Turns a just-analyzed game's Blunders/Mistakes into replayable puzzles: the
    // position right before the mistake, plus the correct move the player missed.
    // Deliberately a separate post-processing pass over analyzeMoveScores' own output,
    // rather than folded into that loop, so it can't risk the already-working analysis.
    fun extractPuzzleCandidates(
        movesPlayed: List<String>,
        insights: List<MoveInsight>,
    ): List<PuzzleCandidate> {
        val whitePlayer = Player(isWhite = true, playerType = PlayerType.HUMAN)
        val blackPlayer = Player(isWhite = false, playerType = PlayerType.HUMAN)

        return insights.mapIndexedNotNull { index, insight ->
            if (insight.category != "Blunder" && insight.category != "Mistake") return@mapIndexedNotNull null
            // No second-best-line data (e.g. a forced-mate position) is treated as
            // "allow" rather than reject, so missed-mate puzzles aren't filtered out.
            if ((insight.solutionGap ?: MIN_UNIQUE_SOLUTION_GAP_CP) < MIN_UNIQUE_SOLUTION_GAP_CP) return@mapIndexedNotNull null

            val replayBoard = Board()
            for (ply in 0 until index) {
                val player = if (ply % 2 == 0) whitePlayer else blackPlayer
                val move = convertToMoveObject(movesPlayed[ply], replayBoard, player)
                applyMove(move, replayBoard)
            }

            val activeColor = if (index % 2 == 0) Color.WHITE else Color.BLACK
            PuzzleCandidate(
                fen = FenConverter.toFen(replayBoard, activeColor),
                correctMoveUci = insight.bestMove,
                playedMoveUci = insight.move,
                category = insight.category,
            )
        }
    }

    fun generateInsight(
        moveIndex: Int,
        playerMove: String,
        bestMove: String,
        evalBefore: PositionEval,
        evalAfter: PositionEval,
        tactics: List<String>,
        totalMoves: Int,
        category: String,
        nonPawnMaterial: Int,
    ): MoveInsight {

        val evalLost = calculateLoss(evalBefore.score, evalAfter.score, moveIndex % 2 == 0)
        val gamePhase = when {
            moveIndex < OPENING_PLY_CUTOFF -> GamePhase.OPENING
            nonPawnMaterial <= ENDGAME_MATERIAL_THRESHOLD -> GamePhase.ENDGAME
            else -> GamePhase.MIDDLEGAME
        }
        val solutionGap = evalBefore.secondBestScore?.let { second -> evalBefore.score - second }

        // Was the player's move the best, 2nd best, 3rd best?
        val playerMoveRank = when {
            playerMove == bestMove -> 1
            evalLost < 20 -> 2   // close to best
            evalLost < 60 -> 3
            else -> 4   // not in top 3
        }

        val (insightType, insightDetail) = when {

            // Brilliant tactic found — but only praise it if the engine agrees the move
            // was actually good. A matched tactic pattern only proves the tactic-executing
            // piece is safe on its own landing square, not that exploiting the tactic
            // (e.g. capturing a "pinned" piece that's defended elsewhere) is sound.
            tactics.isNotEmpty() && evalLost <= 50 -> InsightType.BRILLIANT_TACTIC to
                    buildTacticFoundMessage(tactics, playerMove)

            // Missed mate
            isMissedMate(evalBefore.mateIn, evalAfter.mateIn, playerMove, bestMove) ->
                InsightType.MISSED_MATE to
                        "You had checkmate in ${evalBefore.mateIn} but missed it. " +
                        "${bestMove.toReadable()} would have forced mate."

            // Material blunder — dropped a piece
            evalLost > 250 && gamePhase != GamePhase.ENDGAME ->
                InsightType.MATERIAL_BLUNDER to
                        buildMaterialBlunderMessage(
                            evalLost,
                            bestMove,
                            evalBefore.principalVariation
                        )

            // Missed tactic
            evalLost > 100 && evalBefore.principalVariation.isNotEmpty() ->
                InsightType.MISSED_TACTIC to
                        buildMissedTacticMessage(bestMove, evalBefore.principalVariation, evalLost)

            // Endgame error
            evalLost > 80 && gamePhase == GamePhase.ENDGAME ->
                InsightType.ENDGAME_ERROR to
                        "Endgame precision matters — ${bestMove.toReadable()} kept the advantage. " +
                        "Your move let the opponent back into the game."

            // Positional inaccuracy
            evalLost in 40..100 ->
                InsightType.POSITIONAL_ERROR to
                        buildPositionalMessage(evalLost, playerMoveRank, bestMove)

            // Good defense
            evalLost <= 0 && evalBefore.score < -100 ->
                InsightType.GOOD_DEFENSE to
                        "Strong defensive move. You held the position under pressure."

            // Opening deviation
            gamePhase == GamePhase.OPENING && evalLost > 30 ->
                InsightType.OPENING_DEVIATION to
                        "This deviates from the best opening plan. ${bestMove.toReadable()} " +
                        "keeps better piece coordination."

            else -> InsightType.POSITIONAL_ERROR to
                    buildGenericMessage(category, evalLost, bestMove, playerMoveRank)
        }

        return MoveInsight(
            move = playerMove,
            category = category,
            evalBefore = evalBefore.score,
            evalAfter = evalAfter.score,
            evalLost = evalLost,
            bestMove = bestMove,
            bestContinuation = evalBefore.principalVariation.take(4),
            playerMoveRank = playerMoveRank,
            insight = insightType,
            insightDetail = insightDetail,
            isCriticalMoment = false,  // set after all moves analyzed
            gamePhase = gamePhase,
            solutionGap = solutionGap,
        )
    }

    private fun buildTacticFoundMessage(tactics: List<String>, move: String): String {
        val tacticName = tactics.first()
        return when (tacticName) {
            "Fork" -> "Nice fork! ${move.toReadable()} attacks two pieces at once — your opponent is losing material."
            "Pin" -> "Good pin! You've tied down an enemy piece — it can't move without losing something more valuable."
            "Skewer" -> "Skewer! The stronger piece must move, exposing the weaker one behind it."
            "Discovered Check" -> "Discovered check! Moving one piece revealed an attack from another — a powerful double threat."
            "Discovered Attack" -> "Discovered attack! Moving one piece revealed another attacking a valuable enemy piece."
            "Double Check" -> "Double check! Extremely powerful — the king must move, with no other way to escape."
            else -> "Strong tactical move — you found $tacticName."
        }
    }

    private fun buildMaterialBlunderMessage(
        evalLost: Int,
        bestMove: String,
        bestLine: List<String>
    ): String {
        val material = when {
            evalLost > 800 -> "your queen"
            evalLost > 450 -> "a rook"
            evalLost > 280 -> "a minor piece"
            else -> "a pawn"
        }
        return "This loses $material. ${bestMove.toReadable()} was needed to keep the position safe."
    }

    private fun buildMissedTacticMessage(
        bestMove: String,
        bestLine: List<String>,
        evalLost: Int
    ): String {
        val continuation = if (bestLine.size >= 2) {
            " followed by ${bestLine[1].toReadable()}"
        } else ""
        return "${bestMove.toReadable()}$continuation was the winning move here — " +
                "you missed an opportunity to gain ${evalLost / 100f}+ pawns of advantage."
    }

    fun generateGameSummary(insights: List<MoveInsight>, criticalIndex: Int): GameSummary {

        val criticalMove = insights.getOrNull(criticalIndex)
        val biggestSwing = criticalMove?.evalLost ?: 0

        val narrative = buildGameNarrative(insights, criticalIndex)
        val weakness = detectWeakness(insights)

        return GameSummary(
            whiteAccuracy = calculateAccuracy(
                insights.filterIndexed { i, _ -> i % 2 == 0 }.map { it.evalLost }, 100.0
            ),
            blackAccuracy = calculateAccuracy(
                insights.filterIndexed { i, _ -> i % 2 != 0 }.map { it.evalLost }, 100.0
            ),
            criticalMoveIndex = criticalIndex,
            criticalMoveDescription = criticalMove?.insightDetail ?: "",
            longestWinningStreak = findLongestStreak(insights),
            biggestMistakeMove = criticalMove?.move ?: "",
            gameNarrative = narrative,
            weaknessHint = weakness,
        )
    }

    private fun buildGameNarrative(insights: List<MoveInsight>, criticalIndex: Int): String {
        val blunders = insights.count { it.category == "Blunder" }
        val brilliants = insights.count { it.category == "Brilliant" }
        val opening = insights.take(10).count { it.evalLost < 30 }
        val endgame = insights.takeLast(10)

        val openingQuality = when {
            opening > 7 -> "strong opening"
            opening > 4 -> "decent opening"
            else -> "shaky opening"
        }

        val midgameStory = when {
            blunders > 2 -> "but the middlegame had critical errors"
            brilliants > 1 -> "with sharp tactical play in the middlegame"
            else -> "with solid but imprecise middlegame play"
        }

        return "A $openingQuality $midgameStory. " +
                if (blunders > 0) "The turning point was move ${criticalIndex + 1}."
                else "No major blunders — a well-played game."
    }

    private fun detectWeakness(insights: List<MoveInsight>): String {
        val endgameErrors = insights.count {
            it.gamePhase == GamePhase.ENDGAME && it.evalLost > 80
        }
        val openingErrors = insights.count {
            it.gamePhase == GamePhase.OPENING && it.evalLost > 30
        }
        val tacticalMisses = insights.count {
            it.insight == InsightType.MISSED_TACTIC
        }
        val materialDrops = insights.count {
            it.insight == InsightType.MATERIAL_BLUNDER
        }

        return when {
            materialDrops > 1 ->
                "Focus on piece safety — check if your pieces are hanging before each move."

            tacticalMisses > 2 ->
                "You're missing tactical patterns. Try 10 puzzles per day focusing on forks and pins."

            endgameErrors > 2 ->
                "Your endgame technique needs work. Study basic king and pawn endgames."

            openingErrors > 3 ->
                "Your opening needs attention. Learn 2-3 solid openings and stick to them."

            else ->
                "Work on calculating 2-3 moves ahead before committing to a move."
        }
    }

    private fun findLongestStreak(insights: List<MoveInsight>): Pair<String, Int> {
        var maxStreak = 0
        var curStreak = 0
        var streakColor = "White"
        var maxColor = "White"

        insights.forEachIndexed { i, insight ->
            val color = if (i % 2 == 0) "White" else "Black"
            if (insight.evalLost <= 30) {
                if (color == streakColor) curStreak++
                else {
                    curStreak = 1; streakColor = color
                }
            } else {
                curStreak = 0
            }
            if (curStreak > maxStreak) {
                maxStreak = curStreak
                maxColor = streakColor
            }
        }
        return Pair(maxColor, maxStreak)
    }

    private fun buildPositionalMessage(
        evalLost: Int,
        rank: Int,
        bestMove: String
    ): String = when {
        rank == 2 -> "Almost the best move — only ${evalLost}cp from optimal. " +
                "${bestMove.toReadable()} was slightly more precise."

        rank == 3 -> "${bestMove.toReadable()} was better, giving you more active piece play."
        else -> "This move gives away ${evalLost}cp of advantage. " +
                "${bestMove.toReadable()} maintains better control."
    }

    private fun buildGenericMessage(
        category: String,
        evalLost: Int,
        bestMove: String,
        rank: Int
    ): String = when (category) {
        "Best" -> if (rank == 1) "The best move in this position."
        else "Very strong — only ${evalLost}cp from perfect."

        "Great" -> "${bestMove.toReadable()} was marginally better but this is a solid choice."
        else -> "${bestMove.toReadable()} was stronger here."
    }

    private fun String.toReadable(): String {
        if (length < 4) return this

        // Handle castling
        if (this == "e1g1" || this == "e8g8") return "O-O"
        if (this == "e1c1" || this == "e8c8") return "O-O-O"

        val from = substring(0, 2)
        val to = substring(2, 4)

        // Promotion suffix
        val promotion = if (length == 5) when (this[4]) {
            'q' -> "=Q"
            'r' -> "=R"
            'b' -> "=B"
            'n' -> "=N"
            else -> ""
        } else ""

        return "$to$promotion"
    }

    private suspend fun evaluateAllPositions(
        movesPlayed: List<String>,
        onProgress: ((Int, Int) -> Unit)?
    ): List<PositionEval> {
        // Analysis must always run at full engine strength, regardless of whatever
        // Skill Level a prior live AI move (handleAIMove) left set on this shared
        // Stockfish process.
        stockfish.setDifficultyLevel(20)
        // Second line lets us gauge how far ahead the best move is of the runner-up —
        // used to gate puzzle solution-uniqueness (extractPuzzleCandidates).
        stockfish.sendCommand("setoption name MultiPV value 2")
        val total = movesPlayed.size + 1
        return (0..movesPlayed.size).map { i ->
            val moves = movesPlayed.subList(0, i).joinToString(" ")
            stockfish.sendCommand("position startpos moves $moves")
            stockfish.sendCommand("go movetime 500")
            val result = stockfish.getDetailedEval()   // ← was getMoveScore()
            onProgress?.invoke(i + 1, total)
            result
        }
    }

    private fun analyzeSingleMove(
        index: Int,
        movesPlayed: List<String>,
        positionEvals: List<PositionEval>,
        firstBoard: Board,
        recognizer: TacticsRecognizer,
    ): SingleMoveResult {
        val isWhiteMove = index % 2 == 0
        val currentPlayer = if (isWhiteMove) players[0] else players[1]
        val playerColor = if (isWhiteMove) Color.WHITE else Color.BLACK
        val partialMove = movesPlayed[index]

        val evalBefore = positionEvals[index]
        val evalAfter = positionEvals[index + 1]

        // Use .score, .bestMove, .mateIn from PositionEval directly
        val loss = calculateLoss(evalBefore.score, evalAfter.score, isWhiteMove)
        val evalAfterFromWhite = if (isWhiteMove) -evalAfter.score else evalAfter.score

        val recommendedMoveObject =
            convertToMoveObject(evalBefore.bestMove, firstBoard, currentPlayer)
        val playerMoveObject = convertToMoveObject(partialMove, firstBoard, currentPlayer)

        var category = categorizeByLoss(loss)
        // Snapshot before this ply's move is applied below, so it matches the position
        // `evalBefore` was computed for.
        val nonPawnMaterial = combinedNonPawnMaterial(firstBoard)

        val targetPosition = recommendedMoveObject.to.position
        val capturedPiece = firstBoard.getBox(targetPosition.row, targetPosition.column).chessPiece
        val captureOpportunity = getCaptureOpportunity(
            firstBoard,
            recommendedMoveObject.from.position,
            targetPosition,
            currentPlayer,
        )

        // Small losses aren't worth flagging as a "Miss" just because the exact
        // recommended capture wasn't played — only demote once it actually cost something.
        if (loss >= MIN_MISS_LOSS_CP && isMissedCapture(
                capturedPiece,
                playerColor,
                captureOpportunity?.isProfitable == true,
                partialMove,
                evalBefore.bestMove,
            )
        ) category = "Miss"

        val missedMate = if (isMissedMate(
                evalBefore.mateIn,
                evalAfter.mateIn,
                partialMove,
                evalBefore.bestMove,
            )
        ) {
            category = "MissedMate"
            MissedMate(
                move = partialMove,
                mateInBefore = evalBefore.mateIn,
                player = currentPlayer,
                moveObject = recommendedMoveObject,
            )
        } else null

        val tactics = listOfNotNull(
            recognizer.isFork(firstBoard, playerMoveObject, currentPlayer),
            recognizer.isPin(firstBoard, playerMoveObject, currentPlayer),
            recognizer.isDiscoveredCheck(firstBoard, playerMoveObject, currentPlayer),
            recognizer.isDiscoveredAttack(firstBoard, playerMoveObject, currentPlayer),
            recognizer.isSkewer(firstBoard, playerMoveObject),
            recognizer.isDoubleCheck(firstBoard, playerMoveObject, currentPlayer),
        )
        // A tactic pattern (Pin/Fork/etc.) only proves the tactic-executing piece is safe
        // on its own landing square — it says nothing about whether a later move actually
        // exploiting it (e.g. capturing a "pinned" piece that's defended elsewhere) is
        // sound. The engine's own loss measurement already captures that; don't let a
        // matched tactic pattern override a move Stockfish says was actually bad.
        if (tactics.isNotEmpty() && loss <= 50) category = "Brilliant"

        applyMove(playerMoveObject, firstBoard)

        return SingleMoveResult(
            category = category,
            loss = loss,
            evalAfterFromWhite = evalAfterFromWhite,
            bestMove = evalBefore.bestMove,
            captureOpportunity = captureOpportunity,
            missedMate = missedMate,
            tactics = tactics,
            isWhiteMove = isWhiteMove,
            nonPawnMaterial = nonPawnMaterial,
        )
    }

    // Combined non-pawn, non-king material remaining for both sides — used to detect
    // the endgame by actual simplification instead of proximity to the game's last move.
    private fun combinedNonPawnMaterial(board: Board): Int =
        players.sumOf { player ->
            getAlivePieces(player, board)
                .filterNot { it is King || it is Pawn }
                .sumOf { it.value }
        }

    fun buildAnalysisResult(results: List<SingleMoveResult>): AnalysisResult {
        val categoryCounts = mutableMapOf(
            "Best" to intArrayOf(0, 0),
            "Great" to intArrayOf(0, 0),
            "Inaccurate" to intArrayOf(0, 0),
            "Mistake" to intArrayOf(0, 0),
            "Blunder" to intArrayOf(0, 0),
            "Brilliant" to intArrayOf(0, 0),
            "Miss" to intArrayOf(0, 0),
            "MissedMate" to intArrayOf(0, 0),
        )

        results.forEach { r ->
            val colorIndex = if (r.isWhiteMove) 0 else 1
            categoryCounts[r.category]?.let { it[colorIndex]++ }
        }

        val whiteLosses = results.filter { it.isWhiteMove }.map { it.loss }
        val blackLosses = results.filter { !it.isWhiteMove }.map { it.loss }
        val scalingFactor = 100.0

        return AnalysisResult(
            evaluations = results.map { it.evalAfterFromWhite },
            whiteAccuracy = String.format(
                Locale.US, "%.1f", calculateAccuracy(whiteLosses, scalingFactor)
            ).toDouble(),
            blackAccuracy = String.format(
                Locale.US, "%.1f", calculateAccuracy(blackLosses, scalingFactor)
            ).toDouble(),
            whiteCategoryCounts = categoryCounts.mapValues { it.value[0] },
            blackCategoryCounts = categoryCounts.mapValues { it.value[1] },
            moveCategories = results.map { it.category },
            bestMoves = results.map { it.bestMove },
            captureOpportunity = results.map { it.captureOpportunity }.toMutableList(),
            missedMates = results.map { it.missedMate }.toMutableList(),
            tactics = results.map { it.tactics }.toMutableList(),
            losses = results.map { it.loss },
            insights = emptyList(),
            gameSummary = null,
            criticalMoveIndex = -1,
        )
    }

    fun calculateLoss(
        scoreBestForPlayer: Int,
        scoreAfterFromOpponent: Int,
        isWhiteMove: Boolean
    ): Int {
        val scoreBestForWhite = if (isWhiteMove) scoreBestForPlayer else -scoreBestForPlayer
        val scoreAfterFromWhite =
            if (isWhiteMove) -scoreAfterFromOpponent else scoreAfterFromOpponent
        return if (isWhiteMove) {
            scoreBestForWhite - scoreAfterFromWhite
        } else {
            scoreAfterFromWhite - scoreBestForWhite
        }
    }

    fun categorizeByLoss(loss: Int): String = when {
        loss <= 10 -> "Best"
        loss <= 50 -> "Great"
        loss <= 100 -> "Inaccurate"
        loss <= 300 -> "Mistake"
        else -> "Blunder"
    }

    fun isMissedMate(
        mateInBefore: Int,
        mateInAfter: Int,
        playerMove: String,
        recommendedMove: String
    ): Boolean = mateInBefore in 1..3
            && mateInAfter == 0
            && playerMove != recommendedMove

    fun isMissedCapture(
        capturedPiece: ChessPiece?,
        playerColor: Color,
        captureIsProfitable: Boolean,
        playerMove: String,
        recommendedMove: String
    ): Boolean = capturedPiece != null
            && capturedPiece.color != playerColor
            && captureIsProfitable
            && playerMove != recommendedMove

    fun calculateAccuracy(losses: List<Number>, scalingFactor: Double): Double {
        if (losses.isEmpty()) return 0.0
        return (
            losses
                .map { maxOf(0.0, it.toDouble()) }
                .map { 1.0 / (1.0 + it / scalingFactor) }
                .average() * 100
        ).coerceIn(0.0, 100.0)
    }

    fun getFoolishMoveWithBias(player: Player, board: Board): Move? {
        val pieces = getAlivePieces(player, board)

        // Prioritize pawns moving forward
        val pawnMoves = mutableListOf<Move>()
        val otherMoves = mutableListOf<Move>()

        for (piece in pieces) {
            val validPositions = getValidMoves(piece.position, player, board)
            for (position in validPositions) {
                val move = Move(
                    board.getBox(piece.position.row, piece.position.column),
                    board.getBox(position.row, position.column),
                    player,
                    piece
                )

                if (piece is Pawn) {
                    pawnMoves.add(move)  // Prefer pawn moves
                } else {
                    otherMoves.add(move)
                }
            }
        }

        // 80% chance to move a pawn if available
        return if (pawnMoves.isNotEmpty() && Math.random() < 0.8) {
            pawnMoves.random()
        } else {
            (pawnMoves + otherMoves).randomOrNull()
        }
    }

    suspend fun handleAIMove(player: Player, level: AILevel): Boolean =
        withContext(Dispatchers.Default) {
            try {
                Log.d(TAG, "handleAIMove: started AI move")
                // Create a snapshot of movesPlayed to avoid concurrent modification
                val movesSnapshot = movesPlayed.toMutableList()
                val movesString = convertMovesToNotation(movesSnapshot).joinToString(" ")
                Log.d(TAG, "handleAIMove: moves played are: $movesString")
                Log.d(
                    TAG,
                    "handleAIMove: movesPlayed snapshot: ${movesSnapshot.joinToString()}"
                ) // Avoid direct toString() on mutable collection

                // Send the position and start Stockfish thinking
                stockfish.sendCommand("position startpos moves $movesString")
                val skillLevel = if (level == AILevel.LEVEL1) 0 else 4
                stockfish.setDifficultyLevel(skillLevel)
                stockfish.sendCommand("go movetime 1")

                val bestMove = stockfish.getBestMove()
                Log.d(TAG, "handleAIMove: moveByStockfish is $bestMove")

                run {
                    // Handle Stockfish move and update game state
                    val move = handleStockfishBestMove(bestMove, player)
                    if (move != null) {
                        aiLastMove = move
                        Log.d(TAG, "handleAIMove: aiLastMove: $aiLastMove")
                        // Synchronize board modifications
                        val moveSuccessful =
                            makeMove(
                                board,
                                move,
                                player
                            ) // Use a cloned board to avoid mid-operation changes
                        if (moveSuccessful) {
                            aiStartPosition = move.from.position
                            Log.d(TAG, "handleAIMove: aiStartPosition set to $aiStartPosition")
                            Log.d(TAG, "handleAIMove: move successful")

                            // Check for winner and update turn safely
                            checkForWinner(player)
                            // A freshly committed move invalidates whatever was previously
                            // undone — see handleHumanMove for the same guard.
                            movesUndone.clear()
                            if (!isGameOver) {
                                changeCurrentTurn()
                                return@withContext true
                            }
                            return@withContext false
                        } else {
                            Log.d(TAG, "handleAIMove: move failed")
                            return@withContext false
                        }
                    } else {
                        Log.d(TAG, "handleAIMove: no valid move parsed from Stockfish")
                        return@withContext false
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in handleAIMove: ${e.message}", e)
                return@withContext false
            }
        }

    fun convertMovesToNotation(allMoves: List<Move>): List<String> {
        Log.d(TAG, "convertMovesToNotation: moves are $allMoves")
        val moves = mutableListOf<String>()

        allMoves.forEach { move ->
            val startPosition = move.from.position.toAlgebraicNotation()
            val endPosition = move.to.position.toAlgebraicNotation()

            // Promotion info is encapsulated with the move
            val promotionSuffix = when (move.promotionPiece) {
                is Queen -> "q"
                is Rook -> "r"
                is Bishop -> "b"
                is Knight -> "n"
                else -> ""
            }

            moves.add(startPosition + endPosition + promotionSuffix)
        }

        return moves
    }

    fun convertMovesToSANNotation(moves: List<Move>): MutableList<SANMovePair> {
        val pairedMoves = mutableListOf<SANMovePair>()
        var moveNumber = 1
        var whitePieceDrawable: Int? = null
        var whiteDestination: String? = null

        moves.forEachIndexed { index, move ->
            val from = move.from.position.toAlgebraicNotation() // e.g., "e2"
            val to = move.to.position.toAlgebraicNotation()     // e.g., "e4"
            val piece = move.pieceMoved!!

            val (pieceDrawable, destination) = when {
                piece.vectorAsset == R.drawable.king_white && from == "e1" -> {
                    when (to) {
                        "g1" -> null to "O-O"
                        "c1" -> null to "O-O-O"
                        else -> piece.vectorAsset to to
                    }
                }

                piece.vectorAsset == R.drawable.king_dark && from == "e8" -> {
                    when (to) {
                        "g8" -> null to "O-O"
                        "c8" -> null to "O-O-O"
                        else -> piece.vectorAsset to to
                    }
                }

                piece.vectorAsset in listOf(R.drawable.pawn_white, R.drawable.pawn_dark) -> {
                    if (from[0] != to[0]) null to "${from[0]}x$to" // Capture or en passant
                    else null to to // Simple pawn move
                }

                else -> piece.vectorAsset to to // Other pieces (knight, bishop, etc.)
            }

            if (index % 2 == 0) {
                whitePieceDrawable = pieceDrawable
                whiteDestination = destination
            } else {
                pairedMoves.add(
                    SANMovePair(
                        moveNumber = moveNumber,
                        whitePieceDrawable = whitePieceDrawable,
                        whiteDestination = whiteDestination ?: "",
                        blackPieceDrawable = pieceDrawable,
                        blackDestination = destination
                    )
                )
                whitePieceDrawable = null
                whiteDestination = null
                moveNumber++
            }
        }

        if (whitePieceDrawable != null) {
            pairedMoves.add(
                SANMovePair(
                    moveNumber = moveNumber,
                    whitePieceDrawable = whitePieceDrawable,
                    whiteDestination = whiteDestination!!,
                    blackPieceDrawable = null,
                    blackDestination = ""
                )
            )
        }

        return pairedMoves
    }

    private fun handleStockfishBestMove(bestMove: String, player: Player): Move? {
        Log.d(TAG, "handleStockfishBestMove: bestMove length is ${bestMove.length}")
        if (bestMove.length < 4) return null
        if (bestMove.length == 4 || bestMove.length == 5) {
            val fromPosition = Position.fromAlgebraicNotation(bestMove.substring(0, 2))
            val toPosition = Position.fromAlgebraicNotation(bestMove.substring(2, 4))

            val fromStr = bestMove.substring(0, 2)

            val fromSpot = board.getBox(fromPosition.row, fromPosition.column)
            val toSpot = board.getBox(toPosition.row, toPosition.column)

            val pieceMoved = fromSpot.chessPiece
            val color = pieceMoved?.color

            if (pieceMoved == null) {
                Log.e(TAG, "Stockfish gave move from empty square: $bestMove")
                return null
            }
            if ((pieceMoved.color == Color.WHITE) != player.isWhite) {
                Log.e(
                    TAG,
                    "Stockfish gave move for wrong color! from=$fromStr piece=$pieceMoved player=$player"
                )
                return null
            }
            val move = Move(
                from = fromSpot,
                to = toSpot,
                player = player,
                pieceMoved = pieceMoved
            )

            if (bestMove.length == 5 && pieceMoved is Pawn) {
                Log.d(TAG, "handleStockfishBestMove: this is pawn promotion")
                move.promotionPiece = when (bestMove[4]) {
                    'q' -> Queen(
                        color = pieceMoved.color,
                        position = toSpot.position,
                        isKilled = false,
                        vectorAsset = if (color == Color.WHITE) R.drawable.queen_white else R.drawable.queen_dark
                    )

                    'r' -> Rook(
                        color = pieceMoved.color,
                        position = toSpot.position,
                        isKilled = false,
                        vectorAsset = if (color == Color.WHITE) R.drawable.rook_white else R.drawable.rook_dark
                    )

                    'b' -> Bishop(
                        color = pieceMoved.color,
                        position = toSpot.position,
                        isKilled = false,
                        vectorAsset = if (color == Color.WHITE) R.drawable.bishop_white else R.drawable.bishop_dark
                    )

                    'n' -> Knight(
                        color = pieceMoved.color,
                        position = toSpot.position,
                        isKilled = false,
                        vectorAsset = if (color == Color.WHITE) R.drawable.knight_white else R.drawable.knight_dark
                    )

                    else -> null
                }
            }
            return move
        }
        return null
    }


    // A move is valid exactly when it doesn't leave the mover's own king in check —
    // this used to compute detectCheck(player) twice (once guarded by a redundant
    // findKing() null-check that detectCheck() already performs internally).
    private fun isMoveValid(player: Player): Boolean = !detectCheck(player)

    private fun makeMove(myBoard: Board, move: Move, player: Player): Boolean {
        val sourcePiece = move.pieceMoved ?: return false

        if (player != currentTurn.value) return false
        if ((sourcePiece.color == Color.WHITE) != player.isWhite) return false

        // Remember the en passant target and the mover's hasMoved flag as they stood
        // before this move, so undo can restore them exactly. The board's en passant
        // target defaults to "none available" below — it's only re-armed if this move
        // is itself a two-square pawn push.
        move.previousEnPassantTarget = myBoard.enPassantTarget
        move.previousHasMoved = sourcePiece.hasMoved
        move.wasCommitted = true

        // En passant
        if (sourcePiece is Pawn && sourcePiece.isEnPassant(myBoard, move.from, move.to)) {
            move.isEnPassant = true
            // The captured pawn sits beside the mover (same row as `from`, same column
            // as `to`) — not on `move.to` — and must be read before performEnPassant()
            // moves the capturing pawn onto that square.
            val capturedPawn =
                myBoard.getBox(move.from.position.row, move.to.position.column).chessPiece
            sourcePiece.performEnPassant(myBoard, move.from, move.to)
            sourcePiece.hasMoved = true
            if (capturedPawn != null) {
                capturedPawn.isKilled = true
                move.pieceKilled = capturedPawn
                piecesKilled.add(capturedPawn)
            }
            movesPlayed.add(move)
            myBoard.enPassantTarget = null
            return true
        }

        // Castling
        if (sourcePiece is King && sourcePiece.canCastle(move.from, move.to, myBoard)) {
            move.isCastlingMove = true
            sourcePiece.castlingMove(move.from, move.to, myBoard)?.hasMoved = true
            sourcePiece.hasMoved = true
            movesPlayed.add(move)
            myBoard.enPassantTarget = null
            return true
        }

        if (!sourcePiece.canMove(myBoard, move.from, move.to)) return false

        val destPiece = move.to.chessPiece

        if (move.promotionPiece != null) {
            if (sourcePiece !is Pawn) return false

            val promotionRow = if (sourcePiece.color == Color.WHITE) 0 else Board.NUM_ROWS - 1
            if (move.to.position.row != promotionRow) return false

            if (destPiece != null) {
                destPiece.isKilled = true
                move.pieceKilled = destPiece
                piecesKilled.add(destPiece)
            }

            move.promotionPiece?.position = move.to.position
            move.to.chessPiece = move.promotionPiece
            move.from.chessPiece = null
            sourcePiece.hasMoved = true
            movesPlayed.add(move)
            myBoard.enPassantTarget = null

            return true
        }

        if (destPiece != null) {
            destPiece.isKilled = true
            move.pieceKilled = destPiece
            piecesKilled.add(destPiece)
        }

        move.to.chessPiece = move.from.chessPiece
        move.from.chessPiece = null
        sourcePiece.hasMoved = true
        movesPlayed.add(move)

        myBoard.enPassantTarget =
            if (sourcePiece is Pawn && abs(move.to.position.row - move.from.position.row) == 2) {
                Position((move.from.position.row + move.to.position.row) / 2, move.from.position.column)
            } else {
                null
            }

        return true
    }

    private fun checkForWinner(player: Player) {
        val opponent = if (player.isWhite) players[1] else players[0]

        if (detectCheck(opponent)) {
            status = if (detectCheckMate(opponent)) {
                // Set game status to opponent win
                if (player.isWhite) {
                    Log.d(TAG, "makeMove: white wins")
                    isGameOver = true
                    GameStatus.WHITE_WIN
                } else {
                    Log.d(TAG, "makeMove: black wins")
                    isGameOver = true
                    GameStatus.BLACK_WIN
                }
            } else {
                // Set game status to check
                Log.d(TAG, "makeMove: player $opponent is in check")
                GameStatus.CHECK
            }
        } else if (isStalemate(opponent, board)) {
            status = GameStatus.STALEMATE
            isGameOver = true
        } else {
            Log.d(TAG, "makeMove: no player in check")
            // Set game status to active
            status = GameStatus.ACTIVE
        }
    }

    private fun simulateMove(move: Move, player: Player): Boolean {
        //no piece available to move
        val sourcePiece = move.pieceMoved ?: return false

        // Valid player
        if (player != currentTurn.value) {
            Log.d(TAG, "determineMove: not current turn")
            return false
        }

        if ((sourcePiece.color == Color.WHITE) != player.isWhite) {
            Log.d(TAG, "determineMove: color problem")
            return false
        }

        if (sourcePiece is Pawn && sourcePiece.isEnPassant(board, move.from, move.to)) {
            // The captured pawn sits beside the mover, not on `move.to` — same as makeMove().
            val capturedPawn =
                board.getBox(move.from.position.row, move.to.position.column).chessPiece
            //perform enPassant
            sourcePiece.performEnPassant(board, move.from, move.to)
            move.isEnPassant = true
            if (capturedPawn != null) {
                capturedPawn.isKilled = true
                move.pieceKilled = capturedPawn
            }
            //movesPlayed.add(move)

            return true
        }

        // Check if the player is attempting castling
        if (sourcePiece is King && sourcePiece.canCastle(move.from, move.to, board)) {
            // Perform castling move
            sourcePiece.castlingMove(move.from, move.to, board)
            move.isCastlingMove = true
            //movesPlayed.add(move)

            return true
        }

        // Kill?
        val destPiece = move.to.chessPiece
        if (destPiece != null) {
            destPiece.isKilled = true
            move.pieceKilled = destPiece
        }

        // Move piece from the start box to end box
        move.to.chessPiece = move.from.chessPiece
        move.from.chessPiece = null

        Log.d(TAG, "determineMove: move added is $move")
        //movesPlayed.add(move)
        return true
    }

    fun changeCurrentTurn() {
        // Set the current turn to the other player
        currentTurn.value = if (currentTurn.value == players[0]) {
            players[1]
        } else {
            players[0]
        }
        Log.d(TAG, "changeCurrentTurn: current turn is ${currentTurn.value}")
    }

    fun getValidMoves(position: Position, player: Player, board: Board): MutableList<Position> {
        val selectedSpot = board.getBox(position.row, position.column)
        val selectedPiece = selectedSpot.chessPiece
        val playerColor = if (player.isWhite) Color.WHITE else Color.BLACK
        val validMovesForPiece = mutableListOf<Position>()
        if (selectedPiece != null) {
            if (selectedPiece.color == playerColor) {
                for (row in 0 until Board.NUM_ROWS) {
                    for (col in 0 until Board.NUM_COLUMNS) {
                        val endPosition = Position(row, col)
                        val endSpot = board.getBox(endPosition.row, endPosition.column)
                        if (selectedPiece.canMove(
                                board = board,
                                start = selectedSpot,
                                end = endSpot
                            )
                        ) {
                            validMovesForPiece.add(endPosition)
                        }
                    }
                }
            }

        }
        return validMovesForPiece
    }

    fun getRemainingValidMoves(position: Position, player: Player, board: Board): List<Position> {
        val startSpot = board.getBox(position.row, position.column)
        val chessPiece = startSpot.chessPiece
        val endPositions = getValidMoves(position, player, board)
        //Log.d(TAG, "before getRemainingValidMoves: all moves are $movesPlayed")
        val validPositions = mutableListOf<Position>()

        if (chessPiece != null) {
            for (endPosition in endPositions) {
                val endSpot = board.getBox(endPosition.row, endPosition.column)
                Log.d(TAG, "getRemainingValidMoves: endSpot is $endSpot")
                // Simulate the move
                val move = Move(startSpot, endSpot, player, chessPiece)
                simulateMove(move, player)

                // Check for checks
                val isCheck = detectCheck(player, board)

                // If not in check, add to valid positions
                if (!isCheck) {
                    validPositions.add(endPosition)
                }

                undoMove(move, board)
                Log.d(TAG, "getRemainingValidMoves: all moves are $movesPlayed")
            }
        }

        return validPositions
    }


    private fun detectCheck(player: Player, myBoard: Board): Boolean {
        val opponent = if (player.isWhite) players[1] else players[0]
        val playerColor = if (player.isWhite) Color.WHITE else Color.BLACK
        val kingSpot = findKing(myBoard, playerColor)
        val opponentPieces = getAlivePieces(opponent, myBoard)
        return opponentPieces.any {
            if (kingSpot != null) {
                it.canMove(myBoard, myBoard.getBox(it.position.row, it.position.column), kingSpot)
            } else false
        }
    }


    fun detectCheck(player: Player): Boolean {
        val opponent = if (player.isWhite) players[1] else players[0]
        val playerColor = if (player.isWhite) Color.WHITE else Color.BLACK
        val kingSpot = findKing(board, playerColor)
        val opponentPieces = getAlivePieces(opponent, board)
        return opponentPieces.any {
            if (kingSpot != null) {
                it.canMove(board, board.getBox(it.position.row, it.position.column), kingSpot)
            } else false
        }
    }

    private fun detectCheckMate(player: Player): Boolean {
        if (!detectCheck(player)) return false // Not in check, so not in checkmate
        val pieces = getAlivePieces(player, board)
        currentTurn.value = player
        val opponent = if (player.isWhite) players[1] else players[0]
        for (piece in pieces) {
            val positions = getValidMoves(piece.position, player, board)
            for (position in positions) {
                val move = Move(
                    board.getBox(piece.position.row, piece.position.column),
                    board.getBox(position.row, position.column),
                    player, // Use the original player object
                    piece
                )
                simulateMove(move, player)

                // Step 7: Check if the player is still in check after the move
                val isInCheck = detectCheck(player, board)
                Log.d(TAG, "detectCheckMate: is playerInCheck $isInCheck")

                Log.d(TAG, "isStalemate: moves undone here")
                undoMove(move, board)

                // If any move gets the player out of check, return false (not in checkmate)
                if (!isInCheck) {
                    currentTurn.value = opponent
                    return false
                }
            }
        }
        currentTurn.value = opponent
        return true // No legal move removes check, it's checkmate
    }


    fun findKing(myBoard: Board, playerColor: Color): Spot? {
        // Iterate through the board to find the king spot of the specified color
        for (row in 0 until Board.NUM_ROWS) {
            for (col in 0 until Board.NUM_COLUMNS) {
                val spot = myBoard.getBox(row, col)
                val piece = spot.chessPiece
                Log.d(TAG, "findKing: spot is $spot: piece is $piece")
                if (piece != null && piece is King) {
                    if (piece.color == playerColor) {
                        return spot
                    } // Found the king spot
                }
            }
        }
        return null
    }

    fun getAlivePieces(player: Player, myBoard: Board): MutableList<ChessPiece> {
        val availablePieces = mutableListOf<ChessPiece>()
        val playerColor = if (player.isWhite) Color.WHITE else Color.BLACK
        // Iterate through the board to find available pieces
        for (row in 0 until 8) {
            for (col in 0 until 8) {
                if (myBoard.getBox(row, col).chessPiece != null) {
                    if (playerColor == myBoard.getBox(row, col).chessPiece!!.color) {
                        val chessPiece = myBoard.getBox(row, col).chessPiece
                        val chessPiecePosition = Position(row, col)
                        chessPiece!!.position = chessPiecePosition
                        availablePieces += chessPiece
                    }
                }
            }
        }
        return availablePieces
    }

    private fun isStalemate(player: Player, board: Board): Boolean {
        val opponent = if (player.isWhite) players[1] else players[0]
        // Check if the current player has any legal moves
        val pieces = getAlivePieces(player, board)
        currentTurn.value = player
        for (piece in pieces) {
            // Must reset per piece — a destination that's illegal (into check) for one
            // piece says nothing about whether the same square is a legal, different
            // piece's move, so this can't be shared/accumulated across pieces.
            val invalidPositions = mutableListOf<Position>()
            val validMoves = getValidMoves(piece.position, player, board)
            for (position in validMoves) {
                val move = Move(
                    board.getBox(piece.position.row, piece.position.column),
                    board.getBox(position.row, position.column),
                    player, // Use the original player object
                    piece
                )
                simulateMove(move, player)

                // Step 7: Check if the player is still in check after the move
                val isInCheck = detectCheck(player, board)
                if (isInCheck) invalidPositions.add(position)

                Log.d(TAG, "isStalemate: move undone is $move")
                undoMove(move, board, player)
            }
            validMoves.removeAll(invalidPositions)

            if (validMoves.isNotEmpty()) {
                // The player has at least one legal move, so it's not stalemate
                currentTurn.value = opponent
                return false
            }
        }

        // Check if the player's king is not in check
        // The player's king is not in check and they have no legal moves, so it's stalemate
        currentTurn.value = opponent
        return !detectCheck(player, board)
    }


    private fun undoMove(move: Move, myBoard: Board, player: Player) {
        val startSpot = move.from
        val endSpot = move.to

        // If it was a castling move, undo the castling
        if (move.isCastlingMove) {
            // If it was a king-side castling
            if (endSpot.position.column - startSpot.position.column == 2) {
                // Move the rook back to its original position
                val rookStartColumn = endSpot.position.column + 1
                val rookEndColumn = startSpot.position.column + 1
                val rookStartSpot = myBoard.getBox(startSpot.position.row, rookStartColumn)
                val rookEndSpot = myBoard.getBox(startSpot.position.row, rookEndColumn)
                rookStartSpot.chessPiece = rookEndSpot.chessPiece
                rookEndSpot.chessPiece = null
                // A rook can only ever have castled if it hadn't moved before — safe to
                // restore unconditionally, real or simulated undo alike.
                rookStartSpot.chessPiece?.hasMoved = false

                //move the king back to its original position
                val kingStartColumn = startSpot.position.column
                val kingEndColumn = endSpot.position.column
                val kingStartSpot = myBoard.getBox(startSpot.position.row, kingStartColumn)
                val kingEndSpot = myBoard.getBox(endSpot.position.row, kingEndColumn)
                kingStartSpot.chessPiece = kingEndSpot.chessPiece
                kingEndSpot.chessPiece = null
            }
            // If it was a queen-side castling
            if (endSpot.position.column - startSpot.position.column == -2) {
                // Move the rook back to its original position
                val rookStartColumn = endSpot.position.column - 2
                val rookEndColumn = endSpot.position.column + 1
                val rookStartSpot = myBoard.getBox(startSpot.position.row, rookStartColumn)
                val rookEndSpot = myBoard.getBox(startSpot.position.row, rookEndColumn)
                rookStartSpot.chessPiece = rookEndSpot.chessPiece
                rookEndSpot.chessPiece = null
                rookStartSpot.chessPiece?.hasMoved = false
                //move the king back to its original position
                val kingStartColumn = startSpot.position.column
                val kingEndColumn = endSpot.position.column
                val kingStartSpot = myBoard.getBox(startSpot.position.row, kingStartColumn)
                val kingEndSpot = myBoard.getBox(endSpot.position.row, kingEndColumn)
                kingStartSpot.chessPiece = kingEndSpot.chessPiece
                kingEndSpot.chessPiece = null
            }
            //movesPlayed.remove(move)
        } else if (move.isEnPassant) {
            // Calculate the position of the captured pawn based on the end spot
            val capturedPawnPosition = Position(startSpot.position.row, endSpot.position.column)

            // Get the spot of the captured pawn
            val capturedPawnSpot =
                board.getBox(capturedPawnPosition.row, capturedPawnPosition.column)

            // Move the pawn back to its original position
            startSpot.chessPiece = endSpot.chessPiece
            endSpot.chessPiece = null

            // Restore the actual captured pawn (identity preserved — used elsewhere e.g.
            // for animation matching), falling back to fabricating one only in the
            // unlikely case pieceKilled wasn't captured (e.g. a hand-built Move).
            val opponentColor = if (move.player.isWhite) Color.BLACK else Color.WHITE
            capturedPawnSpot.chessPiece = move.pieceKilled?.also { it.isKilled = false }
                ?: Pawn(color = opponentColor, position = capturedPawnPosition, isKilled = false)
            //movesPlayed.remove(move)
        } else {
            // Restore the piece that was moved to its original position
            startSpot.chessPiece = move.pieceMoved
            // Restore any piece that was captured during the move
            endSpot.chessPiece = move.pieceKilled

            //movesPlayed.remove(move)
        }
        // Only restore when this undo pairs with a real makeMove() — never for the
        // speculative simulateMove()/undoMove() cycles used by legality scans (isStalemate
        // also calls this 3-arg overload, but never sets wasCommitted).
        if (move.wasCommitted) {
            myBoard.enPassantTarget = move.previousEnPassantTarget
            move.pieceMoved?.hasMoved = move.previousHasMoved
        }
        currentTurn.value = player
    }

    fun undoMove(move: Move, myBoard: Board) {
        val startSpot = move.from
        val endSpot = move.to

        if (move.isCastlingMove) {
            if (endSpot.position.column - startSpot.position.column == 2) {
                val rookStartColumn = endSpot.position.column + 1
                val rookEndColumn = startSpot.position.column + 1
                val rookStartSpot = myBoard.getBox(startSpot.position.row, rookStartColumn)
                val rookEndSpot = myBoard.getBox(startSpot.position.row, rookEndColumn)
                rookStartSpot.chessPiece = rookEndSpot.chessPiece
                rookEndSpot.chessPiece = null
                // A rook can only ever have castled if it hadn't moved before — safe to
                // restore unconditionally, real or simulated undo alike.
                rookStartSpot.chessPiece?.hasMoved = false

                val kingStartSpot =
                    myBoard.getBox(startSpot.position.row, startSpot.position.column)
                val kingEndSpot = myBoard.getBox(endSpot.position.row, endSpot.position.column)
                kingStartSpot.chessPiece = kingEndSpot.chessPiece
                kingEndSpot.chessPiece = null
            }
            if (endSpot.position.column - startSpot.position.column == -2) {
                val rookStartColumn = endSpot.position.column - 2
                val rookEndColumn = endSpot.position.column + 1
                val rookStartSpot = myBoard.getBox(startSpot.position.row, rookStartColumn)
                val rookEndSpot = myBoard.getBox(startSpot.position.row, rookEndColumn)
                rookStartSpot.chessPiece = rookEndSpot.chessPiece
                rookEndSpot.chessPiece = null
                rookStartSpot.chessPiece?.hasMoved = false

                val kingStartSpot =
                    myBoard.getBox(startSpot.position.row, startSpot.position.column)
                val kingEndSpot = myBoard.getBox(endSpot.position.row, endSpot.position.column)
                kingStartSpot.chessPiece = kingEndSpot.chessPiece
                kingEndSpot.chessPiece = null
            }

        } else if (move.isEnPassant) {
            val capturedPawnPosition = Position(startSpot.position.row, endSpot.position.column)

            // ✅ was `board`, now `myBoard`
            val capturedPawnSpot = myBoard.getBox(
                capturedPawnPosition.row,
                capturedPawnPosition.column
            )

            startSpot.chessPiece = endSpot.chessPiece
            endSpot.chessPiece = null

            val opponentColor = if (move.player.isWhite) Color.BLACK else Color.WHITE
            capturedPawnSpot.chessPiece = move.pieceKilled?.also { it.isKilled = false }
                ?: Pawn(color = opponentColor, position = capturedPawnPosition, isKilled = false)

        } else {
            startSpot.chessPiece = move.pieceMoved
            endSpot.chessPiece = move.pieceKilled

            // ✅ was `board`, now `myBoard`
            myBoard.getBox(startSpot.position.row, startSpot.position.column).chessPiece =
                startSpot.chessPiece
            myBoard.getBox(endSpot.position.row, endSpot.position.column).chessPiece =
                endSpot.chessPiece

            Log.d(TAG, "undoMove: piece removed is: $move")
        }
        if (move.wasCommitted) {
            myBoard.enPassantTarget = move.previousEnPassantTarget
            move.pieceMoved?.hasMoved = move.previousHasMoved
        }
    }

    fun undoMoveByPlayer(move: Move, isReviewMode: Boolean = false) {
        val startSpot = move.from
        val endSpot = move.to

        // If it was a castling move, undo the castling
        if (move.isCastlingMove) {
            Log.d(TAG, "undoMove: castling undoing")
            // If it was a king-side castling
            if (endSpot.position.column - startSpot.position.column == 2) {
                // Move the rook back to its original position
                val rookStartColumn = endSpot.position.column + 1
                val rookEndColumn = startSpot.position.column + 1
                val rookStartSpot = board.getBox(startSpot.position.row, rookStartColumn)
                val rookEndSpot = board.getBox(startSpot.position.row, rookEndColumn)
                Log.d(
                    TAG,
                    "undoMoveByPlayer: rook startSpot/endSpot is $rookStartSpot $rookEndSpot"
                )
                board.getBox(startSpot.position.row, rookStartColumn).chessPiece =
                    rookEndSpot.chessPiece
                rookEndSpot.chessPiece = null
                // A rook can only ever have castled if it hadn't moved before — safe to
                // restore unconditionally, real or simulated undo alike.
                rookStartSpot.chessPiece?.hasMoved = false

                //move the king back to its original position
                val kingStartColumn = startSpot.position.column
                val kingEndColumn = endSpot.position.column
                val kingStartSpot = board.getBox(startSpot.position.row, kingStartColumn)
                val kingEndSpot = board.getBox(endSpot.position.row, kingEndColumn)
                kingStartSpot.chessPiece = kingEndSpot.chessPiece
                kingEndSpot.chessPiece = null
            }
            // If it was a queen-side castling
            if (endSpot.position.column - startSpot.position.column == -2) {
                // Move the rook back to its original position
                val rookStartColumn = endSpot.position.column - 2
                val rookEndColumn = endSpot.position.column + 1
                val rookStartSpot = board.getBox(startSpot.position.row, rookStartColumn)
                val rookEndSpot = board.getBox(startSpot.position.row, rookEndColumn)
                rookStartSpot.chessPiece = rookEndSpot.chessPiece
                rookEndSpot.chessPiece = null
                rookStartSpot.chessPiece?.hasMoved = false
                //move the king back to its original position
                val kingStartColumn = startSpot.position.column
                val kingEndColumn = endSpot.position.column
                val kingStartSpot = board.getBox(startSpot.position.row, kingStartColumn)
                val kingEndSpot = board.getBox(endSpot.position.row, kingEndColumn)
                kingStartSpot.chessPiece = kingEndSpot.chessPiece
                kingEndSpot.chessPiece = null
            }
            if (!isReviewMode) {
                movesUndone.add(move)
                movesPlayed.remove(move)
            }
        } else if (move.isEnPassant) {
            // Calculate the position of the captured pawn based on the end spot
            val capturedPawnPosition = Position(startSpot.position.row, endSpot.position.column)

            // Get the spot of the captured pawn
            val capturedPawnSpot =
                board.getBox(capturedPawnPosition.row, capturedPawnPosition.column)

            // Move the pawn back to its original position
            startSpot.chessPiece = endSpot.chessPiece
            endSpot.chessPiece = null

            val opponentColor = if (move.player.isWhite) Color.BLACK else Color.WHITE

            // Restore the captured pawn to its original position
            capturedPawnSpot.chessPiece = move.pieceKilled?.also { it.isKilled = false }
                ?: Pawn(color = opponentColor, position = capturedPawnPosition, isKilled = false)
            if (!isReviewMode) {
                if (move.pieceKilled != null) piecesKilled.remove(move.pieceKilled)
                movesUndone.add(move)
                movesPlayed.remove(move)
            }
        } else {
            // Restore the piece that was moved to its original position
            startSpot.chessPiece = move.pieceMoved
            // Restore any piece that was captured during the move
            endSpot.chessPiece = move.pieceKilled

            Log.d(TAG, "undoMove: piece Moved From $endSpot to $startSpot")
            if (!isReviewMode) {
                if (move.pieceKilled != null) piecesKilled.remove(move.pieceKilled)
                movesUndone.add(move)
                movesPlayed.remove(move)
            }
        }
        if (move.wasCommitted) {
            board.enPassantTarget = move.previousEnPassantTarget
            move.pieceMoved?.hasMoved = move.previousHasMoved
        }
    }

    fun redoMove(move: Move, myBoard: Board) {
        val piece = move.pieceMoved ?: return

        piece.position = move.from.position
        myBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = piece

        currentTurn.value = move.player
        val success = makeMove(myBoard, move, move.player)

        if (success) {
            changeCurrentTurn()  // ← this is what was missing
        }

        movesUndone.remove(move)
    }
}
