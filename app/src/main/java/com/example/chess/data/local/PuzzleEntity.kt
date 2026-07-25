package com.example.chess.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// A puzzle auto-extracted from a Blunder/Mistake the player made in a past game — see
// GamePlay.extractPuzzleCandidates(). `fen` captures the position right before the
// mistake; solving the puzzle means finding `correctMoveUci` again.
@Entity(tableName = "puzzles")
data class PuzzleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val fen: String,
    val correctMoveUci: String,
    val playedMoveUci: String,
    val category: String,
    val sourceGameTimestamp: Long,
    val solved: Boolean = false,
    val createdAt: Long,
)
