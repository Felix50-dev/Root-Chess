package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Move
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.repository.TacticsRecognizer
import junit.framework.TestCase.assertEquals
import org.junit.Test

class TacticsRecognizerTest {

    @Test
    fun isForkReturnsForkWhenAttackingTwoValuablePieces() {
        val board = Board()
        val stockfish = StockfishEngine()
        val game = GamePlay(board, stockfish)
        val recognizer = TacticsRecognizer(game)
        val isFork = recognizer::isFork

        val queen = Queen(Color.BLACK, Position(5, 6), isKilled = false)
        board.getBox(5,6).chessPiece = queen

        val rook = Rook(Color.BLACK, Position(6, 5), isKilled = false)
        board.getBox(6,5).chessPiece = rook

        val knight = Knight(Color.WHITE, Position(3, 2), isKilled = false)
        board.getBox(3,2).chessPiece = knight


        val player = Player(true, PlayerType.HUMAN)
        game.currentTurn.value = player

        val from = Position(3, 2)
        val to = Position(4, 4)
        val move = Move(board.getBox(from.row, from.column),board.getBox(to.row, to.column) , player,knight)

        val result = isFork(board, move, player)
        assertEquals("Fork", result)
    }

    @Test
    fun isPinReturnsPinWhenSlidingPiecePinsOpponentPieceToKing() {
        val board = Board()
        val stockfish = StockfishEngine()
        val game = GamePlay(board, stockfish)
        val recognizer = TacticsRecognizer(game)
        val isPin = recognizer::isPin

        board.clear()

        val bishop = Bishop(Color.WHITE, Position(0, 6), isKilled = false)
        board.getBox(0,6).chessPiece = bishop

        val king = King(Color.BLACK, Position(5, 1), isKilled = false)
        board.getBox(5,1).chessPiece = king

        val knight = Knight(Color.BLACK, Position(3, 3), isKilled = false)
        board.getBox(3,3).chessPiece = knight

        game.currentTurn.value = Player(true, PlayerType.HUMAN)

        val from = Position(0, 6)
        val to = Position(2, 4)

        val move = Move(
            from = board.getBox(from.row, from.column),
            to = board.getBox(to.row, to.column),
            player = game.currentTurn.value!!,
            pieceMoved = bishop
        )

        val result = isPin(board, move, game.currentTurn.value!!)
        assertEquals("Pin", result)
    }

    @Test
    fun isDiscoveredAttackReturnsDiscoveredAttackWhenUncoveredPieceThreatensKing() {
        val board = Board()
        val stockfish = StockfishEngine()
        val game = GamePlay(board, stockfish)
        val player1 = Player(isWhite = true, PlayerType.HUMAN)
        val player2 = Player(isWhite = false, PlayerType.HUMAN)
        game.init(player1, player2, false)
        val recognizer = TacticsRecognizer(game)
        val isDiscoveredAttack = recognizer::isDiscoveredCheck

        board.clear()

        val rook = Rook(Color.WHITE, Position(0, 3), false)
        board.getBox(0, 3).chessPiece = rook

        val knight = Knight(Color.WHITE, Position(3, 3), false)
        board.getBox(3, 3).chessPiece = knight

        val blackKing = King(Color.BLACK, Position(7, 3), false)
        board.getBox(7, 3).chessPiece = blackKing

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(3, 3),
            to = board.getBox(4, 5),
            player = game.currentTurn.value!!,
            pieceMoved = knight
        )

        val result = isDiscoveredAttack(board, move, game.currentTurn.value!!)
        assertEquals("Discovered Check", result)
    }

    @Test
    fun isSkewerReturnsSkewerWhenValuablePieceMovesExposingLowerPiece() {
        val board = Board()
        val stockfish = StockfishEngine()
        val game = GamePlay(board, stockfish)
        val player1 = Player(isWhite = true, PlayerType.HUMAN)
        val player2 = Player(isWhite = false, PlayerType.HUMAN)
        game.init(player1, player2, false)
        val recognizer = TacticsRecognizer(game)
        val isSkewer = recognizer::isSkewer

        board.clear()

        val queen = Queen(Color.WHITE, Position(0, 0), false)
        board.getBox(0, 0).chessPiece = queen

        val king = King(Color.BLACK, Position(4, 4), false)
        board.getBox(4, 4).chessPiece = king

        val rook = Rook(Color.BLACK, Position(4, 7), false)
        board.getBox(4, 7).chessPiece = rook

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 0),
            to = board.getBox(4, 0), // Move queen to a5
            player = game.currentTurn.value!!,
            pieceMoved = queen
        )

        val result = isSkewer(board, move)
        assertEquals("Skewer", result)
    }

    @Test
    fun isDoubleCheckReturnsDoubleCheckWhenTwoPiecesCheckOpponentKing() {
        val board = Board()
        val stockfish = StockfishEngine()
        val game = GamePlay(board, stockfish)
        val player1 = Player(isWhite = true, PlayerType.HUMAN)
        val player2 = Player(isWhite = false, PlayerType.HUMAN)
        game.init(player1, player2, false)
        val recognizer = TacticsRecognizer(game)
        val isDoubleCheck = recognizer::isDoubleCheck

        board.clear()

        val bishop = Bishop(Color.WHITE, Position(3, 1), false)
        board.getBox(3, 1).chessPiece = bishop

        val knight = Knight(Color.WHITE, Position(1, 3), false)
        board.getBox(1, 3).chessPiece = knight

        val blackKing = King(Color.BLACK, Position(0, 4), false)
        board.getBox(0, 4).chessPiece = blackKing

        // Set white to move
        game.currentTurn.value = player1

        // White knight moves to g7 (6, 6), giving check and revealing bishop check
        val move = Move(
            from = board.getBox(1, 3),
            to = board.getBox(2, 5),
            player = game.currentTurn.value!!,
            pieceMoved = knight
        )

        val result = isDoubleCheck(board, move)

        assertEquals("Double Check", result)
    }



    private fun Board.clear() {
        for (row in 0..7) {
            for (col in 0..7) {
                getBox(row, col).chessPiece = null
            }
        }
    }

}