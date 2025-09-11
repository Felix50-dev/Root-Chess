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
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.SANMovePair
import com.example.chess.data.model.Spot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject

private const val TAG = "GamePlay"

class GamePlay @Inject constructor(
    var board: Board,
    val stockfish: StockfishEngine
) {
    private lateinit var players: List<Player>
    val currentTurn = MutableStateFlow<Player?>(null)
    var status: GameStatus = GameStatus.ACTIVE
    var isGameOver: Boolean = false
    val movesPlayed: MutableList<Move> = mutableListOf()
    val movesUndone: MutableList<Move> = mutableListOf()
    val piecesKilled: MutableList<ChessPiece> = mutableListOf()
    lateinit var aiStartPosition: Position

    // StateFlow for the promotion piece
    val _promotionPieceFlow = MutableStateFlow<ChessPiece?>(null)
    private val promotionPieceFlow: StateFlow<ChessPiece?> = _promotionPieceFlow.asStateFlow()

    //AI last move
    var aiLastMove: Move? = null

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

    fun handleHumanMove(player: Player, start: Spot, end: Spot, isReviewMode: Boolean, board: Board): Boolean {
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
        val playerColor = if (player.isWhite) Color.WHITE else Color.BLACK

        // Make the move
        if (currentTurn.value?.playerType == PlayerType.HUMAN && makeMove(board, move, player)) {
            Log.d(TAG, "handleHumanMove: passed this point")
            Log.d(TAG, "handleHumanMove: move made is $move board state is ${board.boxes}")
            // Validate the move
            if (isMoveValid(player, playerColor)) {
                checkForWinner(player)
                if (isReviewMode) {
                    changeCurrentTurn()
                    return true
                } else {
                    if (!isGameOver) {
                        changeCurrentTurn()
                        return true
                    }
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

            Log.d(TAG, "getCaptureOpportunity: capture opportunity at: ${captureOpportunity.target}")
            captureOpportunity
        } else {
            null
        }
    }

    private fun ChessPiece.clone(): ChessPiece = when (this) {
        is Pawn -> Pawn(color, Position(position.row, position.column), isKilled, vectorAsset, hasMoved, canEnPassant, value)
        is Queen -> Queen(color, Position(position.row, position.column), isKilled, vectorAsset, hasMoved, canEnPassant, value)
        is Rook -> Rook(color, Position(position.row, position.column), isKilled, vectorAsset, hasMoved, canEnPassant, value)
        is Bishop -> Bishop(color, Position(position.row, position.column), isKilled, vectorAsset, hasMoved, canEnPassant, value)
        is Knight -> Knight(color, Position(position.row, position.column), isKilled, vectorAsset, hasMoved, canEnPassant, value)
        is King -> King(color, Position(position.row, position.column), isKilled, vectorAsset, hasMoved, canEnPassant, value)
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
        val pieceKilled = toBox.chessPiece
        Log.d(TAG, "convertToMoveObject: piece is $pieceMoved")
        Log.d(TAG, "convertToMoveObject: piece is $pieceKilled")
        var promotionPiece: ChessPiece? = null

        if (moveStr.length == 5 && pieceMoved is Pawn) {
            val promotionChar = moveStr[4]
            promotionPiece = when (promotionChar.lowercaseChar()) {
                'q' -> Queen(pieceMoved.color, toBox.position, false)
                'r' -> Rook(pieceMoved.color, toBox.position, false)
                'b' -> Bishop(pieceMoved.color, toBox.position, false)
                'n' -> Knight(pieceMoved.color, toBox.position, false)
                else -> null
            }
        }

        _promotionPieceFlow.value = promotionPiece

        return Move(
            from = fromBox,
            to = toBox,
            player = player,
            pieceMoved = pieceMoved,
            pieceKilled = pieceKilled
        )
    }

    private fun applyMove(move: Move) {
        val fromBox = move.from
        val toBox = move.to

        // Clear origin
        fromBox.chessPiece = null

        // Place piece on destination (or promoted piece)
        val pieceToPlace = move.pieceMoved
        toBox.chessPiece = pieceToPlace

        // Update piece position
        pieceToPlace?.position = toBox.position

        Log.d(TAG, "applyMove: applied move $move")
    }



    suspend fun analyzeMoveScores(movesPlayed: List<String>): AnalysisResult {
        val evaluations = mutableListOf<Int>()
        val losses = mutableListOf<Int>()
        val moveCategories = mutableListOf<String>()
        val bestMoves = mutableListOf<String>()
        val firstBoard = Board()
        val captureOpportunities = mutableListOf<CaptureOpportunity?>()
        val missedMates = mutableListOf<MissedMate?>()
        val tactics = mutableListOf<List<String>>()

        val categoryCounts = mutableMapOf(
            "Best" to intArrayOf(0, 0),
            "Great" to intArrayOf(0, 0),
            "Inaccurate" to intArrayOf(0, 0),
            "Mistake" to intArrayOf(0, 0),
            "Blunder" to intArrayOf(0, 0),
            "Brilliant" to intArrayOf(0, 0),
            "Miss" to intArrayOf(0, 0),
            "MissedMate" to intArrayOf(0,0)
        )


        for (i in movesPlayed.indices) {
            val partialMovesBefore = if (i > 0) movesPlayed.subList(0, i).joinToString(" ") else ""
            stockfish.sendCommand("position startpos moves $partialMovesBefore")
            stockfish.sendCommand("go depth 10")

            val (scoreBestForPlayer,  recommendedMove, mateInBefore) = stockfish.getMoveScore()
            val currentPlayer = if (i % 2 == 0) players[0] else players[1]
            val recommendedMoveObject = convertToMoveObject(recommendedMove, firstBoard, currentPlayer)
            Log.d(TAG, "analyzeMoveScores: recommended move is $recommendedMoveObject")

            val scoreBestForWhite = if (i % 2 == 0) scoreBestForPlayer else -scoreBestForPlayer

            val partialMovesAfter = movesPlayed.subList(0, i + 1).joinToString(" ")
            val partialMove = movesPlayed[i]
            Log.d(TAG, "analyzeMoveScores: move made is $partialMove")
            stockfish.sendCommand("position startpos moves $partialMovesAfter")
            stockfish.sendCommand("go depth 10")

            val (scoreAfterFromOpponent, _, mateInAfter ) = stockfish.getMoveScore()
            val playerMove = convertToMoveObject(partialMove, firstBoard, currentPlayer)
            Log.d(
                TAG,
                "analyzeMoveScores: cp score is $scoreBestForPlayer move is $playerMove string is $partialMove"
            )
            //Log.d(TAG, "analyzeMoveScores: cp score is $scoreAfterFromOpponent")

            bestMoves.add(recommendedMove)

            val scoreAfterFromWhite =
                if (i % 2 == 0) -scoreAfterFromOpponent else scoreAfterFromOpponent

            val loss = if (i % 2 == 0) {
                scoreBestForWhite - scoreAfterFromWhite
            } else {
                scoreAfterFromWhite - scoreBestForWhite
            }

            losses.add(loss)

            val delta = if (i % 2 == 0) {
                scoreAfterFromWhite - scoreBestForWhite
            } else {
                // Black: improvement if S_after decreases
                scoreBestForWhite - scoreAfterFromWhite
            }

            val recommendedMoveGain = if (i % 2 == 0) {
                scoreBestForWhite - scoreAfterFromWhite
            } else {
                scoreAfterFromWhite - scoreBestForWhite
            }

            if (i % 2 == 0) {
                Log.d(
                    TAG,
                    "analyzeMoveScores: recommendedMove for white is $recommendedMove loss for white is $loss delta is $delta "
                )
            } else {
                Log.d(
                    TAG,
                    "analyzeMoveScores: recommendedMove for black is $recommendedMove loss for black is $loss delta is $delta"
                )
            }

            // Categorize the move based on loss
            var category = when {
                loss <= 10 -> "Best"
                loss <= 50 -> "Great"
                loss <= 100 -> "Inaccurate"
                loss <= 300 -> "Mistake"
                else -> "Blunder"
            }

            //check if player missed a free piece
            val initialPosition = recommendedMoveObject.from.position
            val targetPosition = recommendedMoveObject.to.position
            val capturedPiece = firstBoard.getBox(targetPosition.row, targetPosition.column).chessPiece
            val captureOpportunity = getCaptureOpportunity(firstBoard, initialPosition,targetPosition, currentPlayer)
            captureOpportunities.add(captureOpportunity)
            Log.d(TAG, "analyzeMoveScores: target square is $targetPosition captured piece is $capturedPiece")

            val playerColor = if (i % 2 == 0) Color.WHITE else Color.BLACK

            if (capturedPiece != null && capturedPiece.color != playerColor) {
                Log.d(TAG, "analyzeMoveScores: capture opportunity is: $captureOpportunity")
                val canCapture = captureOpportunity?.isProfitable
                Log.d(TAG, "analyzeMoveScores: is piece at $targetPosition canCapture: $canCapture")

                if (canCapture == true && recommendedMove != partialMove) {
                    Log.d(TAG, "analyzeMoveScores: passed this point: canCapture $canCapture $recommendedMove $partialMove")
                    category = "Miss"
                }
            }

            val missedMate = mateInBefore in 1..3 && mateInAfter == 0 && partialMove != recommendedMove
            Log.d(TAG, "analyzeMoveScores: missed mate is: $missedMate")

            val tacticalMotifs = checkTactic(playerMove, firstBoard, currentPlayer)
            Log.d(TAG, "analyzeMoveScores: tactics are: $tacticalMotifs")
            tactics.add(tacticalMotifs)

            if (tacticalMotifs.isNotEmpty()) {
                category = "Brilliant"
            }

            if (missedMate) {
                category = "MissedMate" // or "Missed Mate" if you want a separate label
                missedMates.add(
                    MissedMate(
                        move = partialMove,
                        mateInBefore = mateInBefore,
                        player = currentPlayer,
                        moveObject = recommendedMoveObject
                    )
                )
            } else {
                missedMates.add(null)
            }


            val colorIndex = if (i % 2 == 0) 0 else 1
            categoryCounts[category]?.let { it[colorIndex]++ }
            moveCategories.add(category)

            // Log the analysis result
            Log.d("ChessAnalysis", "Move ${movesPlayed[i]}: $category (Loss: $loss, Delta: $delta)")

            // Store the evaluation from White's perspective
            evaluations.add(scoreAfterFromWhite)
            applyMove(playerMove)
        }

        val scalingFactor = 100.0
        val whiteLosses = losses.filterIndexed { index, _ -> index % 2 == 0 }
        val blackLosses = losses.filterIndexed { index, _ -> index % 2 == 1 }

        val whiteAccuracy = if (whiteLosses.isNotEmpty()) {
            whiteLosses.map { 1.0 / (1.0 + it.toDouble() / scalingFactor) }.average() * 100
        } else {
            0.0
        }
        val blackAccuracy = if (blackLosses.isNotEmpty()) {
            blackLosses.map { 1.0 / (1.0 + it.toDouble() / scalingFactor) }.average() * 100
        } else {
            0.0
        }

        val formattedWhiteAccuracy = String.format(Locale.US, "%.1f", whiteAccuracy).toDouble()
        val formattedBlackAccuracy = String.format(Locale.US, "%.1f", blackAccuracy).toDouble()

        val whiteCategoryCounts = categoryCounts.mapValues { it.value[0] }
        val blackCategoryCounts = categoryCounts.mapValues { it.value[1] }

        Log.d(
            TAG,
            "analyzeMoveScores: black accuracy is $blackAccuracy, white accuracy is $whiteAccuracy bestMoves are $bestMoves"
        )
        Log.d(TAG, "analyzeMoveScores: capture opportunities are $captureOpportunities")

        return AnalysisResult(
            evaluations,
            formattedWhiteAccuracy,
            formattedBlackAccuracy,
            whiteCategoryCounts,
            blackCategoryCounts,
            moveCategories,
            bestMoves,
            captureOpportunities,
            missedMates,
            tactics
        )
    }

    private fun checkTactic(move: Move, board: Board, player: Player): List<String> {
        val recognizer = TacticsRecognizer(this)
        val fork = recognizer.isFork(board, move, player)
        val pin = recognizer.isPin(board, move, player)
        val discovered = recognizer.isDiscoveredCheck(board, move, player)

        return listOfNotNull(fork, pin, discovered)
    }

    suspend fun handleAIMove(player: Player, level: AILevel): Boolean =
        withContext(Dispatchers.IO) {
            try {
                // Create a snapshot of movesPlayed to avoid concurrent modification
                val movesSnapshot = synchronized(movesPlayed) { movesPlayed.toMutableList() }
                val movesString = convertMovesToNotation(movesSnapshot).joinToString(" ")
                Log.d(TAG, "handleAIMove: movesByStockFish are: $movesString")
                Log.d(
                    TAG,
                    "handleAIMove: movesPlayed snapshot: ${movesSnapshot.joinToString()}"
                ) // Avoid direct toString() on mutable collection

                // Send the position and start Stockfish thinking
                stockfish.sendCommand("position startpos moves $movesString")
                val skillLevel = if (level == AILevel.LEVEL1) 0 else 4
                stockfish.setDifficultyLevel(skillLevel)
                stockfish.sendCommand("go depth 1")

                val bestMove = stockfish.getBestMove()
                Log.d(TAG, "handleAIMove: moveByStockfish is $bestMove")

                run {
                    // Handle Stockfish move and update game state
                    val move = handleStockfishBestMove(bestMove, player)
                    if (move != null) {
                        aiLastMove = move
                        Log.d(TAG, "handleAIMove: aiLastMove: $aiLastMove")
                        // Synchronize board modifications
                        val moveSuccessful = synchronized(board) {
                            makeMove(
                                board,
                                move,
                                player
                            ) // Use a cloned board to avoid mid-operation changes
                        }
                        if (moveSuccessful) {
                            aiStartPosition = move.from.position
                            Log.d(TAG, "handleAIMove: aiStartPosition set to $aiStartPosition")
                            Log.d(TAG, "handleAIMove: move successful")

                            // Check for winner and update turn safely
                            synchronized(this@GamePlay) {
                                checkForWinner(player)
                                if (!isGameOver) {
                                    changeCurrentTurn()
                                    return@withContext true
                                }
                                return@withContext false
                            }
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

    fun convertMovesToNotation(allMoves: MutableList<Move>): MutableList<String> {
        val moves = mutableListOf<String>()
        allMoves.forEach {
            val startPosition = it.from.position.toAlgebraicNotation()
            val endPosition = it.to.position.toAlgebraicNotation()
            val move = startPosition + endPosition
            moves.add(move)
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
        if (bestMove.length == 4 || bestMove.length == 5) {
            val fromPosition = Position.fromAlgebraicNotation(bestMove.substring(0, 2))
            val toPosition = Position.fromAlgebraicNotation(bestMove.substring(2, 4))

            val fromSpot = board.getBox(fromPosition.row, fromPosition.column)
            val toSpot = board.getBox(toPosition.row, toPosition.column)

            val pieceMoved = fromSpot.chessPiece
            val color = pieceMoved?.color

            if (bestMove.length == 5 && pieceMoved is Pawn) {
                Log.d(TAG, "handleStockfishBestMove: this is pawn promotion")
                _promotionPieceFlow.value = when (bestMove[4]) {
                    'q' -> Queen(
                        pieceMoved.color,
                        toSpot.position,
                        false,
                        vectorAsset = if (color == Color.WHITE) R.drawable.queen_white else R.drawable.queen_dark
                    )

                    'r' -> Rook(
                        pieceMoved.color,
                        toSpot.position,
                        false,
                        vectorAsset = if (color == Color.WHITE) R.drawable.rook_white else R.drawable.rook_dark
                    )

                    'b' -> Bishop(
                        pieceMoved.color,
                        toSpot.position,
                        false,
                        vectorAsset = if (color == Color.WHITE) R.drawable.bishop_white else R.drawable.bishop_dark
                    )

                    'n' -> Knight(
                        pieceMoved.color,
                        toSpot.position,
                        false,
                        vectorAsset = if (color == Color.WHITE) R.drawable.knight_white else R.drawable.knight_dark
                    )

                    else -> null
                }
            }
            // Create a Move object
            return Move(
                from = fromSpot,
                to = toSpot,
                player = player,
                pieceMoved = pieceMoved
            )
        }
        return null
    }


    private fun isMoveValid(player: Player, playerColor: Color): Boolean {
        // Check if the player's king is in check after making the move
        val kingPositionAfterMove = findKing(board, playerColor)?.position
        val isInCheckAfterMove = kingPositionAfterMove != null && detectCheck(player)

        // If the move leaves the player in check, revert it
        if (isInCheckAfterMove) {
            return false
        }

        // Check if the move would put the player in check by the opponent
        val isInCheckByOpponent = detectCheck(player)
        return !isInCheckByOpponent
    }

    fun makeMove(myBoard: Board, move: Move, player: Player): Boolean {
        Log.d(TAG, "makeMove: board state is ${myBoard.boxes}")
        Log.d(TAG, "makeMove: move is $move current player is $player")
        //no piece available to move
        val sourcePiece = move.pieceMoved ?: return false
        // Valid player
        if (player != currentTurn.value) {
            Log.d(TAG, "makeMove: not players turn")
            return false
        }

        if ((sourcePiece.color == Color.WHITE) != player.isWhite) {
            Log.d(TAG, "makeMove: color problem")
            return false
        }

        if (sourcePiece is Pawn && sourcePiece.isEnPassant(myBoard, move.from, move.to)) {
            Log.d(TAG, "makeMove: isEnPassant")
            move.isEnPassant = true
            //perform enPassant
            sourcePiece.performEnPassant(myBoard, move.from, move.to)
            sourcePiece.hasMoved = true
            val destPiece = move.to.chessPiece

            if (destPiece != null) {
                piecesKilled.add(destPiece)
            }

            movesPlayed.add(move)
            move.isEnPassant = false

            return true
        }

        // Check if the player is attempting castling
        if (sourcePiece is King && sourcePiece.canCastle(move.from, move.to, myBoard)) {
            Log.d(TAG, "makeMove: castling can happen")
            move.isCastlingMove = true
            // Perform castling move
            sourcePiece.castlingMove(move.from, move.to, myBoard)
            sourcePiece.hasMoved = true

            movesPlayed.add(move)

            return true
        }

        //promotion for AI
        if (promotionPieceFlow.value != null) {
            Log.d(TAG, "makeMove: promoting AI")
            move.from.chessPiece = null
            move.to.chessPiece = _promotionPieceFlow.value
            sourcePiece.hasMoved = true
            movesPlayed.add(move)
            _promotionPieceFlow.value = null
            return true
        }

        //not a  Valid move
        if (!sourcePiece.canMove(myBoard, move.from, move.to)) {
            Log.d(TAG, "makeMove: myBoard is ${myBoard.boxes}")
            Log.d(TAG, "makeMove: invalid move from ${move.from} to ${move.to}")
            return false
        }

        Log.d(TAG, "makeMove: no special move")

        // Kill?
        val destPiece = move.to.chessPiece
        if (destPiece != null) {
            destPiece.isKilled = true
            move.pieceKilled = destPiece
            piecesKilled.add(destPiece)
        }

        // Move piece from the start box to end box
        move.to.chessPiece = move.from.chessPiece
        move.from.chessPiece = null
        sourcePiece.hasMoved = true


        // Store the move
        movesPlayed.add(move)
        Log.d(TAG, "makeMove: move added is: $move")
        Log.d(TAG, "makeMove: all moves are: $movesPlayed")

        return true
    }


    private fun checkForWinner(player: Player) {
        val opponent = if (player.isWhite) players[1] else players[0]

        //Check for check and checkmate
        if (player.isWhite) {
            findKing(board, Color.WHITE)
        } else {
            findKing(board, Color.BLACK)
        }

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
            //perform enPassant
            sourcePiece.performEnPassant(board, move.from, move.to)
            move.isEnPassant = true
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
        val invalidPositions = mutableListOf<Position>()
        currentTurn.value = player
        for (piece in pieces) {
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
                //move the king back to its original position
                val kingStartColumn = startSpot.position.column
                val kingEndColumn = endSpot.position.column
                val kingStartSpot = myBoard.getBox(startSpot.position.row, kingStartColumn)
                val kingEndSpot = myBoard.getBox(endSpot.position.row, kingEndColumn)
                kingStartSpot.chessPiece = kingEndSpot.chessPiece
                kingEndSpot.chessPiece = null
            }
            move.isCastlingMove = false
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

            val opponentColor = if (move.player.isWhite) Color.BLACK else Color.WHITE

            // Restore the captured pawn to its original position
            capturedPawnSpot.chessPiece = Pawn(
                color = opponentColor,
                position = capturedPawnPosition,
                isKilled = false,
                canEnPassant = true
            )
            //movesPlayed.remove(move)
        } else {
            // Restore the piece that was moved to its original position
            startSpot.chessPiece = move.pieceMoved
            // Restore any piece that was captured during the move
            endSpot.chessPiece = move.pieceKilled

            //movesPlayed.remove(move)
        }
        currentTurn.value = player
    }

    fun undoMove(move: Move, myBoard: Board) {
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
                //move the king back to its original position
                val kingStartColumn = startSpot.position.column
                val kingEndColumn = endSpot.position.column
                val kingStartSpot = myBoard.getBox(startSpot.position.row, kingStartColumn)
                val kingEndSpot = myBoard.getBox(endSpot.position.row, kingEndColumn)
                kingStartSpot.chessPiece = kingEndSpot.chessPiece
                kingEndSpot.chessPiece = null
            }
            move.isCastlingMove = false
            //movesPlayed.remove(move)
            Log.d(TAG, "undoMove: piece removed is: $move")
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
            capturedPawnSpot.chessPiece = Pawn(
                color = opponentColor,
                position = capturedPawnPosition,
                isKilled = false,
                canEnPassant = true
            )
            //movesPlayed.remove(move)
        } else {
            // Restore the piece that was moved to its original position
            startSpot.chessPiece = move.pieceMoved
            // Restore any piece that was captured during the move
            endSpot.chessPiece = move.pieceKilled
            board.getBox(startSpot.position.row, startSpot.position.column).chessPiece =
                startSpot.chessPiece
            board.getBox(endSpot.position.row, endSpot.position.column).chessPiece =
                endSpot.chessPiece
            Log.d(TAG, "undoMove: piece removed is: $move")
            //movesPlayed.remove(move)
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
                //move the king back to its original position
                val kingStartColumn = startSpot.position.column
                val kingEndColumn = endSpot.position.column
                val kingStartSpot = board.getBox(startSpot.position.row, kingStartColumn)
                val kingEndSpot = board.getBox(endSpot.position.row, kingEndColumn)
                kingStartSpot.chessPiece = kingEndSpot.chessPiece
                kingEndSpot.chessPiece = null
            }
            move.isCastlingMove = false
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
            capturedPawnSpot.chessPiece = Pawn(
                color = opponentColor,
                position = capturedPawnPosition,
                isKilled = false,
                canEnPassant = true
            )
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
    }

    fun redoMove(move: Move, myBoard: Board) {
        makeMove(myBoard, move, move.player)
        movesUndone.remove(move)
    }
}