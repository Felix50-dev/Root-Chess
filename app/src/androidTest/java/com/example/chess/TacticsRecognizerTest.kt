package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Move
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.repository.TacticsRecognizer
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class TacticsRecognizerTest {

    private lateinit var board: Board
    private lateinit var game: GamePlay
    private lateinit var player1: Player
    private lateinit var player2: Player
    private lateinit var recognizer: TacticsRecognizer

    @Before
    fun setup() {
        board = Board()
        val stockfish = StockfishEngine()
        game = GamePlay(board, stockfish)
        player1 = Player(isWhite = true, PlayerType.HUMAN)
        player2 = Player(isWhite = false, PlayerType.HUMAN)
        game.init(player1, player2, false)
        recognizer = TacticsRecognizer(game)
        board.clear()
    }

    @Test
    fun isForkReturnsForkWhenAttackingTwoValuablePieces() {
        val isFork = recognizer::isFork

        val queen = Queen(color = Color.BLACK, position = Position(5, 6), isKilled = false)
        board.getBox(5,6).chessPiece = queen

        val rook = Rook(color = Color.BLACK, position = Position(6, 5), isKilled = false)
        board.getBox(6,5).chessPiece = rook

        val knight = Knight(color = Color.WHITE, position = Position(3, 2), isKilled = false)
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
    fun isFork_kingMovingToAttackTwoPieces_returnsNull() {

        val king = King(color = Color.WHITE, position = Position(4, 4), isKilled =  false)
        board.getBox(4, 4).chessPiece = king

        val rook1 = Rook(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = rook1

        val rook2 = Rook(color = Color.BLACK, position = Position(3, 5), isKilled = false)
        board.getBox(3, 5).chessPiece = rook2

        val move = Move(
            from = board.getBox(4, 4),
            to = board.getBox(3, 4),
            player = player1,
            pieceMoved = king
        )

        val result = recognizer.isFork(board, move, player1)
        assertNull(result)
    }

    @Test
    fun isPin_bishopPinsKnightToKingOnDiagonal_returnsPin() {
        // Arrange
        //
        // Row 0: . . . . . . B .   (White Bishop at 0,6)
        // Row 2: . . . . B . . .   (Bishop moves here)
        // Row 3: . . . N . . . .   (Black Knight at 3,3) — pinned piece
        // Row 5: . K . . . . . .   (Black King at 5,1)

        val bishop = Bishop(color = Color.WHITE, position = Position(0, 6), isKilled = false)
        board.getBox(0, 6).chessPiece = bishop

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 6),
            to = board.getBox(2, 4),
            player = player1,
            pieceMoved = bishop
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertEquals("Pin", result)
    }

    @Test
    fun isPin_rookPinsKnightToKingOnFile_returnsPin() {
        // Arrange
        //
        // Col 3:
        // Row 0: . . . R . . . .   (White Rook at 0,3)
        // Row 3: . . . N . . . .   (Black Knight at 3,3) — pinned piece
        // Row 7: . . . K . . . .   (Black King at 7,3)

        val rook = Rook(color = Color.WHITE, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = rook

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 3),
            to = board.getBox(1, 3),   // rook moves one step down same file
            player = player1,
            pieceMoved = rook
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertEquals("Pin", result)
    }

    @Test
    fun isPin_queenPinsRookToKingOnRank_returnsPin() {
        // Arrange
        //
        // Row 4: Q . . . R . . K
        //       (4,0)  (4,4)  (4,7)

        val queen = Queen(color = Color.WHITE, position = Position(4, 0), isKilled = false)
        board.getBox(4, 0).chessPiece = queen

        val rook = Rook(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = rook

        val king = King(color = Color.BLACK, position = Position(4, 7), isKilled = false)
        board.getBox(4, 7).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(4, 0),
            to = board.getBox(4, 2),   // queen moves along rank toward pinned piece
            player = player1,
            pieceMoved = queen
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertEquals("Pin", result)
    }

    @Test
    fun isPin_twoBlockingPieces_returnsNull() {
        // Arrange — two pieces between attacker and king, not a pin
        //
        // Row 0: . . . . . . B .   (White Bishop at 0,6)
        // Row 2: . . . . B . . .   (Bishop moves here)
        // Row 3: . . . N . . . .   (Black Knight 1 at 3,3)
        // Row 4: . . N . . . . .   (Black Knight 2 at 4,2)
        // Row 5: . K . . . . . .   (Black King at 5,1)

        val bishop = Bishop(color = Color.WHITE, position = Position(0, 6), isKilled = false)
        board.getBox(0, 6).chessPiece = bishop

        val knight1 = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight1

        val knight2 = Knight(color = Color.BLACK, position = Position(4, 2), isKilled = false)
        board.getBox(4, 2).chessPiece = knight2

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 6),
            to = board.getBox(2, 4),
            player = player1,
            pieceMoved = bishop
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertNull(result)
    }

    @Test
    fun isPin_noBlockingPiece_returnsNull() {
        // Arrange — clear diagonal between bishop and king, no pin possible
        //
        // Row 2: . . . . B . . .   (Bishop moves here)
        // Row 5: . K . . . . . .   (Black King at 5,1)

        val bishop = Bishop(color = Color.WHITE, position = Position(0, 6), isKilled = false)
        board.getBox(0, 6).chessPiece = bishop

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 6),
            to = board.getBox(2, 4),
            player = player1,
            pieceMoved = bishop
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertNull(result)
    }

    @Test
    fun isPin_notAlignedWithKing_returnsNull() {
        // Arrange — bishop moves to square not aligned with king
        //
        // Row 1: . . . . . B . .   (Bishop moves here — not aligned with king)
        // Row 5: . K . . . . . .   (Black King at 5,1)

        val bishop = Bishop(color = Color.WHITE, position = Position(0, 6), isKilled = false)
        board.getBox(0, 6).chessPiece = bishop

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        // Bishop moves to (1,5) — not aligned with king at (5,1)
        // rowDiff=4, colDiff=4 — actually is diagonal ✅ let's pick truly unaligned
        // (0,6) to (1,4) — rowDiff to king = 4, colDiff = 3 — not aligned ✅
        val move = Move(
            from = board.getBox(0, 6),
            to = board.getBox(1, 4),
            player = player1,
            pieceMoved = bishop
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertNull(result)
    }

    @Test
    fun isPin_knightCannotPin_returnsNull() {
        // Arrange — knight is not a sliding piece, can never pin
        val knight = Knight(color = Color.WHITE, position = Position(2, 2), isKilled = false)
        board.getBox(2, 2).chessPiece = knight

        val pawn = Pawn(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = pawn

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(2, 2),
            to = board.getBox(4, 3),
            player = player1,
            pieceMoved = knight
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertNull(result)
    }

    @Test
    fun isDiscoveredAttackReturnsDiscoveredAttackWhenUncoveredPieceThreatensKing() {
        val isDiscoveredCheck = recognizer::isDiscoveredCheck

        val rook = Rook(color = Color.WHITE, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = rook

        val knight = Knight(color = Color.WHITE, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val blackKing = King(color = Color.BLACK, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = blackKing

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(3, 3),
            to = board.getBox(4, 5),
            player = game.currentTurn.value!!,
            pieceMoved = knight
        )

        val result = isDiscoveredCheck(board, move, game.currentTurn.value!!)
        assertEquals("Discovered Check", result)
    }

    @Test
    fun isSkewerReturnsSkewerWhenValuablePieceMovesExposingLowerPiece() {
        val isSkewer = recognizer::isSkewer

        val queen = Queen(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = queen

        val king = King(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = king

        val rook = Rook(color = Color.BLACK, position = Position(4, 7), isKilled = false)
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
    fun isSkewerReturnsSkewerOnDiagonal() {

        val bishop = Bishop(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = bishop

        val queen = Queen(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = queen

        val rook = Rook(color = Color.BLACK, position = Position(6, 6), isKilled = false)
        board.getBox(6, 6).chessPiece = rook

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 0),
            to = board.getBox(2, 2), // Bishop moves diagonally closer
            player = game.currentTurn.value!!,
            pieceMoved = bishop
        )

        val result = recognizer.isSkewer(board, move)
        assertEquals("Skewer", result)
    }


    @Test
    fun isDoubleCheckReturnsDoubleCheckWhenTwoPiecesCheckOpponentKing() {

        val bishop = Bishop(color = Color.WHITE, position = Position(3, 1), isKilled = false)
        board.getBox(3, 1).chessPiece = bishop

        val knight = Knight(color = Color.WHITE, position = Position(1, 3), isKilled = false)
        board.getBox(1, 3).chessPiece = knight

        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
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

        val result = recognizer.isDoubleCheck(board, move, player1)

        assertEquals("Double Check", result)
    }

    @Test
    fun isDiscoveredCheck_knightMovesRevealingRook_returnsDiscoveredCheck() {
        // Arrange
        //
        // Col 3:
        // Row 0: . . . R . . . .   (White Rook at 0,3)
        // Row 3: . . . N . . . .   (White Knight at 3,3) — blocking rook
        // Row 7: . . . K . . . .   (Black King at 7,3)
        //
        // Knight moves away from col 3, revealing rook's attack on king

        val rook = Rook(color = Color.WHITE, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = rook

        val knight = Knight(color = Color.WHITE, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val blackKing = King(color = Color.BLACK, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = blackKing

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(3, 3),
            to = board.getBox(4, 5),   // knight moves off column 3
            player = player1,
            pieceMoved = knight
        )

        // Act
        val result = recognizer.isDiscoveredCheck(board, move, player1)

        // Assert
        assertEquals("Discovered Check", result)
    }

    @Test
    fun isDiscoveredCheck_bishopMovesRevealingRookOnRank_returnsDiscoveredCheck() {
        // Arrange
        //
        // Row 4: R . B . . . . K
        //       (4,0) (4,2)   (4,7)
        //
        // Bishop on (4,2) blocks rook on (4,0) from attacking king on (4,7)
        // Bishop moves diagonally off rank 4, revealing rook

        val rook = Rook(color = Color.WHITE, position = Position(4, 0), isKilled = false)
        board.getBox(4, 0).chessPiece = rook

        val bishop = Bishop(color = Color.WHITE, position = Position(4, 2), isKilled = false)
        board.getBox(4, 2).chessPiece = bishop

        val blackKing = King(color = Color.BLACK, position = Position(4, 7), isKilled = false)
        board.getBox(4, 7).chessPiece = blackKing

        game.currentTurn.value = player1

        // Bishop moves diagonally — off rank 4, clearing path for rook
        val move = Move(
            from = board.getBox(4, 2),
            to = board.getBox(3, 3),
            player = player1,
            pieceMoved = bishop
        )

        // Act
        val result = recognizer.isDiscoveredCheck(board, move, player1)

        // Assert
        assertEquals("Discovered Check", result)
    }

    @Test
    fun isDiscoveredCheck_movingPieceAloneAttacksKing_returnsNull() {
        // Arrange
        //
        // Moving piece directly attacks king after move
        // but no other piece is revealed — just a regular check, not discovered
        //
        // Row 5: . . . . N . . .   (White Knight at 5,4)
        // Row 7: . . . K . . . .   (Black King at 7,3)
        //
        // Knight moves to (6,2) — attacks king at (7,4)? No
        // Knight at (5,4) to (7,3) — direct check, not discovered

        val knight = Knight(color = Color.WHITE, position = Position(5, 4), isKilled = false)
        board.getBox(5, 4).chessPiece = knight

        val blackKing = King(color = Color.BLACK, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = blackKing

        game.currentTurn.value = player1

        // Knight moves directly to attack king — rowDiff=2, colDiff=1 ✅
        val move = Move(
            from = board.getBox(5, 4),
            to = board.getBox(7, 3),
            player = player1,
            pieceMoved = knight
        )

        // Act
        val result = recognizer.isDiscoveredCheck(board, move, player1)

        // Assert — moving piece attacks king but it's not a discovered check
        assertNull(result)
    }

    @Test
    fun isDiscoveredCheck_noCheckAfterMove_returnsNull() {
        // Arrange
        //
        // Knight moves but reveals nothing — rook not aligned with king
        //
        // Row 0: . . . R . . . .   (White Rook at 0,3)
        // Row 3: . . . N . . . .   (White Knight at 3,3)
        // Row 7: . . . . . K . .   (Black King at 7,5) — different column
        //
        // Rook on col 3 can never attack king on col 5

        val rook = Rook(color = Color.WHITE, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = rook

        val knight = Knight(color = Color.WHITE, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val blackKing = King(color = Color.BLACK, position = Position(7, 5), isKilled = false)
        board.getBox(7, 5).chessPiece = blackKing

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(3, 3),
            to = board.getBox(4, 5),
            player = player1,
            pieceMoved = knight
        )

        // Act
        val result = recognizer.isDiscoveredCheck(board, move, player1)

        // Assert
        assertNull(result)
    }

    @Test
    fun isDiscoveredCheck_pawnMovesRevealingBishopOnDiagonal_returnsDiscoveredCheck() {
        // Arrange
        //
        // Row 4: . . . . . . . .
        // Row 5: . . . . . B . .   (White Bishop at 5,5)
        // Row 6: . . . . P . . .   (White Pawn at 6,4) — blocking bishop diagonal
        // Row 7: . . . K . . . .   (Black King at 7,3)
        //
        // Bishop on (5,5) → king on (7,3): rowDiff=2, colDiff=2 ✅ diagonal
        // Pawn on (6,4) sits on that diagonal — blocking bishop
        // Pawn moves forward to (5,4), clearing diagonal for bishop

        val bishop = Bishop(color = Color.WHITE, position = Position(5, 5), isKilled = false)
        board.getBox(5, 5).chessPiece = bishop

        val pawn = Pawn(color = Color.WHITE, position = Position(6, 4), isKilled = false)
        board.getBox(6, 4).chessPiece = pawn

        val blackKing = King(color = Color.BLACK, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = blackKing

        game.currentTurn.value = player1

        // Pawn moves forward — off the bishop's diagonal
        val move = Move(
            from = board.getBox(6, 4),
            to = board.getBox(5, 4),
            player = player1,
            pieceMoved = pawn
        )

        // Act
        val result = recognizer.isDiscoveredCheck(board, move, player1)

        // Assert
        assertEquals("Discovered Check", result)
    }

    private fun Board.clear() {
        for (row in 0..7) {
            for (col in 0..7) {
                getBox(row, col).chessPiece = null
            }
        }
    }

}
