package com.example.chess.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PuzzleDao {

    @Insert
    suspend fun insertAll(puzzles: List<PuzzleEntity>)

    @Query("SELECT * FROM puzzles ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<PuzzleEntity>>

    @Query("SELECT * FROM puzzles WHERE solved = 0 ORDER BY createdAt DESC")
    fun observeUnsolved(): Flow<List<PuzzleEntity>>

    @Query("SELECT * FROM puzzles WHERE id = :id")
    suspend fun getById(id: Long): PuzzleEntity?

    @Update
    suspend fun update(puzzle: PuzzleEntity)
}
