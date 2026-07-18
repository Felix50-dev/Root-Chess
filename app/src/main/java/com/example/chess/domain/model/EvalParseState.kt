package com.example.chess.domain.model

data class EvalParseState(
    var score: Int = 0,
    var bestMove: String = "",
    var mateIn: Int = 0,
    var pv: List<String> = emptyList(),
    var depth: Int = 0,
    var done: Boolean = false,
)
