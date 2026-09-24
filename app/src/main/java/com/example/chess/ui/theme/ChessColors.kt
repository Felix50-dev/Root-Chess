package com.example.chess.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Chess-domain color tokens that have no Material 3 role: board squares, board overlays,
// and success states (M3 has no "success"). Accessed via ChessTheme.colors (Theme.kt).
@Immutable
data class ChessColors(
    val boardLight: Color,      // light squares
    val boardDark: Color,       // dark squares
    val boardLegalDot: Color,   // legal-move dot on empty squares
    val boardSelected: Color,   // selection / capturable-target ring
    val boardLastMove: Color,   // last-move highlight overlay
    val boardCheck: Color,      // king-in-check border
    val evalAdvantage: Color,   // eval-bar advantage fill
    val success: Color,
    val onSuccess: Color,
)

val LightChessColors = ChessColors(
    boardLight = Color(0xFFF0D9B5),
    boardDark = Color(0xFFA26232),
    boardLegalDot = Color(0x8800C853),
    boardSelected = Color(0xFFFFB300),
    boardLastMove = Color(0x4D00C853),
    boardCheck = Color(0xFFE53935),
    evalAdvantage = Color(0xFF4CDA24),
    success = Color(0xFF2E7D32),
    onSuccess = Color(0xFFFFFFFF),
)

// Board colors are deliberately IDENTICAL in dark mode — a chess board must not invert;
// piece contrast and player muscle memory depend on the fixed square colors.
val DarkChessColors = LightChessColors.copy(
    success = Color(0xFF81C784),
    onSuccess = Color(0xFF0A3D12),
)

internal val LocalChessColors = staticCompositionLocalOf { LightChessColors }
