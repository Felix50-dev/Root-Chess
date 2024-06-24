package com.example.chess.data.repository

import android.util.Log
import androidx.annotation.WorkerThread
import com.example.chess.data.model.AILevel
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Move
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.Spot
import com.example.chess.data.model.distanceTo
import javax.inject.Inject

private const val TAG = "GamePlay"

class GamePlay @Inject constructor(
    var board: Board
) {
    private lateinit var players: Array<Player>
    lateinit var currentTurn: Player
    var status: GameStatus = GameStatus.ACTIVE
    var isGameOver: Boolean = false
    val movesPlayed: MutableList<Move> = mutableListOf()
    val movesUndone: MutableList<Move> = mutableListOf()
    private var moveCount: Int = 0

    fun init(p1: Player, p2: Player, aiLevel: AILevel) {
        isGameOver = false
        players = arrayOf(p1, p2)
        board.resetBoard()

        currentTurn = if (p1.isWhite) {
            p1
        } else {
            p2
        }

        movesPlayed.clear()

        status = GameStatus.ACTIVE

        if (p1.playerType == PlayerType.AI) {
            handleAIMove(p1, aiLevel)
        }
    }

    fun playerMove(player: Player, start: Spot, end: Spot, aiLevel: AILevel): Boolean {
        if (player.playerType == PlayerType.HUMAN) {
            return handleHumanMove(player, start, end, aiLevel)
        } else {
            handleAIMove(currentTurn, aiLevel)
        }
        return false
    }

    private fun handleHumanMove(player: Player, start: Spot, end: Spot, aiLevel: AILevel): Boolean {
        if (isGameOver) return false
        val startBox = board.getBox(start.position.row, start.position.column)
        val endBox = board.getBox(end.position.row, end.position.column)
        val move = Move(startBox, endBox, player, start.chessPiece)

        val playerColor = if (player.isWhite) Color.WHITE else Color.BLACK

        // Make the move
        if (currentTurn.playerType == PlayerType.HUMAN && makeMove(board, move, player)) {
            // Validate the move
            if (isMoveValid(player, playerColor)) {
                checkForWinner(player)
                if (!isGameOver) {
                    changeCurrentTurn()
                    if (currentTurn.playerType == PlayerType.AI) {
                        handleAIMove(currentTurn, aiLevel)
                    }
                    return true
                }
            } else {
                // Revert the move if invalid
                undoMove(move, board, player)
            }
        }
        return false
    }

    private fun handleAIMove(player: Player, aiLevel: AILevel): Boolean {
        val legalMoves = generateAllLegalMoves(player, board)
        val depth = 1
        val bestMove = when (aiLevel) {
            AILevel.LEVEL1 -> {
                findBestMove(board, depth, player)
            }
            AILevel.LEVEL2 -> {
                if (isOpeningPhase(moveCount)) {
                    chooseOpeningMove(player)
                } else {
                    evaluateMovesLevel2(legalMoves, player)
                }
            }
        }

        if (makeMove(board, bestMove, player)) {
            moveCount++
            checkForWinner(player)
            if (isGameOver) return false
            changeCurrentTurn()
            return true
        }
        return false
    }

    private fun chooseOpeningMove(player: Player): Move {
        if (player.isWhite) Color.WHITE else Color.BLACK

        val openingMovesWhite = listOf(
            Pair(Position(6, 4), Position(4, 4)), // e2 to e4
            Pair(Position(6, 3), Position(4, 3)), // d2 to d4
            Pair(Position(7, 6),  Position(5, 5)), // Nf3
            Pair(Position(7, 1), Position(5, 2)), // Nc3
            // Add more opening moves as needed
        )

        val openingMovesBlack = listOf(
            Pair(Position(1, 4), Position(3, 4)), // e2 to e4
            Pair(Position(1, 3), Position(3, 3)), // d2 to d4
            Pair(Position(0, 6), Position(2, 5)), // Nf3
            Pair(Position(0, 1), Position(2, 2)), // Nc3
            // Add more opening moves as needed
        )
        val openingMove = if (player.isWhite) openingMovesWhite.random() else openingMovesBlack.random()
        val startBox = board.getBox(openingMove.first.row, openingMove.first.column)
        val endBox = board.getBox(openingMove.second.row, openingMove.second.column)
        return Move(startBox, endBox, player, startBox.chessPiece)
    }

    private fun isOpeningPhase(moveCount: Int): Boolean {
        return moveCount < 4 // Adjust this threshold based on your preference
    }

    private fun generateAllLegalMoves(player: Player, board: Board): List<Move> {
        val playerPieces = getAlivePieces(player, board)
        val legalMoves = mutableListOf<Move>()
        for (piece in playerPieces) {
            val start = piece.position
            val startSpot = board.getBox(start.row, start.column)
            val endPositions = getRemainingValidMoves(start, player)
            for (endPosition in endPositions) {
                val endSpot = board.getBox(endPosition.row, endPosition.column)
                val move = Move(startSpot, endSpot, player, piece)
                legalMoves.add(move)
            }
        }
        return legalMoves
    }

    private fun evaluateMoves(possibleMoves: List<Move>): Move {
        // Get capture moves
        val captureMoves = possibleMoves.filter { it.to.chessPiece != null }

        // Find the best capture move
        val bestCaptureMove = captureMoves.maxByOrNull { evaluateCaptureMove(it) }

        // Log the best move
        Log.d(TAG, "evaluateMoves: best move is: ${bestCaptureMove ?: possibleMoves.random()}")

        // Return the best capture move if available, otherwise a random move
        return bestCaptureMove ?: possibleMoves.random()
    }

    private fun evaluateCaptureMove(move: Move): Int {
        return evaluatePieceValue(move.to.chessPiece)
    }

    private fun evaluatePieceValue(piece: ChessPiece?): Int {
        return when (piece) {
            is Pawn -> 1
            is Knight -> 3
            is Bishop -> 3
            is Rook -> 5
            is Queen -> 9
            is King -> 100
            else -> 0 //no piece
        }
    }

    private fun evaluateMove(move: Move, player: Player): Int {
        var score = 0
        // Basic capture evaluation
        val capturedPiece = move.to.chessPiece
        if (capturedPiece != null) {
            score += evaluatePieceValue(capturedPiece)
        }
        //check for king safety
        determineMove(move, player)
        val playerColor = if (player.isWhite) Color.WHITE else Color.BLACK
        val kingSpot = findKing(board, playerColor = playerColor )
        kingSpot?.let {
            score += evaluateKingSafety(it, board, player)
        }
        // Positional value (simplified example)
        // Encourage center control (positions roughly in the center of the board)
        if (move.to.position.row in 2..5 && move.to.position.column in 2..5) {
            score += 1
        }

        //check for possible check or checkMate
        val opponent = if (move.player.isWhite) players[1] else players[0]
        if (detectCheck(opponent)) score += 10
        if (detectCheckMate(opponent)) score += 1000

        // Penalize moves that leave the piece unprotected
        if (isPieceInDanger(move.to, board, player)) {
            Log.d(TAG, "evaluateMove: ")
            score -= evaluatePieceValue(move.pieceMoved) * 2
        }

        if (isPieceInDanger(move.from, board, player)) {
            Log.d(TAG, "evaluateMove: ")
            score += evaluatePieceValue(move.pieceMoved) * 2
        }

        Log.d(TAG, "evaluateMove: score is :$score")
        undoMove(move, board)
        return score
    }

    private fun evaluateBoard(board: Board): Int {
        var score = 0
        for (row in 0 until Board.NUM_ROWS) {
            for (col in 0 until Board.NUM_COLUMNS) {
                val spot = board.getBox(row, col )
                spot.chessPiece?.let { piece ->
                    val pieceValue = when (piece) {
                        is Pawn -> 1
                        is Knight, is Bishop -> 3
                        is Rook -> 5
                        is Queen -> 9
                        is King -> 100 // Arbitrarily high value for the king
                    }
                    score = pieceValue * if (piece.color == Color.WHITE) 1 else -1
                }
            }
        }
        Log.d(TAG, "evaluateBoard: board score is $score")
        return score
    }

    private fun evaluateBoard(board: Board, player: Player): Int {
        var score = 0
        val opponent = if (player.isWhite) players[1] else players[0]

        //evaluate material balance
        val playerPieces = getAlivePieces(player, board)
        val opponentPieces = getAlivePieces(opponent, board)
        for (piece in playerPieces) {
            score += piece.value
        }
        for (piece in opponentPieces) {
            score -= piece.value
        }

        //Evaluate piece safety
        for (piece in playerPieces) {
            if (isPieceInDanger(board.getBox(piece.position.row, piece.position.column), board, player)) {
                score -= piece.value
            }
        }
        for (piece in playerPieces) {
            if (isPieceInDanger(board.getBox(piece.position.row, piece.position.column), board, opponent)) {
                score += piece.value
            }
        }
        currentTurn = player

        return score
    }

    @WorkerThread
    private fun minimax(board: Board, depth: Int, isMaximizing: Boolean, myPlayer: Player): Int {
        Log.d(TAG, "minimax: minimax running")
        if (depth == 0) return evaluateBoard(board, myPlayer)

        val player = if (isMaximizing) players[0] else players[1]
        val moves = generateAllLegalMoves(player, board)

        return if (isMaximizing) {
            var maxEval = Int.MIN_VALUE
            for (move in moves) {
                makeMove(board, move, player)
                val eval = minimax(board, depth - 1, false, myPlayer)
                undoMove(move, board)
                maxEval = maxOf(maxEval, eval)
            }
            maxEval
        } else {
            var minEval = Int.MAX_VALUE
            for (move in moves) {
                makeMove(board, move, player)
                val eval = minimax(board, depth - 1, true, myPlayer)
                undoMove(move, board)
                minEval = minOf(minEval, eval)
            }
            minEval
        }
    }

    private fun findBestMove(board: Board, depth: Int, myPlayer: Player): Move {
        var bestMove: Move? = null
        var bestValue = Int.MIN_VALUE

        val player = if (players[1].playerType == PlayerType.AI) players[1] else players[0]
        val moves = generateAllLegalMoves(player, board)

        for (move in moves) {
            makeMove(board, move, player)
            val boardValue = minimax(board, depth - 1, false, myPlayer)
            undoMove(move, board)
            if (boardValue > bestValue) {
                bestValue = boardValue
                bestMove = move
            }
        }

        return bestMove ?: moves.random()
    }

    private fun evaluateMovesLevel2(possibleMoves: List<Move>, player: Player): Move {
        var bestMove: Move? = null
        var bestScore = Int.MIN_VALUE

        for (move in possibleMoves) {
            val score = evaluateMove(move, player)
            if (score > bestScore) {
                bestMove = move
                bestScore = score
            }
        }
        Log.d(TAG, "evaluateMovesLevel2: bestMove is $bestMove")
        return bestMove ?: possibleMoves.random()
    }

    private fun evaluateKingSafety(kingSpot: Spot, board: Board, player: Player): Int {
        var safetyScore = 0
        //check for pawns protecting the king
        val kingPosition = kingSpot.position
        val pawnProtectionPositions = listOf(
            Position(kingPosition.row + 1, kingPosition.column -1),
            Position(kingPosition.row + 1, kingPosition.column),
            Position(kingPosition.row + 1, kingPosition.column + 1),
        )

        for (pos in pawnProtectionPositions) {
            if (board.isValidPosition(pos)) {
                val spot = board.getBox(pos.row, pos.column)
                if (spot.chessPiece is Pawn && (spot.chessPiece as Pawn).color == kingSpot.chessPiece?.color) {
                    safetyScore += 1
                }
            }
        }
        //check for enemy pieces proximity
        val enemyPiecesProximity = getAlivePieces(player, board).map {
            it.position.distanceTo(kingPosition)
        }.count {
            it <= 2
        }
        safetyScore -= enemyPiecesProximity
        Log.d(TAG, "evaluateKingSafety: safety score is: $safetyScore")
        return safetyScore
    }

    private fun isPieceInDanger(spot: Spot, board: Board, player: Player): Boolean {
        // Determine the opponent's color
        val opponentColor = if (player.isWhite) Color.BLACK else Color.WHITE
        val opponent = if (player.isWhite) players[1] else players[0]

        // Iterate through all the spots on the board to find opponent's pieces
        currentTurn = opponent
        for (row in 0 until Board.NUM_ROWS) {
            for (col in 0 until Board.NUM_COLUMNS) {
                val currentSpot = board.getBox(row, col)
                val currentPiece = currentSpot.chessPiece

                // If the current spot contains an opponent's piece
                if (currentPiece != null && currentPiece.color == opponentColor) {
                    // Generate all possible moves for the opponent's piece
                    val possibleMoves = getRemainingValidMoves(currentPiece.position, opponent)
                    Log.d(
                        TAG,
                        "isPieceInDanger: current piece is $currentPiece start spot is ${currentPiece.position}  possibleMoves are: $possibleMoves"
                    )
                    // Check if any of these moves can capture the piece at the given spot
                    for (move in possibleMoves) {
                        if (move == spot.position) {
                            currentTurn = player
                            return true // The piece at the given spot is in danger
                        }
                    }
                }
            }
        }
        currentTurn = player
        return false // The piece at the given spot is not in danger
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

    private fun makeMove(myBoard: Board, move: Move, player: Player): Boolean {
        Log.d(TAG, "makeMove: function running")
        //no piece available to move
        val sourcePiece = move.pieceMoved ?: return false

        // Valid player
        if (player != currentTurn) {
            Log.d(TAG, "makeMove: not players turn")
            return false
        }

        if ((sourcePiece.color == Color.WHITE) != player.isWhite) {
            Log.d(TAG, "makeMove: color problem")
            return false
        }

        if (sourcePiece is Pawn && sourcePiece.isEnPassant(board, move.from, move.to)) {
            Log.d(TAG, "makeMove: isEnPassant")
            move.isEnPassant = true
            //perform enPassant
            sourcePiece.performEnPassant(board, move.from, move.to)
            sourcePiece.hasMoved = true

            movesPlayed.add(move)
            move.isEnPassant = false

            return true
        }

        // Check if the player is attempting castling
        if (sourcePiece is King && sourcePiece.canCastle(move.from, move.to, board)) {
            Log.d(TAG, "makeMove: castling can happen")
            move.isCastlingMove = true
            // Perform castling move
            sourcePiece.castlingMove(move.from, move.to, board)
            sourcePiece.hasMoved = true

            movesPlayed.add(move)
            move.isCastlingMove = false

            return true
        }

        //not a  Valid move
        if (!sourcePiece.canMove(myBoard, move.from, move.to)) {
            Log.d(TAG, "makeMove: invalid move")
            return false
        }
        Log.d(TAG, "makeMove: no special move")

        // Kill?
        val destPiece = move.to.chessPiece
        if (destPiece != null) {
            destPiece.isKilled = true
            move.pieceKilled = destPiece
        }

        // Move piece from the start box to end box
        move.to.chessPiece = move.from.chessPiece
        move.from.chessPiece = null
        sourcePiece.hasMoved = true

        // Store the move
        movesPlayed.add(move)

        return true
    }


    private fun checkForWinner(player: Player) {
        Log.d(TAG, "checkForWinner: check for winner started")
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

    private fun determineMove(move: Move, player: Player): Boolean {
        Log.d(TAG, "determineMove: determine move running")
        //no piece available to move
        val sourcePiece = move.pieceMoved ?: return false

        // Valid player
        if (player != currentTurn) {
            Log.d(TAG, "determineMove: not players turn")
            return false
        }

        if ((sourcePiece.color == Color.WHITE) != player.isWhite) {
            Log.d(TAG, "determineMove: color problem")
            return false
        }

        if (sourcePiece is Pawn && sourcePiece.isEnPassant(board, move.from, move.to)) {
            Log.d(TAG, "determineMove: isEnPassant")
            //perform enPassant
            sourcePiece.performEnPassant(board, move.from, move.to)
            move.isEnPassant = true
            movesPlayed.add(move)

            return true
        }

        // Check if the player is attempting castling
        if (sourcePiece is King && sourcePiece.canCastle(move.from, move.to, board)) {
            // Perform castling move
            sourcePiece.castlingMove(move.from, move.to, board)
            move.isCastlingMove = true
            movesPlayed.add(move)

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

        movesPlayed.add(move)
        Log.d(TAG, "determineMove: piece moved from ${move.from} to ${move.to}")
        return true
    }

    private fun changeCurrentTurn() {
        // Set the current turn to the other player
        currentTurn = if (currentTurn == players[0]) {
            players[1]
        } else {
            players[0]
        }
        Log.d(TAG, "changeCurrentTurn: current turn is $currentTurn")
    }

    private fun getValidMoves(position: Position, player: Player): MutableList<Position> {
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

    fun getRemainingValidMoves(position: Position, player: Player): List<Position> {
        val startSpot = board.getBox(position.row, position.column)
        val chessPiece = startSpot.chessPiece
        val endPositions = getValidMoves(position, player)
        val validPositions = mutableListOf<Position>()

        if (chessPiece != null) {
            for (endPosition in endPositions) {
                val endSpot = board.getBox(endPosition.row, endPosition.column)

                // Simulate the move
                val move = Move(startSpot, endSpot, player, chessPiece)
                Log.d(TAG, "getRemainingValidMoves: move is from $startSpot to $endSpot")
                determineMove(move, player)

                // Check for checks
                val isCheck = detectCheck(player, board)

                // If not in check, add to valid positions
                if (!isCheck) {
                    validPositions.add(endPosition)
                }

                // Undo the move
                undoMove(move, board)
            }
        }

        return validPositions
    }


    private fun detectCheck(player: Player, myBoard: Board): Boolean {
        val opponent = if (player.isWhite) players[1] else players[0]
        val playerColor = if (player.isWhite) Color.WHITE else Color.BLACK
        val kingSpot = findKing(myBoard, playerColor)
        Log.d(TAG, "detectCheck: king is at position: $kingSpot")
        val opponentPieces = getAlivePieces(opponent, myBoard)
        return opponentPieces.any {
            if (kingSpot != null) {
                it.canMove(myBoard, myBoard.getBox(it.position.row, it.position.column), kingSpot)
            } else false
        }
    }


    private fun detectCheck(player: Player): Boolean {
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
        currentTurn = player
        val opponent = if (player.isWhite) players[1] else players[0]
        for (piece in pieces) {
            val positions = getValidMoves(piece.position, player)
            Log.d(TAG, "detectCheckMate: positions are $positions")
            Log.d(TAG, "detectCheckMate: piece is at position: ${piece.position}")
            for (position in positions) {
                val move = Move(
                    board.getBox(piece.position.row, piece.position.column),
                    board.getBox(position.row, position.column),
                    player, // Use the original player object
                    piece
                )
                determineMove(move, player)

                // Step 7: Check if the player is still in check after the move
                val isInCheck = detectCheck(player, board)
                Log.d(TAG, "detectCheckMate: is playerInCheck $isInCheck")

                undoMove(move, board)

                // If any move gets the player out of check, return false (not in checkmate)
                if (!isInCheck) {
                    currentTurn = opponent
                    return false
                }
            }
        }
        currentTurn = opponent
        return true // No legal move removes check, it's checkmate
    }


    private fun findKing(myBoard: Board, playerColor: Color): Spot? {
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

    private fun getAlivePieces(player: Player, myBoard: Board): MutableList<ChessPiece> {
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
        Log.d(TAG, "isStalemate: opponent is: $opponent")
        // Check if the current player has any legal moves
        val pieces = getAlivePieces(player, board)
        Log.d(TAG, "isStalemate: pieces are $pieces")
        val invalidPositions = mutableListOf<Position>()
        currentTurn = player
        for (piece in pieces) {
            val validMoves = getValidMoves(piece.position, player)
            Log.d(TAG, "isStalemate: validMoves are $validMoves")
            for (position in validMoves) {
                val move = Move(
                    board.getBox(piece.position.row, piece.position.column),
                    board.getBox(position.row, position.column),
                    player, // Use the original player object
                    piece
                )
                determineMove(move, player)

                // Step 7: Check if the player is still in check after the move
                val isInCheck = detectCheck(player, board)
                Log.d(TAG, "isStalemate: isInCheck $isInCheck")
                if (isInCheck) invalidPositions.add(position)

                undoMove(move, board, player)
            }
            validMoves.removeAll(invalidPositions)

            if (validMoves.isNotEmpty()) {
                // The player has at least one legal move, so it's not stalemate
                currentTurn = opponent
                return false
            }
        }

        // Check if the player's king is not in check
        // The player's king is not in check and they have no legal moves, so it's stalemate
        currentTurn = opponent
        return !detectCheck(player, board)
    }


    private fun undoMove(move: Move, myBoard: Board, player: Player) {
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
            movesPlayed.remove(move)
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
            movesPlayed.remove(move)
        } else {
            // Restore the piece that was moved to its original position
            startSpot.chessPiece = move.pieceMoved
            // Restore any piece that was captured during the move
            endSpot.chessPiece = move.pieceKilled

            Log.d(TAG, "undoMove: piece Moved From $endSpot to $startSpot")
            movesPlayed.remove(move)
        }
        currentTurn = player
    }

    private fun undoMove(move: Move, myBoard: Board) {
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
            movesPlayed.remove(move)
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
            movesPlayed.remove(move)
        } else {
            // Restore the piece that was moved to its original position
            startSpot.chessPiece = move.pieceMoved
            // Restore any piece that was captured during the move
            endSpot.chessPiece = move.pieceKilled

            Log.d(TAG, "undoMove: piece Moved From $endSpot to $startSpot")
            movesPlayed.remove(move)
        }
    }

    fun undoMoveByPlayer(move: Move, myBoard: Board) {
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
            movesUndone.add(move)
            movesPlayed.remove(move)
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
            movesUndone.add(move)
            movesPlayed.remove(move)
        } else {
            // Restore the piece that was moved to its original position
            startSpot.chessPiece = move.pieceMoved
            // Restore any piece that was captured during the move
            endSpot.chessPiece = move.pieceKilled

            Log.d(TAG, "undoMove: piece Moved From $endSpot to $startSpot")
            movesUndone.add(move)
            movesPlayed.remove(move)
        }
    }

    fun redoMove(move: Move, myBoard: Board) {
        makeMove(myBoard, move, move.player)
        movesUndone.remove(move)
    }
}