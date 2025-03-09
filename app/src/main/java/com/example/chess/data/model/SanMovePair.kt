package com.example.chess.data.model

data class SANMovePair(
    val moveNumber: Int,
    val whitePieceDrawable: Int?,
    val whiteDestination: String,
    val blackPieceDrawable: Int?,
    val blackDestination: String
)
