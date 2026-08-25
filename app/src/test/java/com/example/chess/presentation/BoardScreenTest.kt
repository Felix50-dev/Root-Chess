package com.example.chess.presentation

import com.example.chess.data.model.Color
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.data.model.King
import org.junit.Assert.assertEquals
import org.junit.Test

class BoardScreenTest {

    @Test
    fun calculateMaterialAdvantage_emptyList_returnsZero() {
        val result = calculateMaterialAdvantage(emptyList())
        assertEquals(0, result)
    }

    @Test
    fun calculateMaterialAdvantage_whiteLosesMaterial_returnsNegativeValue() {
        val lostPieces = listOf(
            Pawn(Color.WHITE, Position(0, 0), true),
            Rook(Color.WHITE, Position(0, 0), true)
        )
        // White lost a Pawn (1) and Rook (5), so White's advantage is -6
        val result = calculateMaterialAdvantage(lostPieces)
        assertEquals(-6, result)
    }

    @Test
    fun calculateMaterialAdvantage_blackLosesMaterial_returnsPositiveValue() {
        val lostPieces = listOf(
            Queen(Color.BLACK, Position(0, 0), true),
            Pawn(Color.BLACK, Position(0, 0), true)
        )
        // Black lost a Queen (9) and Pawn (1), so White's advantage is +10
        val result = calculateMaterialAdvantage(lostPieces)
        assertEquals(10, result)
    }

    @Test
    fun calculateMaterialAdvantage_bothLoseMaterial_returnsNetDifference() {
        val lostPieces = listOf(
            Pawn(Color.WHITE, Position(0, 0), true), // -1
            Queen(Color.BLACK, Position(0, 0), true), // +9
            King(Color.WHITE, Position(0, 0), true), // 0 (ignored)
            null // null ignored
        )
        // Net advantage for White = -1 + 9 = +8
        val result = calculateMaterialAdvantage(lostPieces)
        assertEquals(8, result)
    }
}
