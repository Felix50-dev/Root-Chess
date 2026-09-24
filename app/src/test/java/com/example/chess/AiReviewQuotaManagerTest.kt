package com.example.chess

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.chess.data.billing.AiReviewQuotaManager
import com.example.chess.data.billing.EpochClock
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class AiReviewQuotaManagerTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Mutable "now" so tests can cross midnight deterministically.
    private var nowMillis = 1_757_300_000_000L
    private val clock = EpochClock { nowMillis }

    private var fileCounter = 0

    private fun newManager(): AiReviewQuotaManager {
        val file = File(tmp.root, "quota${fileCounter++}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        return AiReviewQuotaManager(dataStore, clock)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun firstClaimOfTheDay_isAllowed() = runBlocking {
        assertTrue(newManager().claimOrCheck("gameA"))
    }

    @Test
    fun sameGame_staysAllowedAllDay() = runBlocking {
        val quota = newManager()
        assertTrue(quota.claimOrCheck("gameA"))
        // Stepping back and forth through the same review re-checks repeatedly.
        assertTrue(quota.claimOrCheck("gameA"))
        assertTrue(quota.claimOrCheck("gameA"))
    }

    @Test
    fun secondGameSameDay_isDenied() = runBlocking {
        val quota = newManager()
        assertTrue(quota.claimOrCheck("gameA"))
        assertFalse(quota.claimOrCheck("gameB"))
        // And the denied game did not steal the slot from the allowed one.
        assertTrue(quota.claimOrCheck("gameA"))
    }

    @Test
    fun midnightRollover_freesTheSlotForANewGame() = runBlocking {
        val quota = newManager()
        assertTrue(quota.claimOrCheck("gameA"))
        assertFalse(quota.claimOrCheck("gameB"))

        nowMillis += 24 * 60 * 60 * 1000L

        // New day: first claimant wins the fresh slot...
        assertTrue(quota.claimOrCheck("gameB"))
        // ...and yesterday's game no longer holds it.
        assertFalse(quota.claimOrCheck("gameA"))
    }
}
