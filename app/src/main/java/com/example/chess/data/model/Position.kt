package com.example.chess.data.model

import androidx.compose.ui.geometry.Offset
import kotlin.math.abs

data class Position(
    val row: Int,
    val column: Int
)

fun Position.toOffset(): Offset {
    return Offset( column.toFloat(), row.toFloat())
}

fun Position.distanceTo(other: Position): Int {
    return maxOf(abs(this.row - other.row), abs(this.column - other.column))
}
