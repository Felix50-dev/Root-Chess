package com.example.chess.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row per roadmap bot the player has ever played at least once. starRating is a
// monotonic best-ever: RoadmapProgressRepository.recordGameResult only ever raises it
// (via max(existing, baseline)), so replaying a beaten bot and losing never un-earns a
// previously earned star. botId matches BotProfile.id.
@Entity(tableName = "bot_progress")
data class BotProgressEntity(
    @PrimaryKey val botId: String,
    val lastResult: String,
    val starRating: Int,
    val gamesPlayed: Int,
    val lastPlayedAt: Long,
)
