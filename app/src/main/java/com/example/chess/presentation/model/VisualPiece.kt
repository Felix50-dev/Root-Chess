package com.example.chess.presentation.model

import androidx.compose.runtime.Immutable
import com.example.chess.data.model.Color
import com.example.chess.data.model.Position

@Immutable
data class VisualPiece(
    val id: String,
    val asset: Int,
    val color: Color,
    val position: Position,
    val previousPosition: Position? = null,
)
