package com.example.chess

import com.example.chess.domain.OpeningDetector
import com.example.chess.domain.model.Opening
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class OpeningDetectorTest {

    private val book = listOf(
        Opening(eco = "C60", name = "Ruy Lopez", moves = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5")),
        Opening(
            eco = "C68", name = "Ruy Lopez: Exchange Variation",
            moves = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6", "b5c6")
        ),
        Opening(eco = "B20", name = "Sicilian Defense", moves = listOf("e2e4", "c7c5")),
    )

    @Test
    fun `returns the longest matching book line`() {
        val played = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5", "a7a6", "b5c6", "d7c6")

        val match = OpeningDetector.detect(played, book)

        assertEquals("Ruy Lopez: Exchange Variation", match?.opening?.name)
        assertEquals(7, match?.matchedPlyCount)
    }

    @Test
    fun `returns the shallower match when the deeper line was not reached`() {
        val played = listOf("e2e4", "e7e5", "g1f3", "b8c6", "f1b5")

        val match = OpeningDetector.detect(played, book)

        assertEquals("Ruy Lopez", match?.opening?.name)
        assertEquals(5, match?.matchedPlyCount)
    }

    @Test
    fun `returns null when no book line matches`() {
        val played = listOf("g1f3", "g8f6")

        val match = OpeningDetector.detect(played, book)

        assertNull(match)
    }

    @Test
    fun `returns null for an empty move list`() {
        val match = OpeningDetector.detect(emptyList(), book)

        assertNull(match)
    }
}
