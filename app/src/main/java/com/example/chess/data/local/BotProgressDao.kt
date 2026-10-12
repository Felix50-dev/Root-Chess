package com.example.chess.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BotProgressDao {

    @Query("SELECT * FROM bot_progress")
    fun observeAll(): Flow<List<BotProgressEntity>>

    @Query("SELECT * FROM bot_progress WHERE botId = :botId")
    suspend fun getByBotId(botId: String): BotProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: BotProgressEntity)
}
