package com.example.chess

import com.example.chess.data.model.Bishop
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
    fun emptyListReturnsZero() {
        val advantage = calculateMaterialAdvantage(emptyList())
        assertEquals(0, advantage)
    }

    @Test
    fun listWithNullsReturnsZero() {
        val advantage = calculateMaterialAdvantage(listOf(null, null))
        assertEquals(0, advantage)
    }

    @Test
    fun whitePieceLostDecreasesAdvantage() {
        val whitePawn = Pawn(Color.WHITE, Position(0, 0), isKilled = true)
        val advantage = calculateMaterialAdvantage(listOf(whitePawn))
        assertEquals(-1, advantage)
    }

    @Test
    fun blackPieceLostIncreasesAdvantage() {
        val blackQueen = Queen(Color.BLACK, Position(0, 0), isKilled = true)
        val advantage = calculateMaterialAdvantage(listOf(blackQueen))
        assertEquals(9, advantage)
    }

    @Test
    fun mixedLostPiecesCalculatedCorrectly() {
        val pos = Position(0, 0)
        val lostPieces = listOf(
            Pawn(Color.WHITE, pos, isKilled = true),   // -1
            Rook(Color.BLACK, pos, isKilled = true),   // +5
            Bishop(Color.BLACK, pos, isKilled = true), // +3
            Knight(Color.WHITE, pos, isKilled = true), // -3
            null
        )
        val advantage = calculateMaterialAdvantage(lostPieces)
        assertEquals(4, advantage)
    }

    @Test
    fun kingsAreIgnored() {
        val pos = Position(0, 0)
        val lostPieces = listOf(
            King(Color.WHITE, pos, isKilled = true),
            King(Color.BLACK, pos, isKilled = true)
        )
        val advantage = calculateMaterialAdvantage(lostPieces)
        assertEquals(0, advantage)
    }
}
