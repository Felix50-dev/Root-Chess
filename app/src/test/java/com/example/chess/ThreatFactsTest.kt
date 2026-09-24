package com.example.chess

import com.example.chess.data.repository.ThreatFacts
import com.example.chess.domain.model.PositionEval
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

// JVM tests for the pure threat-fact templates and verdict predicates used by the
// null-move threat probe (GamePlay.threatProbeFacts). Exact-string assertions on the
// templates: the LLM prompt rules key off the "Opponent threat" prefix.
class ThreatFactsTest {

    private fun eval(
        score: Int,
        bestMove: String,
        mateIn: Int = 0,
    ) = PositionEval(score, bestMove, mateIn, emptyList(), 12)

    @Test
    fun describeThreat_captureTemplate() {
        val fact = ThreatFacts.describeThreat(
            opponentColorWord = "Black",
            pieceType = "Knight",
            from = "g5",
            to = "f7",
            capturedColorWord = "White",
            capturedType = "Pawn",
            capturedSquare = "f7",
            mateIn = null,
        )
        assertEquals(
            "Opponent threat: if it were Black's turn, the Black Knight on g5 " +
                "could capture the White Pawn on f7.",
            fact,
        )
    }

    @Test
    fun describeThreat_quietTemplate() {
        val fact = ThreatFacts.describeThreat(
            opponentColorWord = "Black",
            pieceType = "Queen",
            from = "d8",
            to = "h4",
            capturedColorWord = null,
            capturedType = null,
            capturedSquare = null,
            mateIn = null,
        )
        assertEquals(
            "Opponent threat: if it were Black's turn, the Black Queen on d8 " +
                "could play to h4 with a strong threat.",
            fact,
        )
    }

    @Test
    fun describeThreat_mateTemplateWinsOverCapture() {
        val fact = ThreatFacts.describeThreat(
            opponentColorWord = "Black",
            pieceType = "Queen",
            from = "h4",
            to = "f2",
            capturedColorWord = "White",
            capturedType = "Pawn",
            capturedSquare = "f2",
            mateIn = 2,
        )
        assertEquals(
            "Opponent threat: Black is threatening a forced checkmate in 2, " +
                "starting with the Black Queen on h4 to f2.",
            fact,
        )
    }

    @Test
    fun inCheckFact_namesTheMover() {
        assertEquals(
            "White was in check before this move — escaping the check was itself " +
                "the immediate problem to solve.",
            ThreatFacts.inCheckFact("White"),
        )
    }

    @Test
    fun matchesThreat_toleratesPromotionSuffix() {
        assertTrue(ThreatFacts.matchesThreat("e7e8", "e7e8q"))
        assertTrue(ThreatFacts.matchesThreat("e7e8q", "e7e8"))
        assertTrue(ThreatFacts.matchesThreat("g5f7", "g5f7"))
        assertFalse(ThreatFacts.matchesThreat("g5f7", "g5e6"))
        assertFalse(ThreatFacts.matchesThreat("g5f7", null))
    }

    @Test
    fun isParriedByAlternative_truthTable() {
        // Reply differs from the threat → parried regardless of score.
        assertTrue(ThreatFacts.isParriedByAlternative("g5f7", eval(400, "d8h4"), 0))
        // Reply IS the threat and still gains ≥ threshold → NOT parried.
        assertFalse(ThreatFacts.isParriedByAlternative("g5f7", eval(300, "g5f7"), 0))
        // Reply is the threat but no longer gains enough → parried.
        assertTrue(ThreatFacts.isParriedByAlternative("g5f7", eval(40, "g5f7"), 0))
        // Mover was already worse (negative standing eval): the same raw reply score
        // clears the threshold more easily. -200 standing: 300 + (-200) = 100 < 150.
        assertTrue(ThreatFacts.isParriedByAlternative("g5f7", eval(300, "g5f7"), -200))
        // Mate reply that is the threat → never parried, whatever the cp says.
        assertFalse(ThreatFacts.isParriedByAlternative("g5f7", eval(0, "g5f7", mateIn = 2), 0))
    }
}
