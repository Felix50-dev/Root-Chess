package com.example.chess

import com.example.chess.data.model.Bishop
import com.example.chess.data.model.Color
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Position
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook
import com.example.chess.domain.toMaterialDescription
import org.junit.Assert.assertEquals
import org.junit.Test

class ChessPieceNamingTest {

    private val pos = Position(0, 0)

    @Test
    fun `names each piece type`() {
        assertEquals("your queen", Queen(color = Color.WHITE, position = pos, isKilled = false).toMaterialDescription())
        assertEquals("a rook", Rook(color = Color.WHITE, position = pos, isKilled = false).toMaterialDescription())
        assertEquals("a bishop", Bishop(color = Color.WHITE, position = pos, isKilled = false).toMaterialDescription())
        assertEquals("a knight", Knight(color = Color.WHITE, position = pos, isKilled = false).toMaterialDescription())
        assertEquals("a pawn", Pawn(color = Color.WHITE, position = pos, isKilled = false).toMaterialDescription())
    }

    @Test
    fun `piece color does not affect the phrase`() {
        assertEquals(
            Queen(color = Color.WHITE, position = pos, isKilled = false).toMaterialDescription(),
            Queen(color = Color.BLACK, position = pos, isKilled = false).toMaterialDescription(),
        )
    }
}
