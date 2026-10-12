package com.example.chess

import com.example.chess.data.local.BotProgressEntity
import com.example.chess.data.model.BotProfile
import com.example.chess.data.model.RoadmapWorld
import com.example.chess.data.repository.deriveNodeStates
import com.example.chess.domain.model.NodeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoadmapUnlockStateTest {

    private fun bot(id: String) = BotProfile(
        id = id, name = id, elo = 1000, flavorText = "", world = RoadmapWorld.FIRST_MOVES,
        isBoss = false, skillLevel = 1, moveTimeMs = 100,
    )

    private fun progressRow(id: String, stars: Int) = BotProgressEntity(
        botId = id, lastResult = "WIN", starRating = stars, gamesPlayed = 1, lastPlayedAt = 0L,
    )

    private val catalog = listOf(bot("a"), bot("b"), bot("c"))

    @Test
    fun emptyProgress_firstBotIsCurrent_restAreLocked() {
        val result = deriveNodeStates(catalog, emptyMap())

        assertEquals(NodeState.Current, result[0].state)
        assertEquals(NodeState.Locked, result[1].state)
        assertEquals(NodeState.Locked, result[2].state)
    }

    @Test
    fun firstBotCompleted_secondBecomesCurrent() {
        val progress = mapOf("a" to progressRow("a", stars = 2))
        val result = deriveNodeStates(catalog, progress)

        assertEquals(NodeState.Completed(2), result[0].state)
        assertEquals(NodeState.Current, result[1].state)
        assertEquals(NodeState.Locked, result[2].state)
    }

    @Test
    fun allCompleted_noneCurrent() {
        val progress = catalog.associate { it.id to progressRow(it.id, stars = 3) }
        val result = deriveNodeStates(catalog, progress)

        assertTrue(result.all { it.state is NodeState.Completed })
    }

    @Test
    fun progressRecordedOutOfOrder_earlierBotWithNoProgressStaysCurrent() {
        // "c" has a progress row but "b" (earlier in catalog order) does not — "a" is
        // still the first bot with no recorded progress, so it stays Current and "b"
        // stays Locked; "c" still renders Completed, just out of sequence.
        val progress = mapOf("c" to progressRow("c", stars = 1))
        val result = deriveNodeStates(catalog, progress)

        assertEquals(NodeState.Current, result[0].state)
        assertEquals(NodeState.Locked, result[1].state)
        assertEquals(NodeState.Completed(1), result[2].state)
    }
}
