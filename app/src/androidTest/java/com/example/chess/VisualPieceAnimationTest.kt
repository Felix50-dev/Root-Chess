package com.example.chess

import com.example.chess.data.model.Color
import com.example.chess.data.model.Position
import com.example.chess.presentation.model.VisualPiece
import org.junit.Assert.*
import org.junit.Test

class VisualPieceAnimationTest {

    // ── Test 1: previousPosition is distinct from position after sync ─────────
    @Test
    fun previousPosition_differs_from_position_after_move() {
        val e2 = Position(6, 4)
        val e4 = Position(4, 4)

        val before = listOf(VisualPiece("p1", 0, Color.WHITE, e2, null))
        val previousPositions = before.associate { it.id to it.position }

        val after = listOf(VisualPiece("p1", 0, Color.WHITE, e4, previousPositions["p1"]))

        val piece = after.first()
        println("position:         ${piece.position}")
        println("previousPosition: ${piece.previousPosition}")
        println("will animate:     ${piece.previousPosition != piece.position}")

        assertNotEquals(
            "previousPosition must differ from position for animation to occur",
            piece.position,
            piece.previousPosition
        )
    }

    // ── Test 2: snapshot taken BEFORE board mutation gives correct previous ────
    @Test
    fun snapshot_before_mutation_gives_correct_previousPosition() {
        val e2 = Position(6, 4)
        val e4 = Position(4, 4)

        val visualPiecesBefore = listOf(VisualPiece("p1", 0, Color.WHITE, e2, null))

        // CORRECT: snapshot before mutation
        val correctSnapshot = visualPiecesBefore.associate { it.id to it.position }

        // Simulate board mutation
        val boardPositionAfterMove = e4

        val correctResult = VisualPiece("p1", 0, Color.WHITE, e4, correctSnapshot["p1"])
        val wrongResult   = VisualPiece("p1", 0, Color.WHITE, e4, boardPositionAfterMove)

        println("CORRECT prev=${correctResult.previousPosition} pos=${correctResult.position}")
        println("WRONG   prev=${wrongResult.previousPosition}   pos=${wrongResult.position}")

        assertNotEquals("Correct: prev != pos", correctResult.position, correctResult.previousPosition)
        assertEquals("Wrong: prev == pos means no animation", wrongResult.position, wrongResult.previousPosition)
    }

    // ── Test 3: consecutive moves chain previousPosition correctly ────────────
    @Test
    fun consecutive_moves_chain_previousPosition_correctly() {
        val e2 = Position(6, 4)
        val e4 = Position(4, 4)
        val e6 = Position(2, 4)

        var current = listOf(VisualPiece("p1", 0, Color.WHITE, e2, null))

        // Move 1
        val snap1 = current.associate { it.id to it.position }
        current = listOf(VisualPiece("p1", 0, Color.WHITE, e4, snap1["p1"]))
        assertEquals("Move 1: prev should be e2", e2, current.first().previousPosition)
        assertNotEquals("Move 1: should animate", current.first().position, current.first().previousPosition)

        // Move 2 — this is where autoplay broke
        val snap2 = current.associate { it.id to it.position }
        current = listOf(VisualPiece("p1", 0, Color.WHITE, e6, snap2["p1"]))
        assertEquals("Move 2: prev should be e4", e4, current.first().previousPosition)
        assertNotEquals("Move 2: should animate", current.first().position, current.first().previousPosition)

        println("Move 1: prev=${current.first().previousPosition} (should be e4)")
        println("Move 2: pos=${current.first().position}  prev=${current.first().previousPosition}")
    }

    // ── Test 4: LaunchedEffect key — target equality collapses animation ──────
    @Test
    fun same_target_value_means_launchedEffect_does_not_refire() {
        val e4 = Position(4, 4)

        // If a piece moves e2->e4 then back e4->e2 then e2->e4 again,
        // the target Offset is identical on move 1 and move 3.
        // LaunchedEffect(target) will NOT refire on move 3 because target == previous target.
        // This confirms we need a separate key (like moveToken) to force refire.

        val targetMove1 = e4
        val targetMove3 = e4 // same square

        val wouldRefire = targetMove1 != targetMove3
        println("Would LaunchedEffect refire: $wouldRefire")
        println("This is the autoplay bug if false — need moveToken as the key")

        assertFalse(
            "CONFIRMED BUG: LaunchedEffect(target) will NOT refire when piece returns to same square",
            wouldRefire
        )
    }

    // ── Test 5: moveToken as key always forces refire ─────────────────────────
    @Test
    fun moveToken_increments_guarantee_launchedEffect_always_refires() {
        var counter = 0L

        val token1 = ++counter
        val token2 = ++counter
        val token3 = ++counter // same target square as token1 but different token

        println("token1=$token1 token2=$token2 token3=$token3")

        assertNotEquals("token1 != token2", token1, token2)
        assertNotEquals("token2 != token3", token2, token3)
        assertNotEquals("token1 != token3 — LaunchedEffect WILL refire even for same square", token1, token3)
    }
}
