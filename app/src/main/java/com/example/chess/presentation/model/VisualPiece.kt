package com.example.chess.presentation.model

import com.example.chess.data.model.Color
import com.example.chess.data.model.Position

data class VisualPiece(
    val id: String,
    val asset: Int,
    val color: Color,
    val position: Position,
    val previousPosition: Position? = null,
)
