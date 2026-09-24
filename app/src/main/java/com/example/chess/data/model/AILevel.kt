package com.example.chess.data.model

// skillLevel is Stockfish's "Skill Level" UCI option (0..20). moveTimeMs must be large
// enough for a real search — Skill Level weakens play by sampling from searched moves,
// so with a ~1ms search every level plays equally badly.
enum class AILevel(val skillLevel: Int, val moveTimeMs: Int) {
    LEVEL1(3, 300),
    LEVEL2(8, 600),
}
