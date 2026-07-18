package com.example.chess

import com.example.chess.data.StockfishEngine
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Before
import org.junit.Test

class StockfishParserTest {

    private lateinit var engine: StockfishEngine

    @Before
    fun setup() {
        engine = StockfishEngine()
    }

    // ── Best move ─────────────────────────────────────────────────────────────

    @Test
    fun parse_bestMove_extractedCorrectly() {
        val lines = listOf(
            "info depth 10 score cp 42 pv e2e4 e7e5 g1f3",
            "bestmove e2e4 ponder e7e5"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals("e2e4", result.bestMove)
    }

    @Test
    fun parse_bestMove_withNoPonder_extractedCorrectly() {
        val lines = listOf(
            "info depth 8 score cp 15 pv d2d4",
            "bestmove d2d4"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals("d2d4", result.bestMove)
    }

    // ── Score ─────────────────────────────────────────────────────────────────

    @Test
    fun parse_positiveScore_parsedCorrectly() {
        val lines = listOf(
            "info depth 12 score cp 85 pv e2e4",
            "bestmove e2e4"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(85, result.score)
    }

    @Test
    fun parse_negativeScore_parsedCorrectly() {
        val lines = listOf(
            "info depth 12 score cp -120 pv e7e5",
            "bestmove e7e5"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(-120, result.score)
    }

    @Test
    fun parse_multipleDepthLines_takesLastScore() {
        // Stockfish outputs multiple depth lines — should use the last one
        val lines = listOf(
            "info depth 5  score cp 10 pv e2e4",
            "info depth 10 score cp 42 pv e2e4 e7e5",
            "info depth 15 score cp 78 pv e2e4 e7e5 g1f3",
            "bestmove e2e4"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(78, result.score)
    }

    // ── Mate detection ────────────────────────────────────────────────────────

    @Test
    fun parse_mateIn1_parsedCorrectly() {
        val lines = listOf(
            "info depth 3 score mate 1 pv d1h5",
            "bestmove d1h5"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(1, result.mateIn)
    }

    @Test
    fun parse_mateIn3_parsedCorrectly() {
        val lines = listOf(
            "info depth 7 score mate 3 pv d1h5 g8f6 h5f7",
            "bestmove d1h5"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(3, result.mateIn)
    }

    @Test
    fun parse_beingMated_negativeMateParsed() {
        val lines = listOf(
            "info depth 5 score mate -2 pv e1d1",
            "bestmove e1d1"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(-2, result.mateIn)
    }

    @Test
    fun parse_noMate_mateInIsZero() {
        val lines = listOf(
            "info depth 10 score cp 42 pv e2e4",
            "bestmove e2e4"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(0, result.mateIn)
    }

    // ── Principal variation ───────────────────────────────────────────────────

    @Test
    fun parse_pvWithFourMoves_allExtracted() {
        val lines = listOf(
            "info depth 18 score cp 42 pv e2e4 e7e5 g1f3 b8c6",
            "bestmove e2e4"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(listOf("e2e4", "e7e5", "g1f3", "b8c6"), result.principalVariation)
    }

    @Test
    fun parse_pvWithMoreThanFourMoves_cappedAtFour() {
        val lines = listOf(
            "info depth 20 score cp 35 pv e2e4 e7e5 g1f3 b8c6 f1b5 a7a6",
            "bestmove e2e4"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(4, result.principalVariation.size)
        assertEquals(listOf("e2e4", "e7e5", "g1f3", "b8c6"), result.principalVariation)
    }

    @Test
    fun parse_pvWithSingleMove_returnsOneMove() {
        val lines = listOf(
            "info depth 5 score cp 100 pv d1h5",
            "bestmove d1h5"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(listOf("d1h5"), result.principalVariation)
    }

    @Test
    fun parse_pvContainsJunkTokens_filteredOut() {
        // Simulates the "1re cp 49" problem — garbage tokens after pv
        val lines = listOf(
            "info depth 10 score cp 49 pv e2e4 1re cp 49 e7e5",
            "bestmove e2e4"
        )
        val result = engine.parseStockfishOutput(lines)
        // Only valid UCI moves should remain
        assertEquals(listOf("e2e4", "e7e5"), result.principalVariation)
    }

    @Test
    fun parse_pvWithPromotion_promotionMoveIncluded() {
        // Promotion: e7e8q — 5 chars, valid UCI
        val lines = listOf(
            "info depth 15 score cp 800 pv e7e8q d8e8",
            "bestmove e7e8q"
        )
        val result = engine.parseStockfishOutput(lines)
        assertTrue("Promotion move should be in PV",
            result.principalVariation.contains("e7e8q"))
    }

    @Test
    fun parse_emptyPv_returnsEmptyList() {
        val lines = listOf(
            "info depth 1 score cp 0",   // no pv token at all
            "bestmove e2e4"
        )
        val result = engine.parseStockfishOutput(lines)
        assertTrue("PV should be empty when not present",
            result.principalVariation.isEmpty())
    }

    @Test
    fun parse_pvUpdatesAcrossDepths_lastDepthWins() {
        // Each deeper search overwrites PV — final result should be deepest
        val lines = listOf(
            "info depth 5  score cp 10 pv e2e4",
            "info depth 10 score cp 30 pv d2d4 d7d5",
            "info depth 15 score cp 55 pv c2c4 e7e5 g1f3",
            "bestmove c2c4"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(listOf("c2c4", "e7e5", "g1f3"), result.principalVariation)
    }

    // ── Depth ─────────────────────────────────────────────────────────────────

    @Test
    fun parse_depth_extractedCorrectly() {
        val lines = listOf(
            "info depth 18 score cp 42 pv e2e4",
            "bestmove e2e4"
        )
        val result = engine.parseStockfishOutput(lines)
        assertEquals(18, result.depth)
    }

    // ── Edge cases ────────────────────────────────────────────────────────────

    @Test
    fun parse_emptyLines_returnsDefaultEval() {
        val result = engine.parseStockfishOutput(emptyList())
        assertEquals(0, result.score)
        assertEquals(0, result.mateIn)
        assertEquals("", result.bestMove)
        assertTrue(result.principalVariation.isEmpty())
    }

    @Test
    fun parse_noInfoLine_onlyBestMove_stillExtractsBestMove() {
        val lines = listOf("bestmove g1f3")
        val result = engine.parseStockfishOutput(lines)
        assertEquals("g1f3", result.bestMove)
        assertEquals(0, result.score)
    }

    @Test
    fun parse_castlingInPv_includedCorrectly() {
        // Castling: e1g1 — valid UCI move format
        val lines = listOf(
            "info depth 12 score cp 20 pv e1g1 e8g8",
            "bestmove e1g1"
        )
        val result = engine.parseStockfishOutput(lines)
        assertTrue("Castling should be in PV",
            result.principalVariation.contains("e1g1"))
    }
}
