package com.example.chess.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PuzzleEntity::class], version = 1, exportSchema = false)
abstract class PuzzleDatabase : RoomDatabase() {
    abstract fun puzzleDao(): PuzzleDao
}
