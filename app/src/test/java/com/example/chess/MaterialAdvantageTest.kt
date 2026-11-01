package com.example.chess

import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.presentation.calculateMaterialAdvantage
import org.junit.Assert.assertEquals
import org.junit.Test

class MaterialAdvantageTest {

    @Test
    fun `empty list returns zero advantage`() {
        assertEquals(0, calculateMaterialAdvantage(emptyList()))
    }

    @Test
    fun `list with nulls and kings ignores them`() {
        val pieces = listOf(
            null,
            King(color = Color.WHITE, position = Position(0, 0), isKilled = true),
            King(color = Color.BLACK, position = Position(7, 7), isKilled = true),
            null
        )
        assertEquals(0, calculateMaterialAdvantage(pieces))
    }

    @Test
    fun `white lost pawn results in black advantage (-1 for white)`() {
        val pieces = listOf(
            Pawn(color = Color.WHITE, position = Position(1, 0), isKilled = true)
        )
        assertEquals(-1, calculateMaterialAdvantage(pieces))
    }

    @Test
    fun `black lost queen results in white advantage (+9 for white)`() {
        val pieces = listOf(
            Queen(color = Color.BLACK, position = Position(7, 3), isKilled = true)
        )
        assertEquals(9, calculateMaterialAdvantage(pieces))
    }

    @Test
    fun `calculates net advantage across multiple mixed pieces`() {
        // White lost: Pawn (1), Rook (5) -> -6
        // Black lost: Knight (3), Queen (9) -> +12
        // Net: +6
        val pieces = listOf(
            Pawn(color = Color.WHITE, position = Position(1, 0), isKilled = true),
            Knight(color = Color.BLACK, position = Position(7, 1), isKilled = true),
            Rook(color = Color.WHITE, position = Position(0, 0), isKilled = true),
            Queen(color = Color.BLACK, position = Position(7, 3), isKilled = true),
            King(color = Color.BLACK, position = Position(7, 4), isKilled = true),
            null
        )
        assertEquals(6, calculateMaterialAdvantage(pieces))
    }
}
