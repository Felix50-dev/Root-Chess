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
    fun emptyListReturnsZeroAdvantage() {
        assertEquals(0, calculateMaterialAdvantage(emptyList()))
    }

    @Test
    fun listWithNullsReturnsZeroAdvantage() {
        assertEquals(0, calculateMaterialAdvantage(listOf(null, null)))
    }

    @Test
    fun whitePiecesLostGiveNegativeAdvantage() {
        val lost = listOf(
            Pawn(color = Color.WHITE, position = Position(1, 0), isKilled = true), // -1
            Knight(color = Color.WHITE, position = Position(0, 1), isKilled = true), // -3
            Bishop(color = Color.WHITE, position = Position(0, 2), isKilled = true), // -3
            Rook(color = Color.WHITE, position = Position(0, 0), isKilled = true), // -5
            Queen(color = Color.WHITE, position = Position(0, 3), isKilled = true), // -9
            King(color = Color.WHITE, position = Position(0, 4), isKilled = true), // 0
        )
        // White lost: 1 + 3 + 3 + 5 + 9 + 0 = 21 -> whiteAdvantage = -21
        assertEquals(-21, calculateMaterialAdvantage(lost))
    }

    @Test
    fun blackPiecesLostGivePositiveAdvantage() {
        val lost = listOf(
            Pawn(color = Color.BLACK, position = Position(6, 0), isKilled = true), // +1
            Knight(color = Color.BLACK, position = Position(7, 1), isKilled = true), // +3
            Bishop(color = Color.BLACK, position = Position(7, 2), isKilled = true), // +3
            Rook(color = Color.BLACK, position = Position(7, 0), isKilled = true), // +5
            Queen(color = Color.BLACK, position = Position(7, 3), isKilled = true), // +9
            King(color = Color.BLACK, position = Position(7, 4), isKilled = true), // 0
        )
        // Black lost: 1 + 3 + 3 + 5 + 9 + 0 = 21 -> whiteAdvantage = +21
        assertEquals(21, calculateMaterialAdvantage(lost))
    }

    @Test
    fun balancedLossesReturnZeroAdvantage() {
        val lost = listOf(
            Pawn(color = Color.WHITE, position = Position(1, 0), isKilled = true),
            Pawn(color = Color.BLACK, position = Position(6, 0), isKilled = true),
            Queen(color = Color.WHITE, position = Position(0, 3), isKilled = true),
            Queen(color = Color.BLACK, position = Position(7, 3), isKilled = true),
        )
        assertEquals(0, calculateMaterialAdvantage(lost))
    }
}
