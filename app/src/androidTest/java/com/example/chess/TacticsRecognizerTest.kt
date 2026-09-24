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
        game = testGamePlay(board, stockfish)
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
        assertEquals("Fork", result?.name)
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
        // Row 0: . . B . . . . .   (White Bishop at 0,2 — OFF the pin diagonal, so
        //                           this move genuinely creates the pin)
        // Row 2: . . . . B . . .   (Bishop moves here)
        // Row 3: . . . N . . . .   (Black Knight at 3,3) — pinned piece
        // Row 5: . K . . . . . .   (Black King at 5,1)

        val bishop = Bishop(color = Color.WHITE, position = Position(0, 2), isKilled = false)
        board.getBox(0, 2).chessPiece = bishop

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 2),
            to = board.getBox(2, 4),
            player = player1,
            pieceMoved = bishop
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertEquals("Pin", result?.name)
    }

    @Test
    fun isPin_rookPinsKnightToKingOnFile_returnsPin() {
        // Arrange
        //
        // Row 1: R . . → . . . .   (White Rook at 1,0 — off the pin file, so the
        //                           move genuinely creates the pin on column 3)
        // Row 3: . . . N . . . .   (Black Knight at 3,3) — pinned piece
        // Row 7: . . . K . . . .   (Black King at 7,3)

        val rook = Rook(color = Color.WHITE, position = Position(1, 0), isKilled = false)
        board.getBox(1, 0).chessPiece = rook

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(1, 0),
            to = board.getBox(1, 3),   // rook swings onto the king's file
            player = player1,
            pieceMoved = rook
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertEquals("Pin", result?.name)
    }

    @Test
    fun isPin_rookPinsRookToKingOnRank_returnsPin() {
        // Arrange
        //
        // Row 0: . . R . . . . .   (White Rook at 0,2 — off rank 4, so the move
        //                           genuinely creates the pin)
        // Row 4: . . R . r . . K
        //             (4,2) (4,4) (4,7)
        //
        // Uses a Rook (not Queen) as the attacker: since the pinned piece here is
        // itself a Rook that could always legally capture straight back along the
        // same rank (that stays on the pin line, so it never exposes the king
        // either way), a MORE valuable attacker (e.g. a Queen) would make this an
        // unsound pin regardless of any defender — trading a Queen for a Rook loses
        // material even if the Queen is recaptured afterward. An equal-value
        // attacker keeps the trade neutral-or-better for White once defended.

        val rookAttacker = Rook(color = Color.WHITE, position = Position(0, 2), isKilled = false)
        board.getBox(0, 2).chessPiece = rookAttacker

        // Defends the attacking rook's landing square (4,2) — without this, the
        // pinned rook could simply capture it for an even trade, and an undefended
        // even trade still isn't a real, lasting pin.
        val knight = Knight(color = Color.WHITE, position = Position(2, 1), isKilled = false)
        board.getBox(2, 1).chessPiece = knight

        val pinnedRook = Rook(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = pinnedRook

        val king = King(color = Color.BLACK, position = Position(4, 7), isKilled = false)
        board.getBox(4, 7).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 2),
            to = board.getBox(4, 2),   // rook drops onto rank 4, creating the pin
            player = player1,
            pieceMoved = rookAttacker
        )

        // Act
        val result = recognizer.isPin(board, move, player1)

        // Assert
        assertEquals("Pin", result?.name)
    }

    @Test
    fun isPin_pinnerHangingForFree_returnsNull() {
        // Deliberately uses a Queen (not a Rook) as the attacker, undefended — the
        // "pinned" rook can simply capture the queen for free (staying on the same
        // rank keeps the black king just as safe either way), so this isn't a real
        // pin regardless of the value mismatch even being defended.
        val queen = Queen(color = Color.WHITE, position = Position(4, 0), isKilled = false)
        board.getBox(4, 0).chessPiece = queen

        val rook = Rook(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = rook

        val king = King(color = Color.BLACK, position = Position(4, 7), isKilled = false)
        board.getBox(4, 7).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(4, 0),
            to = board.getBox(4, 2),
            player = player1,
            pieceMoved = queen
        )

        val result = recognizer.isPin(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isPin_pinnedPieceDefendedElsewhere_pinStandsButCaptureIsUnsafe() {
        // Arrange
        //
        // Row 0: . . B . . . . .   (White Bishop at 0,2 — off the pin diagonal)
        // Row 2: . . . . B . . .   (Bishop's landing square when creating the pin)
        // Row 3: r . . N . . . .   (Black Rook at 3,0 — defends 3,3 along the rank,
        //                           completely independent of the bishop's diagonal)
        // Row 5: . K . . . . . .   (Black King at 5,1)
        //
        // Same geometry as isPin_bishopPinsKnightToKingOnDiagonal_returnsPin, plus a
        // rook that has nothing to do with the pin diagonal but still defends the
        // knight's square. A pin only proves the knight can't move without losing its
        // king — it says nothing about whether marching in and capturing the knight is
        // actually safe. isPin must still report the pin (it's real), but must not
        // report it for the follow-up capture, since the bishop would just be hanging
        // to the rook there.
        val bishop = Bishop(color = Color.WHITE, position = Position(0, 2), isKilled = false)
        board.getBox(0, 2).chessPiece = bishop

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val rook = Rook(color = Color.BLACK, position = Position(3, 0), isKilled = false)
        board.getBox(3, 0).chessPiece = rook

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        // The pin-creating move: bishop relocates onto the diagonal, still just a
        // geometric restriction on the knight — unaffected by the rook.
        val pinningMove = Move(
            from = board.getBox(0, 2),
            to = board.getBox(2, 4),
            player = player1,
            pieceMoved = bishop
        )
        assertEquals("Pin", recognizer.isPin(board, pinningMove, player1)?.name)

        // The follow-up capture: bishop actually takes the "pinned" knight. The rook
        // has a clear, unrelated rank to recapture on 3,3 once the knight is gone, so
        // this specific move must not be praised as a Pin.
        val captureMove = Move(
            from = board.getBox(2, 4),
            to = board.getBox(3, 3),
            player = player1,
            pieceMoved = bishop
        )
        assertNull(recognizer.isPin(board, captureMove, player1))
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
        assertEquals("Discovered Check", result?.name)
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
        assertEquals("Skewer", result?.name)
    }

    @Test
    fun isSkewerReturnsSkewerOnDiagonal() {

        // Bishop starts OFF the skewer diagonal (0,4) so the move genuinely creates
        // the skewer rather than sliding along a line it already controlled.
        val bishop = Bishop(color = Color.WHITE, position = Position(0, 4), isKilled = false)
        board.getBox(0, 4).chessPiece = bishop

        // Defends the bishop's landing square (2,2) — without this, the queen could
        // just recapture the bishop for free, which isn't a sound skewer at all.
        val knight = Knight(color = Color.WHITE, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = knight

        val queen = Queen(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = queen

        val rook = Rook(color = Color.BLACK, position = Position(6, 6), isKilled = false)
        board.getBox(6, 6).chessPiece = rook

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 4),
            to = board.getBox(2, 2), // Bishop lands on the queen–rook diagonal
            player = game.currentTurn.value!!,
            pieceMoved = bishop
        )

        val result = recognizer.isSkewer(board, move)
        assertEquals("Skewer", result?.name)
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

        assertEquals("Double Check", result?.name)
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
        assertEquals("Discovered Check", result?.name)
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
        assertEquals("Discovered Check", result?.name)
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
        assertEquals("Discovered Check", result?.name)
    }

    // ── Fork: additional coverage ──────────────────────────────────────────────

    @Test
    fun isFork_targetBelowValueThreshold_notCounted_onlyOneQualifyingTarget_returnsNull() {
        // Knight forks a Rook (value 5, qualifies) and a Pawn (value 1, below
        // MIN_FORK_TARGET_VALUE=3) — only one qualifying target, so this is not a fork.
        val knight = Knight(color = Color.WHITE, position = Position(3, 2), isKilled = false)
        board.getBox(3, 2).chessPiece = knight

        val rook = Rook(color = Color.BLACK, position = Position(5, 6), isKilled = false)
        board.getBox(5, 6).chessPiece = rook

        val pawn = Pawn(color = Color.BLACK, position = Position(6, 5), isKilled = false)
        board.getBox(6, 5).chessPiece = pawn

        game.currentTurn.value = player1

        val move = Move(board.getBox(3, 2), board.getBox(4, 4), player1, knight)

        val result = recognizer.isFork(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isFork_blockedByInterveningPiece_targetNotActuallyAttacked_returnsNull() {
        // Row 0: . . . . R . . .   (Black Rook at 0,4 — column target, but blocked)
        // Row 2: . . . . P . . .   (White Pawn at 2,4 — blocks the column, own color)
        // Row 4: . . . . R . . Q   (White Rook at 4,4 moves here; Black Queen at 4,7)
        //
        // The rook "forks" along its row (reaches the queen, unblocked) and its column
        // (blocked by its own pawn before reaching the black rook) — only 1 real
        // qualifying target, so this should NOT be a fork.
        val rook = Rook(color = Color.WHITE, position = Position(6, 4), isKilled = false)
        board.getBox(6, 4).chessPiece = rook

        val queen = Queen(color = Color.BLACK, position = Position(4, 7), isKilled = false)
        board.getBox(4, 7).chessPiece = queen

        val blocker = Pawn(color = Color.WHITE, position = Position(2, 4), isKilled = false)
        board.getBox(2, 4).chessPiece = blocker

        val blackRook = Rook(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        board.getBox(0, 4).chessPiece = blackRook

        game.currentTurn.value = player1

        val move = Move(board.getBox(6, 4), board.getBox(4, 4), player1, rook)

        val result = recognizer.isFork(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isFork_defendedTargets_stillReturnsFork_forkerItselfIsSafe() {
        // Same geometry as isForkReturnsForkWhenAttackingTwoValuablePieces, but both
        // targets are defended (king guards the queen, pawn guards the rook). isFork
        // only checks whether the FORKING piece itself is safe, not whether the
        // individual targets are defended — the opponent still can't save both pieces
        // at once, so this remains a real threat even though neither capture is "free."
        // The knight's own landing square (4,4) isn't attacked by anything here, so
        // it stays a sound fork.
        val knight = Knight(color = Color.WHITE, position = Position(3, 2), isKilled = false)
        board.getBox(3, 2).chessPiece = knight

        val queen = Queen(color = Color.BLACK, position = Position(5, 6), isKilled = false)
        board.getBox(5, 6).chessPiece = queen
        val queenDefender = King(color = Color.BLACK, position = Position(6, 7), isKilled = false)
        board.getBox(6, 7).chessPiece = queenDefender

        val rook = Rook(color = Color.BLACK, position = Position(6, 5), isKilled = false)
        board.getBox(6, 5).chessPiece = rook
        val rookDefender = Pawn(color = Color.BLACK, position = Position(5, 4), isKilled = false)
        board.getBox(5, 4).chessPiece = rookDefender

        game.currentTurn.value = player1

        val move = Move(board.getBox(3, 2), board.getBox(4, 4), player1, knight)

        val result = recognizer.isFork(board, move, player1)

        assertEquals("Fork", result?.name)
    }

    @Test
    fun isFork_playerDeterminesPerspective_wrongPlayerReturnsNull() {
        // isFork derives "my own pieces" from the `player` parameter, not from
        // piece.color — passing the wrong player (black) for a white knight's fork
        // makes the two real black targets look like "my own side" and get skipped,
        // so no fork is found. Confirms `player` is genuinely load-bearing.
        val knight = Knight(color = Color.WHITE, position = Position(3, 2), isKilled = false)
        board.getBox(3, 2).chessPiece = knight

        val queen = Queen(color = Color.BLACK, position = Position(5, 6), isKilled = false)
        board.getBox(5, 6).chessPiece = queen

        val rook = Rook(color = Color.BLACK, position = Position(6, 5), isKilled = false)
        board.getBox(6, 5).chessPiece = rook

        game.currentTurn.value = player1

        val move = Move(board.getBox(3, 2), board.getBox(4, 4), player1, knight)

        val result = recognizer.isFork(board, move, player2)  // wrong player on purpose

        assertNull(result)
    }

    @Test
    fun isFork_forkerHangingForFree_returnsNull() {
        // Same fork geometry as isForkReturnsForkWhenAttackingTwoValuablePieces, but
        // the knight's landing square (4,4) is attacked by an undefended black
        // bishop — the opponent just takes the knight for free instead of losing a
        // piece to the fork, so this is not a real threat.
        val knight = Knight(color = Color.WHITE, position = Position(3, 2), isKilled = false)
        board.getBox(3, 2).chessPiece = knight

        val queen = Queen(color = Color.BLACK, position = Position(5, 6), isKilled = false)
        board.getBox(5, 6).chessPiece = queen

        val rook = Rook(color = Color.BLACK, position = Position(6, 5), isKilled = false)
        board.getBox(6, 5).chessPiece = rook

        val bishop = Bishop(color = Color.BLACK, position = Position(2, 2), isKilled = false)
        board.getBox(2, 2).chessPiece = bishop  // clear diagonal to (4,4), undefended

        game.currentTurn.value = player1

        val move = Move(board.getBox(3, 2), board.getBox(4, 4), player1, knight)

        val result = recognizer.isFork(board, move, player1)

        assertNull(result)
    }

    // ── Pin: additional coverage ───────────────────────────────────────────────

    @Test
    fun isPin_relativePinAgainstQueen_returnsPin() {
        // Row 0: R . . → . . . .   (White Rook at 0,0 swings onto column 3)
        // Row 3: . . . N . . . .   (Black Knight at 3,3) — pinned to the queen
        // Row 6: . . . Q . . . .   (Black Queen at 6,3)
        // Row 7: K . . . . . . .   (Black King at 7,0) — NOT aligned with column 3
        //
        // Textbook relative pin: the knight cannot move without dropping the queen
        // behind it. Detected via the back-piece-more-valuable rule (Queen 9 > Knight
        // 3) — no king on the line required.
        val rook = Rook(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = rook

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val queen = Queen(color = Color.BLACK, position = Position(6, 3), isKilled = false)
        board.getBox(6, 3).chessPiece = queen

        val king = King(color = Color.BLACK, position = Position(7, 0), isKilled = false)
        board.getBox(7, 0).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 0),
            to = board.getBox(0, 3),
            player = player1,
            pieceMoved = rook
        )

        val result = recognizer.isPin(board, move, player1)

        assertEquals("Pin", result?.name)
    }

    @Test
    fun isPin_blockingPieceOwnColor_returnsNull() {
        // Same geometry as isPin_bishopPinsKnightToKingOnDiagonal_returnsPin, but the
        // blocking piece is WHITE (the attacker's own color) — you can't pin your own
        // piece to the enemy king.
        val bishop = Bishop(color = Color.WHITE, position = Position(0, 6), isKilled = false)
        board.getBox(0, 6).chessPiece = bishop

        val knight = Knight(color = Color.WHITE, position = Position(3, 3), isKilled = false)
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

        val result = recognizer.isPin(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isPin_diagonallyAlignedRookCannotPin_returnsNull() {
        // Row 0: R . . . . . . .   (White Rook moves here — diagonally aligned with
        //                           the king, but a rook can't actually move diagonally)
        // Row 2: . . N . . . . .   (Black Knight at 2,2) — sole blocker on the diagonal
        // Row 4: . . . . K . . .   (Black King at 4,4)
        //
        // isAligned() treats row/column/diagonal alignment the same way regardless of
        // piece type, so this passes the coarse pre-filter — but the final
        // canMove-based verification step should correctly reject it since a rook
        // cannot move diagonally.
        val rook = Rook(color = Color.WHITE, position = Position(1, 0), isKilled = false)
        board.getBox(1, 0).chessPiece = rook

        val knight = Knight(color = Color.BLACK, position = Position(2, 2), isKilled = false)
        board.getBox(2, 2).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(1, 0),
            to = board.getBox(0, 0),
            player = player1,
            pieceMoved = rook
        )

        val result = recognizer.isPin(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isPin_playerDeterminesPerspective_wrongPlayerReturnsNull() {
        // Same geometry as isPin_bishopPinsKnightToKingOnDiagonal_returnsPin, but
        // passing the wrong player (black) for a white bishop's pin makes the pinned
        // black knight look like "my own side," so the ray breaks immediately instead
        // of finding a pin. Confirms `player` is genuinely load-bearing here too.
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

        val result = recognizer.isPin(board, move, player2)  // wrong player on purpose

        assertNull(result)
    }

    // ── Skewer: additional coverage ────────────────────────────────────────────

    @Test
    fun isSkewer_weakerPieceInFront_strongerBehind_returnsNull() {
        // Reversed value order from isSkewerReturnsSkewerOnDiagonal — this is a PIN
        // shape (weaker piece in front of a more valuable one), not a skewer.
        val bishop = Bishop(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = bishop

        val rook = Rook(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = rook

        val queen = Queen(color = Color.BLACK, position = Position(6, 6), isKilled = false)
        board.getBox(6, 6).chessPiece = queen

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 0),
            to = board.getBox(2, 2),
            player = player1,
            pieceMoved = bishop
        )

        val result = recognizer.isSkewer(board, move)

        assertNull(result)
    }

    @Test
    fun isSkewer_equalValuePieces_returnsNull() {
        // Two rooks (equal value 5) on the same rank — the comparison is strictly
        // greater-than, so equal values don't count as a skewer.
        val rook = Rook(color = Color.WHITE, position = Position(4, 0), isKilled = false)
        board.getBox(4, 0).chessPiece = rook

        val blackRook1 = Rook(color = Color.BLACK, position = Position(4, 3), isKilled = false)
        board.getBox(4, 3).chessPiece = blackRook1

        val blackRook2 = Rook(color = Color.BLACK, position = Position(4, 6), isKilled = false)
        board.getBox(4, 6).chessPiece = blackRook2

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(4, 0),
            to = board.getBox(4, 1),
            player = player1,
            pieceMoved = rook
        )

        val result = recognizer.isSkewer(board, move)

        assertNull(result)
    }

    @Test
    fun isSkewer_blockedByOwnColorPiece_returnsNull() {
        // Own-color pawn sits directly in the ray before any enemy piece — the scan
        // stops there, never reaching the king/rook further along the row.
        val rook = Rook(color = Color.WHITE, position = Position(4, 0), isKilled = false)
        board.getBox(4, 0).chessPiece = rook

        val whitePawn = Pawn(color = Color.WHITE, position = Position(4, 2), isKilled = false)
        board.getBox(4, 2).chessPiece = whitePawn

        val king = King(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = king

        val blackRook = Rook(color = Color.BLACK, position = Position(4, 7), isKilled = false)
        board.getBox(4, 7).chessPiece = blackRook

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(4, 0),
            to = board.getBox(4, 1),
            player = player1,
            pieceMoved = rook
        )

        val result = recognizer.isSkewer(board, move)

        assertNull(result)
    }

    @Test
    fun isSkewer_onlyOneEnemyPieceOnRay_returnsNull() {
        // Just one enemy piece on the ray, nothing behind it — an attack, not a skewer.
        val rook = Rook(color = Color.WHITE, position = Position(4, 0), isKilled = false)
        board.getBox(4, 0).chessPiece = rook

        val king = King(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(4, 0),
            to = board.getBox(4, 1),
            player = player1,
            pieceMoved = rook
        )

        val result = recognizer.isSkewer(board, move)

        assertNull(result)
    }

    @Test
    fun isSkewer_kingInFrontForcesMove_alwaysDetected() {
        // Queen attacks along a diagonal — King (max value) must move, exposing a
        // Knight behind it. Distinct from the existing skewer tests: attacker is a
        // Queen and the exposed piece is a Knight, neither yet covered. The queen
        // starts off the diagonal so the move genuinely creates the skewer.
        val queen = Queen(color = Color.WHITE, position = Position(1, 7), isKilled = false)
        board.getBox(1, 7).chessPiece = queen

        val king = King(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = king

        val knight = Knight(color = Color.BLACK, position = Position(5, 5), isKilled = false)
        board.getBox(5, 5).chessPiece = knight

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(1, 7),
            to = board.getBox(1, 1),
            player = player1,
            pieceMoved = queen
        )

        val result = recognizer.isSkewer(board, move)

        assertEquals("Skewer", result?.name)
    }

    @Test
    fun isSkewer_relativePinShape_notASkewer_returnsNull() {
        // A relative-pin shape (knight in front of the queen): isSkewer's value check
        // (front > back) correctly rejects it (3 > 9 is false) — this is isPin's
        // territory, covered by isPin_relativePinAgainstQueen_returnsPin.
        val rook = Rook(color = Color.WHITE, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = rook

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val queen = Queen(color = Color.BLACK, position = Position(6, 3), isKilled = false)
        board.getBox(6, 3).chessPiece = queen

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 3),
            to = board.getBox(1, 3),
            player = player1,
            pieceMoved = rook
        )

        val result = recognizer.isSkewer(board, move)

        assertNull(result)
    }

    @Test
    fun isSkewer_skewererHangingForFree_returnsNull() {
        // Same geometry as isSkewerReturnsSkewerOnDiagonal, but without the defending
        // knight — the bishop's landing square (2,2) is undefended and directly
        // attacked by the queen it's "skewering," so the opponent just takes the
        // bishop for free instead of losing the rook behind it.
        val bishop = Bishop(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = bishop

        val queen = Queen(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = queen

        val rook = Rook(color = Color.BLACK, position = Position(6, 6), isKilled = false)
        board.getBox(6, 6).chessPiece = rook

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 0),
            to = board.getBox(2, 2),
            player = player1,
            pieceMoved = bishop
        )

        val result = recognizer.isSkewer(board, move)

        assertNull(result)
    }

    // ── Discovered Check: additional coverage ──────────────────────────────────

    @Test
    fun isDiscoveredCheck_preExistingAttackerUnrelatedMove_returnsNull() {
        // White Rook already has a completely open, unblocked file to the Black king
        // BEFORE the move. A White Knight elsewhere makes a completely unrelated move
        // that doesn't touch column 3 at all. Nothing was newly revealed, so this
        // is NOT a discovered check. Works because Board.clone() preserves piece ids,
        // making the before/after attacker-ID diff meaningful across clones.
        val rook = Rook(color = Color.WHITE, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = rook

        val king = King(color = Color.BLACK, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = king

        val knight = Knight(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = knight

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 0),
            to = board.getBox(2, 1),
            player = player1,
            pieceMoved = knight
        )

        val result = recognizer.isDiscoveredCheck(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isDiscoveredCheck_directCheckAlsoRevealsAnotherAttacker_stillReturnsDiscoveredCheck() {
        // Same board as isDoubleCheckReturnsDoubleCheckWhenTwoPiecesCheckOpponentKing —
        // the knight both directly checks the king AND reveals the bishop's check by
        // moving off its diagonal. isDiscoveredCheck should also fire here (the
        // bishop is a genuine newly-revealed attacker, distinct from the moving
        // knight, which is correctly excluded). Unlike the KNOWN_BUG test above, the
        // bishop here was never attacking before the move (fully blocked by the
        // knight), so this case doesn't depend on the broken ID diff to be correct.
        val bishop = Bishop(color = Color.WHITE, position = Position(3, 1), isKilled = false)
        board.getBox(3, 1).chessPiece = bishop

        val knight = Knight(color = Color.WHITE, position = Position(1, 3), isKilled = false)
        board.getBox(1, 3).chessPiece = knight

        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        board.getBox(0, 4).chessPiece = blackKing

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(1, 3),
            to = board.getBox(2, 5),
            player = player1,
            pieceMoved = knight
        )

        val result = recognizer.isDiscoveredCheck(board, move, player1)

        assertEquals("Discovered Check", result?.name)
    }

    @Test
    fun isDiscoveredCheck_correctPlayerColorHandling_player2Perspective() {
        // Mirror of isDiscoveredCheck_knightMovesRevealingRook_returnsDiscoveredCheck
        // but from Black's perspective (player2 to move), confirming opponentColor
        // derivation from the `player` parameter works symmetrically for both sides.
        val rook = Rook(color = Color.BLACK, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = rook

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val whiteKing = King(color = Color.WHITE, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = whiteKing

        game.currentTurn.value = player2

        val move = Move(
            from = board.getBox(3, 3),
            to = board.getBox(4, 5),
            player = player2,
            pieceMoved = knight
        )

        val result = recognizer.isDiscoveredCheck(board, move, player2)

        assertEquals("Discovered Check", result?.name)
    }

    // ── Discovered Attack ────────────────────────────────────────────────────

    @Test
    fun isDiscoveredAttack_knightMovesRevealingBishopAttackOnRook_returnsDiscoveredAttack() {
        // Row 0: B . . . . . . .   (White Bishop at 0,0)
        // Row 2: . . N . . . . .   (White Knight at 2,2) — blocks the diagonal
        // Row 4: . . . . r . . .   (Black Rook at 4,4)
        //
        // Knight moves off the diagonal, revealing the bishop's attack on the rook —
        // a non-king target, so isDiscoveredCheck wouldn't catch this at all.
        val bishop = Bishop(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = bishop

        val knight = Knight(color = Color.WHITE, position = Position(2, 2), isKilled = false)
        board.getBox(2, 2).chessPiece = knight

        val rook = Rook(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = rook

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(2, 2),
            to = board.getBox(4, 1),   // knight moves off the diagonal
            player = player1,
            pieceMoved = knight
        )

        val result = recognizer.isDiscoveredAttack(board, move, player1)

        assertEquals("Discovered Attack", result?.name)
    }

    @Test
    fun isDiscoveredAttack_onlyRevealsCheckOnKing_returnsNull() {
        // Same board as isDiscoveredCheck_knightMovesRevealingRook_returnsDiscoveredCheck
        // — the only thing revealed is a check on the king, which is
        // isDiscoveredCheck's job; kings are explicitly excluded as targets here.
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
            player = player1,
            pieceMoved = knight
        )

        val result = recognizer.isDiscoveredAttack(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isDiscoveredAttack_revealedTargetBelowValueThreshold_returnsNull() {
        // Same shape as the positive test, but the revealed target is a pawn (value
        // 1, below MIN_FORK_TARGET_VALUE) instead of a rook.
        val bishop = Bishop(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = bishop

        val knight = Knight(color = Color.WHITE, position = Position(2, 2), isKilled = false)
        board.getBox(2, 2).chessPiece = knight

        val pawn = Pawn(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = pawn

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(2, 2),
            to = board.getBox(4, 1),
            player = player1,
            pieceMoved = knight
        )

        val result = recognizer.isDiscoveredAttack(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isDiscoveredAttack_noReveal_returnsNull() {
        // Knight moves, but nothing was ever blocking a line to anything.
        val knight = Knight(color = Color.WHITE, position = Position(2, 2), isKilled = false)
        board.getBox(2, 2).chessPiece = knight

        val rook = Rook(color = Color.BLACK, position = Position(6, 6), isKilled = false)
        board.getBox(6, 6).chessPiece = rook

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(2, 2),
            to = board.getBox(4, 1),
            player = player1,
            pieceMoved = knight
        )

        val result = recognizer.isDiscoveredAttack(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isDiscoveredAttack_preExistingAttackerUnrelatedMove_returnsNull() {
        // Bishop already has a completely open diagonal to the rook BEFORE the move;
        // an unrelated knight move elsewhere doesn't touch that diagonal at all.
        // Nothing was newly revealed, so this should not be a discovered attack.
        val bishop = Bishop(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = bishop

        val rook = Rook(color = Color.BLACK, position = Position(4, 4), isKilled = false)
        board.getBox(4, 4).chessPiece = rook

        val knight = Knight(color = Color.WHITE, position = Position(0, 7), isKilled = false)
        board.getBox(0, 7).chessPiece = knight

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 7),
            to = board.getBox(2, 6),
            player = player1,
            pieceMoved = knight
        )

        val result = recognizer.isDiscoveredAttack(board, move, player1)

        assertNull(result)
    }

    // ── Double Check: additional coverage ──────────────────────────────────────

    @Test
    fun isDoubleCheck_onlySingleAttacker_returnsNull() {
        // Exactly one checking piece post-move — below the >= 2 threshold.
        val knight = Knight(color = Color.WHITE, position = Position(5, 4), isKilled = false)
        board.getBox(5, 4).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(5, 4),
            to = board.getBox(7, 3),
            player = player1,
            pieceMoved = knight
        )

        val result = recognizer.isDoubleCheck(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isDoubleCheck_noAttackers_returnsNull() {
        // No checks at all after the move.
        val knight = Knight(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(7, 7), isKilled = false)
        board.getBox(7, 7).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 0),
            to = board.getBox(2, 1),
            player = player1,
            pieceMoved = knight
        )

        val result = recognizer.isDoubleCheck(board, move, player1)

        assertNull(result)
    }

    // ── Pin: tactical-significance rules ───────────────────────────────────────

    @Test
    fun isPin_defendedPawnPinnedOnDiagonal_returnsNull() {
        // Queen "pins" a DEFENDED pawn to its king on a diagonal. Geometrically true,
        // tactically meaningless: the queen can never profitably capture the pawn, and
        // immobilizing a pawn wins nothing — must not be praised as a Pin.
        val queen = Queen(color = Color.WHITE, position = Position(0, 2), isKilled = false)
        board.getBox(0, 2).chessPiece = queen

        val pawn = Pawn(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = pawn

        // Defends the pawn along rank 3, off the pin diagonal.
        val rook = Rook(color = Color.BLACK, position = Position(3, 0), isKilled = false)
        board.getBox(3, 0).chessPiece = rook

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 2),
            to = board.getBox(2, 4),
            player = player1,
            pieceMoved = queen
        )

        val result = recognizer.isPin(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isPin_undefendedPawnPinnedOnDiagonal_returnsPin() {
        // Same geometry without the defender: the pinned pawn can actually be won,
        // so the pin is a real threat.
        val queen = Queen(color = Color.WHITE, position = Position(0, 2), isKilled = false)
        board.getBox(0, 2).chessPiece = queen

        val pawn = Pawn(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = pawn

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 2),
            to = board.getBox(2, 4),
            player = player1,
            pieceMoved = queen
        )

        val result = recognizer.isPin(board, move, player1)

        assertEquals("Pin", result?.name)
    }

    @Test
    fun isPin_pawnOnFileBetweenRookAndKing_returnsNull() {
        // A pawn "pinned" along a FILE isn't pinned at all — its pushes stay on the
        // file, so nothing is restrained (even though this pawn is undefended).
        val rook = Rook(color = Color.WHITE, position = Position(1, 0), isKilled = false)
        board.getBox(1, 0).chessPiece = rook

        val pawn = Pawn(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = pawn

        val king = King(color = Color.BLACK, position = Position(6, 3), isKilled = false)
        board.getBox(6, 3).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(1, 0),
            to = board.getBox(1, 3),
            player = player1,
            pieceMoved = rook
        )

        val result = recognizer.isPin(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isPin_preExistingPinRookSlidesAlongSameFile_returnsNull() {
        // The rook ALREADY pinned the knight from (0,3); sliding one square down the
        // same file doesn't create anything — re-reporting it every ply would praise
        // moves that achieved nothing.
        val rook = Rook(color = Color.WHITE, position = Position(0, 3), isKilled = false)
        board.getBox(0, 3).chessPiece = rook

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(7, 3), isKilled = false)
        board.getBox(7, 3).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 3),
            to = board.getBox(1, 3),
            player = player1,
            pieceMoved = rook
        )

        val result = recognizer.isPin(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isPin_descriptionNamesColorsPiecesAndSquares() {
        // Grounding check for the LLM Facts block: colors, piece types, and algebraic
        // squares must be exact. Mapping: (2,4)→e6, (3,3)→d5, (5,1)→b3.
        val bishop = Bishop(color = Color.WHITE, position = Position(0, 2), isKilled = false)
        board.getBox(0, 2).chessPiece = bishop

        val knight = Knight(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(5, 1), isKilled = false)
        board.getBox(5, 1).chessPiece = king

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(0, 2),
            to = board.getBox(2, 4),
            player = player1,
            pieceMoved = bishop
        )

        val result = recognizer.isPin(board, move, player1)

        assertEquals(
            "The White Bishop on e6 pins the Black Knight on d5 against the Black King on b3.",
            result?.description
        )
    }

    // ── Fork: tactical-significance rules ──────────────────────────────────────

    @Test
    fun isFork_queenAttacksTwoDefendedKnights_returnsNull() {
        // Queen "forks" two DEFENDED knights: capturing either loses 9-for-3, so
        // neither attack threatens anything — not a fork.
        val queen = Queen(color = Color.WHITE, position = Position(6, 4), isKilled = false)
        board.getBox(6, 4).chessPiece = queen

        val knight1 = Knight(color = Color.BLACK, position = Position(2, 4), isKilled = false)
        board.getBox(2, 4).chessPiece = knight1
        val knight1Defender = Rook(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        board.getBox(0, 4).chessPiece = knight1Defender

        val knight2 = Knight(color = Color.BLACK, position = Position(4, 2), isKilled = false)
        board.getBox(4, 2).chessPiece = knight2
        val knight2Defender = Rook(color = Color.BLACK, position = Position(4, 0), isKilled = false)
        board.getBox(4, 0).chessPiece = knight2Defender

        game.currentTurn.value = player1

        val move = Move(board.getBox(6, 4), board.getBox(4, 4), player1, queen)

        val result = recognizer.isFork(board, move, player1)

        assertNull(result)
    }

    @Test
    fun isFork_royalForkKingAndDefendedQueen_returnsFork() {
        // Knight forks the king (check — always a real threat) and a DEFENDED queen
        // (still profitable: queen is worth more than the knight). The strongest kind
        // of fork, must be detected despite the queen's defender.
        val knight = Knight(color = Color.WHITE, position = Position(3, 2), isKilled = false)
        board.getBox(3, 2).chessPiece = knight

        val king = King(color = Color.BLACK, position = Position(2, 3), isKilled = false)
        board.getBox(2, 3).chessPiece = king

        val queen = Queen(color = Color.BLACK, position = Position(5, 6), isKilled = false)
        board.getBox(5, 6).chessPiece = queen
        val queenDefender = Pawn(color = Color.BLACK, position = Position(4, 5), isKilled = false)
        board.getBox(4, 5).chessPiece = queenDefender

        game.currentTurn.value = player1

        val move = Move(board.getBox(3, 2), board.getBox(4, 4), player1, knight)

        val result = recognizer.isFork(board, move, player1)

        assertEquals("Fork", result?.name)
    }

    @Test
    fun isFork_sliderKeepsSameTargetsAfterSliding_returnsNull() {
        // The queen already attacked both undefended targets from its origin square
        // (the knight down the file, the other knight along the rank). Sliding along
        // the rank keeps both under attack — the double attack wasn't created by this
        // move, so it must not be re-reported as a fork.
        val queen = Queen(color = Color.WHITE, position = Position(2, 0), isKilled = false)
        board.getBox(2, 0).chessPiece = queen

        // Attacked from (2,0) along the rank and still from (2,3).
        val knight1 = Knight(color = Color.BLACK, position = Position(2, 7), isKilled = false)
        board.getBox(2, 7).chessPiece = knight1

        // Attacked from (2,0) down the file and still from (2,3) via the diagonal.
        val knight2 = Knight(color = Color.BLACK, position = Position(5, 0), isKilled = false)
        board.getBox(5, 0).chessPiece = knight2

        game.currentTurn.value = player1

        val move = Move(board.getBox(2, 0), board.getBox(2, 3), player1, queen)

        val result = recognizer.isFork(board, move, player1)

        assertNull(result)
    }

    // ── Skewer: tactical-significance rules ────────────────────────────────────

    @Test
    fun isSkewer_backPieceDefended_returnsNull() {
        // Queen "skewers" the king against a DEFENDED knight: once the king moves,
        // taking the knight loses 9-for-3 — the skewer wins nothing.
        val queen = Queen(color = Color.WHITE, position = Position(1, 7), isKilled = false)
        board.getBox(1, 7).chessPiece = queen

        val king = King(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        board.getBox(3, 3).chessPiece = king

        val knight = Knight(color = Color.BLACK, position = Position(5, 5), isKilled = false)
        board.getBox(5, 5).chessPiece = knight
        // Defends the knight from off the skewer diagonal.
        val knightDefender = Pawn(color = Color.BLACK, position = Position(4, 6), isKilled = false)
        board.getBox(4, 6).chessPiece = knightDefender

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(1, 7),
            to = board.getBox(1, 1),
            player = player1,
            pieceMoved = queen
        )

        val result = recognizer.isSkewer(board, move)

        assertNull(result)
    }

    // ── Discovered Attack: tactical-significance rules ─────────────────────────

    @Test
    fun isDiscoveredAttack_revealedAttackOnDefendedLesserPiece_returnsNull() {
        // Knight move reveals the rook's attack on a DEFENDED knight — capturing it
        // loses 5-for-3, so nothing is actually threatened.
        val rook = Rook(color = Color.WHITE, position = Position(0, 0), isKilled = false)
        board.getBox(0, 0).chessPiece = rook

        val blocker = Knight(color = Color.WHITE, position = Position(2, 0), isKilled = false)
        board.getBox(2, 0).chessPiece = blocker

        val targetKnight = Knight(color = Color.BLACK, position = Position(5, 0), isKilled = false)
        board.getBox(5, 0).chessPiece = targetKnight
        // Black pawn on (4,1) captures toward (5,0) — defends the knight.
        val targetDefender = Pawn(color = Color.BLACK, position = Position(4, 1), isKilled = false)
        board.getBox(4, 1).chessPiece = targetDefender

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(2, 0),
            to = board.getBox(3, 2),
            player = player1,
            pieceMoved = blocker
        )

        val result = recognizer.isDiscoveredAttack(board, move, player1)

        assertNull(result)
    }

    @Test
    fun doubleCheck_alsoFiresDiscoveredCheck_subsumedDownstream() {
        // A double check is by construction also a discovered check (the revealed
        // attacker is a genuine new one). Both detectors fire on the same board —
        // GamePlay.analyzeSingleMove drops "Discovered Check" whenever "Double Check"
        // is present, so the insight message announces the stronger tactic.
        val bishop = Bishop(color = Color.WHITE, position = Position(3, 1), isKilled = false)
        board.getBox(3, 1).chessPiece = bishop

        val knight = Knight(color = Color.WHITE, position = Position(1, 3), isKilled = false)
        board.getBox(1, 3).chessPiece = knight

        val blackKing = King(color = Color.BLACK, position = Position(0, 4), isKilled = false)
        board.getBox(0, 4).chessPiece = blackKing

        game.currentTurn.value = player1

        val move = Move(
            from = board.getBox(1, 3),
            to = board.getBox(2, 5),
            player = player1,
            pieceMoved = knight
        )

        assertEquals("Double Check", recognizer.isDoubleCheck(board, move, player1)?.name)
        assertEquals("Discovered Check", recognizer.isDiscoveredCheck(board, move, player1)?.name)
    }

    private fun Board.clear() {
        for (row in 0..7) {
            for (col in 0..7) {
                getBox(row, col).chessPiece = null
            }
        }
    }

}
