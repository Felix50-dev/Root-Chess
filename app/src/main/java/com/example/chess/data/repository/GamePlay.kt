package com.example.chess.data.repository

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.example.chess.data.model.Board
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.Color
import com.example.chess.data.model.GameStatus
import com.example.chess.data.model.King
import com.example.chess.data.model.Move
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.Position
import com.example.chess.data.model.Spot
import javax.inject.Inject

private const val TAG = "GamePlay"

class GamePlay @Inject constructor(
    var board: Board,
    private val context: Context
) {
    private lateinit var players: Array<Player>
    lateinit var currentTurn: Player
    var status: GameStatus = GameStatus.ACTIVE
    private val movesPlayed: MutableList<Move> = mutableListOf()

    fun init(p1: Player, p2: Player) {
        players = arrayOf(p1, p2)
        board.resetBoard()

        currentTurn = if (p1.isWhite) {
            p1
        } else {
            p2
        }

        movesPlayed.clear()

        status = GameStatus.ACTIVE
    }

    fun isEnd(): Boolean {
        return status != GameStatus.ACTIVE
    }

    fun playerMove(player: Player, start: Spot, end: Spot): Boolean {

        val startBox = board.getBox(start.position.row, start.position.column)
        val endBox = board.getBox(end.position.row, end.position.column)
        val move = Move(startBox, endBox, player, start.chessPiece)

        val playerColor = if (player.isWhite) Color.WHITE else Color.BLACK

        // Make the move
        val moveSuccessful = makeMove(board, move, player)

        if (moveSuccessful) {
            // Check if the player's king is in check after making the move
            val kingPositionAfterMove = findKing(board, playerColor)?.position
            val isInCheckAfterMove = kingPositionAfterMove != null && detectCheck(player)

            if (isInCheckAfterMove) {
                // Revert the move if it leaves the player in check
                undoMove(move, board, player)
                return false
            }

            // Check if the move would put the player in check
            val isInCheckByOpponent = detectCheck(player)

            if (isInCheckByOpponent) {
                // Revert the move if it would put the player in check
                undoMove(move, board, player)
                return false
            }

            // Move is valid and does not leave the player in check
            return true
        }

        // Move was not successful
        return false

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

            movesPlayed.add(move)
            move.isEnPassant = false
            //Check for check and checkmate
            checkForWinner(player)

            // Set the current turn to the other player
            changeCurrentTurn()
            return true
        }

        // Check if the player is attempting castling
        if (sourcePiece is King && sourcePiece.canCastle(move.from, move.to, board)) {
            move.isCastlingMove = true
            // Perform castling move
            sourcePiece.castlingMove(move.from, move.to, board)

            movesPlayed.add(move)
            move.isCastlingMove = false
            //Check for check and checkmate
            checkForWinner(player)

            // Set the current turn to the other player
            changeCurrentTurn()
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

        // Store the move
        movesPlayed.add(move)

        // Move piece from the start box to end box
        move.to.chessPiece = move.from.chessPiece
        move.from.chessPiece = null
        sourcePiece.hasMoved = true

        myBoard.getBox(move.from.position.row, move.from.position.column).chessPiece = null
        myBoard.getBox(move.to.position.row, move.to.position.column).chessPiece = sourcePiece

        //Check for check and checkmate
        checkForWinner(player)

        // Set the current turn to the other player
        changeCurrentTurn()

        return true
    }

    fun checkPawnPromotion(position: Position, promotionPiece: ChessPiece?) {
        // Check if the position is on the last rank for the current player's color
        if ((position.row == 0 && currentTurn.isWhite) || (position.row == 7 && !currentTurn.isWhite)) {
            // Check if there is a pawn at the position
            val spot = board.getBox(position.row, position.column)
            val pawn = spot.chessPiece as? Pawn
            if (pawn != null) {
                // Set the pawn promotion state
                if (promotionPiece != null) spot.chessPiece = promotionPiece
            }
        }
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
                    displayToast(player, "checkmate")
                    GameStatus.WHITE_WIN
                } else {
                    Log.d(TAG, "makeMove: black wins")
                    displayToast(player, "checkmate")
                    GameStatus.BLACK_WIN
                }
            } else {
                // Set game status to check
                Log.d(TAG, "makeMove: player $opponent is in check")
                GameStatus.CHECK
            }
        } else if (isStalemate(opponent, board)) {
            status = GameStatus.STALEMATE
            displayToast(player, "no Winner")
        }

        else {
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

            return true
        }

        // Check if the player is attempting castling
        if (sourcePiece is King && sourcePiece.canCastle(move.from, move.to, board)) {
            // Perform castling move
            sourcePiece.castlingMove(move.from, move.to, board)
            move.isCastlingMove = true

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

    fun getRemainingValidMoves(position: Position, player: Player): MutableList<Position> {
        val startSpot = board.getBox(position.row, position.column)
        val chessPiece = startSpot.chessPiece
        val endPositions = getValidMoves(position, player)
        val validPositions = mutableListOf<Position>()
        if (chessPiece != null) {
            for (endPosition in endPositions) {
                val tempPlayer = Player(player.isWhite)
                val cloneStartSpot = board.getBox(position.row, position.column)
                val cloneChessPiece = cloneStartSpot.chessPiece
                val endSpot = board.getBox(endPosition.row, endPosition.column)
                val move = Move(cloneStartSpot, endSpot, tempPlayer, cloneChessPiece)
                determineMove(move, tempPlayer)
                val isCheck = detectCheck(tempPlayer, board)
                if (!isCheck) validPositions.add(endPosition)
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


    private fun displayToast(player: Player, message: String) {
        val playerColor = if (player.isWhite) "White" else "Black"
        Toast.makeText(context, "$message! $playerColor wins", Toast.LENGTH_SHORT).show()
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
        } else if (move.isEnPassant) {
            // Calculate the position of the captured pawn based on the end spot
            val capturedPawnPosition = Position(startSpot.position.row, endSpot.position.column)

            // Get the spot of the captured pawn
            val capturedPawnSpot = board.getBox(capturedPawnPosition.row, capturedPawnPosition.column)

            // Move the pawn back to its original position
            startSpot.chessPiece = endSpot.chessPiece
            endSpot.chessPiece = null

            val opponentColor = if (move.player.isWhite ) Color.BLACK else Color.WHITE

            // Restore the captured pawn to its original position
            capturedPawnSpot.chessPiece = Pawn(
                color = opponentColor,
                position = capturedPawnPosition,
                isKilled = false,
                canEnPassant = true
            )
        } else {
            // Restore the piece that was moved to its original position
            startSpot.chessPiece = move.pieceMoved
            // Restore any piece that was captured during the move
            endSpot.chessPiece = move.pieceKilled

            Log.d(TAG, "undoMove: piece Moved From $endSpot to $startSpot")

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
        } else if (move.isEnPassant) {
            // Calculate the position of the captured pawn based on the end spot
            val capturedPawnPosition = Position(startSpot.position.row, endSpot.position.column)

            // Get the spot of the captured pawn
            val capturedPawnSpot = board.getBox(capturedPawnPosition.row, capturedPawnPosition.column)

            // Move the pawn back to its original position
            startSpot.chessPiece = endSpot.chessPiece
            endSpot.chessPiece = null

            val opponentColor = if (move.player.isWhite ) Color.BLACK else Color.WHITE

            // Restore the captured pawn to its original position
            capturedPawnSpot.chessPiece = Pawn(
                color = opponentColor,
                position = capturedPawnPosition,
                isKilled = false,
                canEnPassant = true
            )
        } else {
            // Restore the piece that was moved to its original position
            startSpot.chessPiece = move.pieceMoved
            // Restore any piece that was captured during the move
            endSpot.chessPiece = move.pieceKilled

            Log.d(TAG, "undoMove: piece Moved From $endSpot to $startSpot")

        }

    }
}