package com.example.chess

import android.content.Context
import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.repository.GamePlay
import com.example.chess.data.repository.OpeningBookRepository
import com.example.chess.domain.model.PositionEval
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

// JVM tests for the null-move threat probe's selection logic and its no-engine guard
// paths. GamePlay is constructed WITHOUT init() (same pattern as
// GamePlayExplanationFactsTest): the probe must work on isolated replay boards, and its
// in-check / dead-engine paths must never require a live engine process.
class GamePlayThreatProbeTest {

    private lateinit var game: GamePlay

    @Before
    fun setup() {
        game = GamePlay(Board(), StockfishEngine(), OpeningBookRepository(Mockito.mock(Context::class.java)))
    }

    private fun eval(
        score: Int,
        bestMove: String,
        mateIn: Int = 0,
        secondBestScore: Int? = null,
        secondBestMove: String? = null,
        secondBestMateIn: Int? = null,
    ) = PositionEval(
        score, bestMove, mateIn, emptyList(), 12,
        secondBestScore, secondBestMove, secondBestMateIn,
    )

    @Test
    fun selectMeaningfulThreats_filtersSubThresholdAndKeepsGainers() {
        // PV1 gains 300cp over an even standing eval → kept; PV2 gains only 40 → dropped.
        val threats = game.selectMeaningfulThreats(
            eval(300, "g5f7", secondBestScore = 40, secondBestMove = "d8h4"),
            evalBeforeCp = 0,
        )
        assertEquals(1, threats.size)
        assertEquals("g5f7", threats[0].uci)
        assertEquals(300, threats[0].gainCp)
        assertEquals(null, threats[0].mateIn)
    }

    @Test
    fun selectMeaningfulThreats_mateIncludedRegardlessOfScore() {
        val threats = game.selectMeaningfulThreats(
            eval(0, "h4f2", mateIn = 2),
            evalBeforeCp = 0,
        )
        assertEquals(1, threats.size)
        assertEquals(2, threats[0].mateIn)
    }

    @Test
    fun selectMeaningfulThreats_secondPvMateIncludedRegardlessOfScore() {
        // PV1 doesn't qualify on its own (score 0, no mate) but is still emitted since the
        // threshold check is per-line; PV2 is mate-in-3 with a low synthesized-adjacent
        // score (0) — must still be included, proving the threshold exemption applies to
        // the second line the same way it does the first.
        val threats = game.selectMeaningfulThreats(
            eval(0, "h4f2", secondBestScore = 0, secondBestMove = "d8h4", secondBestMateIn = 3),
            evalBeforeCp = 0,
        )
        assertEquals(1, threats.size)
        assertEquals("d8h4", threats[0].uci)
        assertEquals(3, threats[0].mateIn)
    }

    @Test
    fun selectMeaningfulThreats_secondPvNonPositiveMateInNotExempt() {
        // secondBestMateIn <= 0 (the second line's side-to-move is the one getting mated,
        // or a stray non-positive value) must NOT be treated as a threat exemption — only a
        // genuinely positive mate-in-N counts as the opponent threatening mate.
        val threats = game.selectMeaningfulThreats(
            eval(300, "g5f7", secondBestScore = 10, secondBestMove = "d8h4", secondBestMateIn = 0),
            evalBeforeCp = 0,
        )
        assertEquals(1, threats.size)
        assertEquals("g5f7", threats[0].uci)
    }

    @Test
    fun selectMeaningfulThreats_signConvention_negativeStandingEval() {
        // Mover already -200: opponent's standing eval is +200, so a probe score of 300
        // only gains 100 → below threshold, not a threat.
        assertTrue(game.selectMeaningfulThreats(eval(300, "g5f7"), evalBeforeCp = -200).isEmpty())
        // 400 gains 200 → threat.
        val threats = game.selectMeaningfulThreats(eval(400, "g5f7"), evalBeforeCp = -200)
        assertEquals(1, threats.size)
        assertEquals(200, threats[0].gainCp)
    }

    @Test
    fun selectMeaningfulThreats_secondPvNeedsAScore() {
        // PV2 move without a captured score can't be judged → dropped even if PV1 is kept.
        val threats = game.selectMeaningfulThreats(
            eval(300, "g5f7", secondBestScore = null, secondBestMove = "d8h4"),
            evalBeforeCp = 0,
        )
        assertEquals(1, threats.size)
    }

    @Test
    fun selectMeaningfulThreats_invalidBestMoveYieldsNothing() {
        assertTrue(game.selectMeaningfulThreats(eval(500, "(none)"), evalBeforeCp = 0).isEmpty())
        assertTrue(game.selectMeaningfulThreats(eval(500, ""), evalBeforeCp = 0).isEmpty())
    }

    @Test
    fun threatProbeFacts_inCheckShortCircuitsWithoutEngine() = runTest {
        // 1.e4 f5 2.Qh5+ g6 — at index 3 Black is in check before moving; the probe must
        // return the in-check fact without ever touching the (never-started) engine.
        val moves = listOf("e2e4", "f7f5", "d1h5", "g7g6")
        val facts = game.threatProbeFacts(
            moves, index = 3, evalBeforeCp = -100, bestMoveUci = "g7g6", opponentReplyUci = null,
        )
        assertEquals(1, facts.size)
        assertTrue(facts[0].startsWith("Black was in check before this move"))
    }

    @Test
    fun threatProbeFacts_deadEngineReturnsEmpty() = runTest {
        // Engine never started: the probe search yields the empty PositionEval, whose
        // blank bestMove must be treated as "no data", not a threat.
        val facts = game.threatProbeFacts(
            listOf("e2e4", "e7e5"), index = 1, evalBeforeCp = 0,
            bestMoveUci = "b8c6", opponentReplyUci = null,
        )
        assertTrue(facts.isEmpty())
    }

    @Test
    fun threatProbeFacts_outOfRangeIndexReturnsEmpty() = runTest {
        assertTrue(
            game.threatProbeFacts(
                listOf("e2e4"), index = 5, evalBeforeCp = 0,
                bestMoveUci = null, opponentReplyUci = null,
            ).isEmpty()
        )
    }
}
