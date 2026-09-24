package com.example.chess.data.billing

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

// Injectable so tests can control "now"; prod binding is System::currentTimeMillis.
fun interface EpochClock {
    fun nowMillis(): Long
}

// Free-tier limit: ONE game per local calendar day may have AI move explanations. The
// first game whose review requests an explanation claims the day's slot; that same game
// stays allowed all day (stepping back and forth must not burn anything), every other
// game is denied until the date rolls over.
@Singleton
class AiReviewQuotaManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val clock: EpochClock,
) {

    // Check-and-claim in a single dataStore.edit so concurrent explanation launches
    // can't each claim the slot for a different game.
    suspend fun claimOrCheck(gameKey: String): Boolean {
        val today = dayString(clock.nowMillis())
        var allowed = false
        dataStore.edit { prefs ->
            when {
                prefs[KEY_DATE] != today -> {
                    prefs[KEY_DATE] = today
                    prefs[KEY_GAME] = gameKey
                    allowed = true
                }
                prefs[KEY_GAME] == gameKey -> allowed = true
                else -> allowed = false
            }
        }
        return allowed
    }

    // Local calendar day on purpose ("per day" should match the user's clock).
    // SimpleDateFormat because minSdk 24 has no java.time without desugaring.
    private fun dayString(epochMillis: Long): String =
        SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(epochMillis))

    private companion object {
        val KEY_DATE = stringPreferencesKey("ai_review_date")
        val KEY_GAME = stringPreferencesKey("ai_review_game_key")
    }
}
