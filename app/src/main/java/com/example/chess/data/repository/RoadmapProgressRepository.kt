package com.example.chess.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.chess.data.local.BotProgressDao
import com.example.chess.data.local.BotProgressEntity
import com.example.chess.data.model.BotCatalog
import com.example.chess.data.model.GameOutcome
import com.example.chess.domain.model.RoadmapNodeUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow
import kotlin.math.roundToInt

// Owns the two pieces of state the roadmap needs: the player's own Elo-style rating
// (DataStore, shared "app_prefs" instance — same precedent as AiReviewQuotaManager) and
// per-bot progress (Room, same PuzzleDatabase as PuzzleEntity). recordGameResult() is the
// only writer of rating — it runs at game-end regardless of whether the player ever opens
// post-game analysis; applyAccuracyBonus() is a separate, optional upgrade path that only
// ever raises an already-recorded star rating, never gates or lowers it.
@Singleton
class RoadmapProgressRepository @Inject constructor(
    private val botProgressDao: BotProgressDao,
    private val dataStore: DataStore<Preferences>,
) {
    fun observeRating(): Flow<Int> =
        dataStore.data.map { it[KEY_RATING] ?: DEFAULT_RATING }

    fun observeRoadmapState(): Flow<List<RoadmapNodeUiState>> =
        botProgressDao.observeAll().map { rows ->
            deriveNodeStates(BotCatalog.bots, rows.associateBy { it.botId })
        }

    suspend fun recordGameResult(botId: String, outcome: GameOutcome) {
        val bot = BotCatalog.bots.firstOrNull { it.id == botId } ?: return
        val score = when (outcome) {
            GameOutcome.WIN -> 1.0
            GameOutcome.DRAW -> 0.5
            GameOutcome.LOSS -> 0.0
        }

        dataStore.edit { prefs ->
            val rating = prefs[KEY_RATING] ?: DEFAULT_RATING
            val expected = 1.0 / (1.0 + 10.0.pow((bot.elo - rating) / 400.0))
            val newRating = (rating + K_FACTOR * (score - expected))
                .roundToInt()
                .coerceAtLeast(MIN_RATING)
            prefs[KEY_RATING] = newRating
        }

        val existing = botProgressDao.getByBotId(botId)
        val baselineStars = when (outcome) {
            GameOutcome.WIN -> 2
            GameOutcome.DRAW -> 1
            GameOutcome.LOSS -> 0
        }
        botProgressDao.upsert(
            BotProgressEntity(
                botId = botId,
                lastResult = outcome.name,
                starRating = maxOf(existing?.starRating ?: 0, baselineStars),
                gamesPlayed = (existing?.gamesPlayed ?: 0) + 1,
                lastPlayedAt = System.currentTimeMillis(),
            )
        )
    }

    // Only ever raises starRating to 3 on a WIN with a strong accuracy figure from
    // analyzeGame() — never called if the player skips analysis, and never downgrades
    // whatever recordGameResult() already committed.
    suspend fun applyAccuracyBonus(botId: String, accuracy: Double) {
        if (accuracy < ACCURACY_BONUS_THRESHOLD) return
        val existing = botProgressDao.getByBotId(botId) ?: return
        if (existing.lastResult != GameOutcome.WIN.name) return
        if (existing.starRating >= 3) return
        botProgressDao.upsert(existing.copy(starRating = 3))
    }

    companion object {
        const val DEFAULT_RATING = 400
        const val MIN_RATING = 100
        const val K_FACTOR = 32.0
        const val ACCURACY_BONUS_THRESHOLD = 85.0
        val KEY_RATING = intPreferencesKey("player_rating")
    }
}
