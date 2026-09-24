package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.repository.FenConverter
import com.example.chess.data.repository.GamePlay
import com.example.chess.domain.model.GamePhase
import com.example.chess.domain.model.InsightType
import com.example.chess.domain.model.MoveInsight
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class PuzzleExtractionTest {

    private lateinit var game: GamePlay

    @Before
    fun setup() {
        game = testGamePlay()
    }

    private fun insight(move: String, category: String, bestMove: String) = MoveInsight(
        move = move,
        category = category,
        evalBefore = 0,
        evalAfter = 0,
        evalLost = 0,
        bestMove = bestMove,
        bestContinuation = emptyList(),
        playerMoveRank = 1,
        insight = InsightType.POSITIONAL_ERROR,
        insightDetail = "",
        isCriticalMoment = false,
        gamePhase = GamePhase.OPENING,
    )

    @Test
    fun extractPuzzleCandidates_onlyBlunderAndMistakeAreExtracted() {
        val movesPlayed = listOf("e2e4", "e7e5", "b1c3")
        val insights = listOf(
            insight("e2e4", "Best", "e2e4"),
            insight("e7e5", "Great", "e7e5"),
            insight("b1c3", "Blunder", "g1f3"),
        )

        val candidates = game.extractPuzzleCandidates(movesPlayed, insights)

        assertEquals(1, candidates.size)
        assertEquals("Blunder", candidates[0].category)
    }

    @Test
    fun extractPuzzleCandidates_noBlunderOrMistake_returnsEmpty() {
        val movesPlayed = listOf("e2e4", "e7e5")
        val insights = listOf(
            insight("e2e4", "Best", "e2e4"),
            insight("e7e5", "Great", "e7e5"),
        )

        assertTrue(game.extractPuzzleCandidates(movesPlayed, insights).isEmpty())
    }

    @Test
    fun extractPuzzleCandidates_capturesCorrectAndPlayedMoves() {
        val movesPlayed = listOf("e2e4", "e7e5", "b1c3")
        val insights = listOf(
            insight("e2e4", "Best", "e2e4"),
            insight("e7e5", "Great", "e7e5"),
            insight("b1c3", "Mistake", "g1f3"),
        )

        val candidate = game.extractPuzzleCandidates(movesPlayed, insights).single()

        assertEquals("g1f3", candidate.correctMoveUci)
        assertEquals("b1c3", candidate.playedMoveUci)
    }

    // The flagged move is White's 2nd move (ply index 2, 0-based) — the puzzle's FEN
    // must reflect the position right BEFORE that move, i.e. right after 1.e4 e5, with
    // the knight still on b1 and White to move.
    @Test
    fun extractPuzzleCandidates_fenReflectsPositionBeforeTheFlaggedMove() {
        val movesPlayed = listOf("e2e4", "e7e5", "b1c3")
        val insights = listOf(
            insight("e2e4", "Best", "e2e4"),
            insight("e7e5", "Great", "e7e5"),
            insight("b1c3", "Blunder", "g1f3"),
        )

        val candidate = game.extractPuzzleCandidates(movesPlayed, insights).single()
        val (board, activeColor) = FenConverter.fromFen(candidate.fen)

        assertEquals(Color.WHITE, activeColor)
        assertTrue("Knight must still be on b1 — the blunder hasn't been applied",
            board.getBox(7, 1).chessPiece is Knight)
        assertTrue(board.getBox(7, 4).chessPiece is King)
        // 1.e4 e5 is a real two-square black pawn push right before this position, so
        // the en passant target (e6) must round-trip through the puzzle's FEN too.
        assertEquals(com.example.chess.data.model.Position(2, 4), board.enPassantTarget)
    }
}
