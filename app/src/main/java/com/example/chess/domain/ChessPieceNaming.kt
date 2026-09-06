package com.example.chess.domain

import com.example.chess.data.model.Bishop
import com.example.chess.data.model.ChessPiece
import com.example.chess.data.model.King
import com.example.chess.data.model.Knight
import com.example.chess.data.model.Pawn
import com.example.chess.data.model.Queen
import com.example.chess.data.model.Rook

fun ChessPiece.toMaterialDescription(): String = when (this) {
    is Queen -> "your queen"
    is Rook -> "a rook"
    is Bishop -> "a bishop"
    is Knight -> "a knight"
    is Pawn -> "a pawn"
    is King -> "material"  // unreachable — a king is never actually captured
}
