package com.example.chess.data.model

// The human player's result in a COMPUTER-mode game, from their own side of the board —
// already resolved relative to which color they played. Feeds RoadmapProgressRepository's
// Elo update; unrelated to GameStatus, which is the raw board-state outcome (WHITE_WIN/
// BLACK_WIN/STALEMATE) before color is factored in.
enum class GameOutcome { WIN, LOSS, DRAW }
