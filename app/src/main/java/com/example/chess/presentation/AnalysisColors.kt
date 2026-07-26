package com.example.chess.presentation

import androidx.compose.ui.graphics.Color

val AccentBlue   = Color(0xFF378ADD)
val AccentPurple = Color(0xFF7F77DD)

private val CategoryBrilliant  = Color(0xFF1D9E75)
private val CategoryBest       = Color(0xFF378ADD)
private val CategoryGood       = Color(0xFF639922)
private val CategoryInaccuracy = Color(0xFFBA7517)
private val CategoryMistake    = Color(0xFFD85A30)
private val CategoryBlunder    = Color(0xFFE24B4A)
private val CategoryMiss       = Color(0xFFD4537E)
private val CategoryMissedMate = Color(0xFF7F77DD)
private val CategoryDefault    = Color(0xFF888888)

fun categoryColor(type: String): Color = when (type) {
    "Brilliant"                -> CategoryBrilliant
    "Best"                     -> CategoryBest
    "Great"                    -> CategoryGood
    "Inaccuracy", "Inaccurate" -> CategoryInaccuracy
    "Mistake"                  -> CategoryMistake
    "Blunder"                  -> CategoryBlunder
    "Miss"                     -> CategoryMiss
    "MissedMate"               -> CategoryMissedMate
    else                       -> CategoryDefault
}

fun categoryIcon(type: String): String = when (type) {
    "Brilliant"                -> "✦"
    "Best"                     -> "✓"
    "Great"                    -> "↑"
    "Inaccuracy", "Inaccurate" -> "?!"
    "Mistake"                  -> "?"
    "Blunder"                  -> "??"
    "Miss"                     -> "!"
    "MissedMate"               -> "M"
    else                       -> "·"
}
