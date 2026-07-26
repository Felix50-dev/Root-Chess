package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Player
import com.example.chess.data.model.PlayerType
import com.example.chess.data.model.Position
import com.example.chess.data.model.Rook
import com.example.chess.data.repository.GamePlay
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class AnalyzeMoveScoresTest {

    private lateinit var game: GamePlay
    private lateinit var player1: Player
    private lateinit var player2: Player

    @Before
    fun setup() {
        val board = Board()
        game = GamePlay(board, StockfishEngine())
        player1 = Player(isWhite = true, PlayerType.HUMAN)
        player2 = Player(isWhite = false, PlayerType.HUMAN)
        game.init(player1, player2, true)
    }

    @Test
    fun calculateLoss_perfectMove_returnsZeroOrNegative() {
        // When player plays the best move, loss should be <= 0
        // scoreBestForPlayer = 50, scoreAfterFromOpponent = -50 (same position from opponent view)
        val loss = game.calculateLoss(
            scoreBestForPlayer      = 50,
            scoreAfterFromOpponent  = -50,
            isWhiteMove             = true
        )
        assertTrue("Perfect move should have loss <= 0", loss <= 0)
    }

    @Test
    fun calculateLoss_blackMove_calculatedFromBlackPerspective() {
        // Black plays a good move — loss should be small
        val loss = game.calculateLoss(
            scoreBestForPlayer      = -30,  // best for black
            scoreAfterFromOpponent  = 30,   // white's eval after black's good move
            isWhiteMove             = false
        )
        assertTrue("Good black move should have low loss", loss <= 50)
    }

    @Test
    fun calculateLoss_whiteMove_correctLoss() {
        // Best for white = +100cp, after move white is at +80cp → loss = 20
        val loss = game.calculateLoss(
            scoreBestForPlayer     = 100,
            scoreAfterFromOpponent = -80,
            isWhiteMove            = true
        )
        assertEquals(20, loss)
    }

    @Test
    fun calculateLoss_blackMove_correctLoss() {
        // Best for black = -100cp (from white's view), after move position is -80cp → loss = 20
        val loss = game.calculateLoss(
            scoreBestForPlayer     = 100,   // Stockfish always returns score for current player
            scoreAfterFromOpponent = -80,   // opponent (white) eval after black's good move
            isWhiteMove            = false
        )
        assertEquals(20, loss)
    }

    // ── Category mapping ──────────────────────────────────────────────────────

    @Test
    fun categorizeByLoss_loss0_returnsBest() =
        assertEquals("Best", game.categorizeByLoss(0))

    @Test
    fun categorizeByLoss_loss10_returnsBest() =
        assertEquals("Best", game.categorizeByLoss(10))

    @Test
    fun categorizeByLoss_loss11_returnsGreat() =
        assertEquals("Great", game.categorizeByLoss(11))

    @Test
    fun categorizeByLoss_loss50_returnsGreat() =
        assertEquals("Great", game.categorizeByLoss(50))

    @Test
    fun categorizeByLoss_loss51_returnsInaccurate() =
        assertEquals("Inaccurate", game.categorizeByLoss(51))

    @Test
    fun categorizeByLoss_loss100_returnsInaccurate() =
        assertEquals("Inaccurate", game.categorizeByLoss(100))

    @Test
    fun categorizeByLoss_loss101_returnsMistake() =
        assertEquals("Mistake", game.categorizeByLoss(101))

    @Test
    fun categorizeByLoss_loss300_returnsMistake() =
        assertEquals("Mistake", game.categorizeByLoss(300))

    @Test
    fun categorizeByLoss_loss301_returnsBlunder() =
        assertEquals("Blunder", game.categorizeByLoss(301))

    @Test
    fun categorizeByLoss_largeValue_returnsBlunder() =
        assertEquals("Blunder", game.categorizeByLoss(9999))

    @Test
    fun categorizeByLoss_negativeLoss_returnsBest() =
        // Negative loss = player played better than recommended (opponent blundered)
        assertEquals("Best", game.categorizeByLoss(-50))

    // ── Boundary values ───────────────────────────────────────────────────────

    @Test
    fun categorizeByLoss_boundaryValues_allCorrect() {
        val expected = mapOf(
            -1  to "Best",
             0  to "Best",
             10 to "Best",
             11 to "Great",
             50 to "Great",
             51 to "Inaccurate",
            100 to "Inaccurate",
            101 to "Mistake",
            300 to "Mistake",
            301 to "Blunder",
            500 to "Blunder",
        )
        expected.forEach { (loss, category) ->
            assertEquals(
                "Loss $loss should be $category",
                category,
                game.categorizeByLoss(loss)
            )
        }
    }

    // ── Missed mate detection ─────────────────────────────────────────────────

    @Test
    fun isMissedMate_mateIn1Available_playerMissed_returnsTrue() =
        assertTrue(game.isMissedMate(
            mateInBefore     = 1,
            mateInAfter      = 0,
            playerMove       = "e2e4",
            recommendedMove  = "d1h5"
        ))

    @Test
    fun isMissedMate_mateIn3Available_playerMissed_returnsTrue() =
        assertTrue(game.isMissedMate(
            mateInBefore     = 3,
            mateInAfter      = 0,
            playerMove       = "e2e4",
            recommendedMove  = "d1h5"
        ))

    @Test
    fun isMissedMate_playerFoundMate_returnsFalse() =
        // Player played the mating move
        assertFalse(game.isMissedMate(
            mateInBefore     = 1,
            mateInAfter      = 0,
            playerMove       = "d1h5",   // same as recommended
            recommendedMove  = "d1h5"
        ))

    @Test
    fun isMissedMate_mateStillAvailable_returnsFalse() =
        // mateInAfter != 0 means mate still exists after player's move
        assertFalse(game.isMissedMate(
            mateInBefore     = 2,
            mateInAfter      = 1,   // mate still available
            playerMove       = "e2e4",
            recommendedMove  = "d1h5"
        ))

    @Test
    fun isMissedMate_mateIn4_outsideWindow_returnsFalse() =
        // Only flags mate in 1-3, not deeper
        assertFalse(game.isMissedMate(
            mateInBefore     = 4,
            mateInAfter      = 0,
            playerMove       = "e2e4",
            recommendedMove  = "d1h5"
        ))

    @Test
    fun isMissedMate_noMateAvailable_returnsFalse() =
        assertFalse(game.isMissedMate(
            mateInBefore     = 0,
            mateInAfter      = 0,
            playerMove       = "e2e4",
            recommendedMove  = "d1h5"
        ))

    // ── Miss (capture) detection ──────────────────────────────────────────────

    @Test
    fun isMissedCapture_freePieceAvailable_playerIgnored_returnsTrue() {
        val blackRook = Rook(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        assertTrue(game.isMissedCapture(
            capturedPiece       = blackRook,
            playerColor         = Color.WHITE,
            captureIsProfitable = true,
            playerMove          = "e2e4",
            recommendedMove     = "d1d3"   // recommended was to capture
        ))
    }

    @Test
    fun isMissedCapture_playerCaptured_returnsFalse() {
        val blackRook = Rook(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        assertFalse(game.isMissedCapture(
            capturedPiece       = blackRook,
            playerColor         = Color.WHITE,
            captureIsProfitable = true,
            playerMove          = "d1d3",   // player DID capture
            recommendedMove     = "d1d3"
        ))
    }

    @Test
    fun isMissedCapture_captureNotProfitable_returnsFalse() {
        val blackPawn = Pawn(color = Color.BLACK, position = Position(3, 3), isKilled = false)
        assertFalse(game.isMissedCapture(
            capturedPiece       = blackPawn,
            playerColor         = Color.WHITE,
            captureIsProfitable = false,   // defended piece, not worth taking
            playerMove          = "e2e4",
            recommendedMove     = "d1d3"
        ))
    }

    @Test
    fun isMissedCapture_noCaptureAvailable_returnsFalse() =
        assertFalse(game.isMissedCapture(
            capturedPiece       = null,    // no piece to capture
            playerColor         = Color.WHITE,
            captureIsProfitable = false,
            playerMove          = "e2e4",
            recommendedMove     = "d2d4"
        ))

    @Test
    fun isMissedCapture_ownPieceOnSquare_returnsFalse() {
        // Can't "miss" capturing your own piece
        val whiteRook = Rook(color = Color.WHITE, position = Position(3, 3), isKilled = false)
        assertFalse(game.isMissedCapture(
            capturedPiece       = whiteRook,
            playerColor         = Color.WHITE,  // same color
            captureIsProfitable = true,
            playerMove          = "e2e4",
            recommendedMove     = "d1d3"
        ))
    }


    // ── Accuracy — already exists but worth confirming edge cases ────────────

    @Test
    fun calculateAccuracy_allPerfectMoves_returns100() {
        val losses = listOf(0, 0, 0, 0, 0)
        val accuracy = game.calculateAccuracy(losses, 100.0)
        assertEquals(100.0, accuracy, 0.1)
    }

    @Test
    fun calculateAccuracy_allBlunders_returnsLow() {
        val losses = listOf(500, 500, 500, 500)
        val accuracy = game.calculateAccuracy(losses, 100.0)
        assertTrue("All blunders should give accuracy < 20", accuracy < 20.0)
    }

    @Test
    fun calculateAccuracy_emptyList_returns0() {
        val accuracy = game.calculateAccuracy(emptyList(), 100.0)
        assertEquals(0.0, accuracy, 0.001)
    }

    @Test
    fun calculateAccuracy_mixedMoves_betweenBounds() {
        val losses = listOf(0, 50, 200, 10, 400)
        val accuracy = game.calculateAccuracy(losses, 100.0)
        assertTrue("Mixed accuracy should be between 0 and 100", accuracy in 0.0..100.0)
    }

    @Test
    fun calculateAccuracy_negativeLoss_clampedToZero() {
        // Negative loss (lucky move) should not push accuracy above 100
        val losses = listOf(-500, -200, -100)
        val accuracy = game.calculateAccuracy(losses, 100.0)
        assertTrue("Negative losses should not exceed 100%", accuracy <= 100.0)
    }

    @Test
    fun buildAnalysisResult_countsSplitByColor() {
        val results = listOf(
            fakeMoveResult("Blunder", isWhite = true),
            fakeMoveResult("Best",    isWhite = false),
            fakeMoveResult("Mistake", isWhite = true),
            fakeMoveResult("Best",    isWhite = false),
        )
        val result = game.buildAnalysisResult(results)

        assertEquals(1, result.whiteCategoryCounts["Blunder"])
        assertEquals(1, result.whiteCategoryCounts["Mistake"])
        assertEquals(2, result.blackCategoryCounts["Best"])
    }

    @Test
    fun buildAnalysisResult_accuracyReflectsLosses() {
        val results = listOf(
            fakeMoveResult("Best", isWhite = true,  loss = 0),
            fakeMoveResult("Best", isWhite = false, loss = 0),
        )
        val result = game.buildAnalysisResult(results)
        assertEquals(100.0, result.whiteAccuracy, 0.1)
        assertEquals(100.0, result.blackAccuracy, 0.1)
    }

    @Test
    fun buildAnalysisResult_moveCategories_preservesOrder() {
        val categories = listOf("Best", "Blunder", "Mistake", "Great")
        val results = categories.mapIndexed { i, cat ->
            fakeMoveResult(cat, isWhite = i % 2 == 0)
        }
        assertEquals(categories, game.buildAnalysisResult(results).moveCategories)
    }

    // Helper
    private fun fakeMoveResult(
        category: String,
        isWhite: Boolean,
        loss: Int = 0,
    ) = GamePlay.SingleMoveResult(
        category = category,
        loss = loss,
        evalAfterFromWhite = 0,
        bestMove = "e2e4",
        captureOpportunity = null,
        missedMate = null,
        tactics = emptyList(),
        isWhiteMove = isWhite,
        nonPawnMaterial = 62,
    )
}


