package com.example.chess

import com.example.chess.data.StockfishEngine
import com.example.chess.data.model.Board
import com.example.chess.data.repository.GamePlay
import com.example.chess.domain.model.InsightType
import com.example.chess.domain.model.PositionEval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Before
import org.junit.Test

// A tactic pattern (Pin/Fork/etc.) only proves the tactic-executing piece is safe on
// its own landing square — not that exploiting it later is actually sound (e.g.
// capturing a "pinned" piece that's defended elsewhere). generateInsight must defer to
// the engine's own loss measurement instead of unconditionally praising any matched
// tactic. See TacticsRecognizerTest.isPin_pinnedPieceDefendedElsewhere_pinStandsButCaptureIsUnsafe
// for the board-level scenario this guards against.
class GenerateInsightTest {

    private lateinit var game: GamePlay

    @Before
    fun setup() {
        game = GamePlay(Board(), StockfishEngine())
    }

    private fun eval(score: Int) = PositionEval(
        score = score,
        bestMove = "e2e4",
        mateIn = 0,
        principalVariation = emptyList(),
        depth = 10,
    )

    @Test
    fun tacticFound_butMoveActuallyBlundersMaterial_notLabeledBrilliant() {
        val insight = game.generateInsight(
            moveIndex = 0,
            playerMove = "c1c4",
            bestMove = "c1c4",
            evalBefore = eval(0),
            evalAfter = eval(350),   // strongly good for the opponent post-move
            tactics = listOf("Pin"),
            totalMoves = 20,
        )

        assertNotEquals(InsightType.BRILLIANT_TACTIC, insight.insight)
    }

    @Test
    fun tacticFound_andMoveIsSound_labeledBrilliant() {
        val insight = game.generateInsight(
            moveIndex = 0,
            playerMove = "c1c4",
            bestMove = "c1c4",
            evalBefore = eval(0),
            evalAfter = eval(0),   // no material lost
            tactics = listOf("Pin"),
            totalMoves = 20,
        )

        assertEquals(InsightType.BRILLIANT_TACTIC, insight.insight)
    }
}
