package com.example.chess

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.example.chess.data.local.BotProgressDao
import com.example.chess.data.local.BotProgressEntity
import com.example.chess.data.model.GameOutcome
import com.example.chess.data.repository.RoadmapProgressRepository
import com.example.chess.domain.model.NodeState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

private class FakeBotProgressDao : BotProgressDao {
    private val rows = mutableMapOf<String, BotProgressEntity>()
    private val flow = MutableStateFlow<List<BotProgressEntity>>(emptyList())

    override fun observeAll(): Flow<List<BotProgressEntity>> = flow

    override suspend fun getByBotId(botId: String): BotProgressEntity? = rows[botId]

    override suspend fun upsert(entity: BotProgressEntity) {
        rows[entity.botId] = entity
        flow.value = rows.values.toList()
    }
}

class RoadmapProgressRepositoryTest {

    @get:Rule
    val tmp = TemporaryFolder()

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var fileCounter = 0

    private fun newRepository(): RoadmapProgressRepository {
        val file = File(tmp.root, "rating${fileCounter++}.preferences_pb")
        val dataStore = PreferenceDataStoreFactory.create(scope = scope) { file }
        return RoadmapProgressRepository(FakeBotProgressDao(), dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
    }

    @Test
    fun recordGameResult_win_increasesRating() = runBlocking {
        val repo = newRepository()
        val before = repo.observeRating().first()

        repo.recordGameResult("pip", GameOutcome.WIN)

        assertTrue(repo.observeRating().first() > before)
    }

    @Test
    fun recordGameResult_loss_decreasesOrHoldsRating() = runBlocking {
        val repo = newRepository()
        val before = repo.observeRating().first()

        repo.recordGameResult("grandmaster", GameOutcome.LOSS)

        assertTrue(repo.observeRating().first() <= before)
    }

    @Test
    fun recordGameResult_lossAgainstSimilarlyRatedBot_dropsMoreThanLossAgainstMuchStrongerBot() = runBlocking {
        // Losing to an opponent close to your own rating is more "expected" to go the
        // other way, so it should cost more rating than losing to a vastly stronger one.
        val repoSimilar = newRepository()
        repoSimilar.recordGameResult("pip", GameOutcome.LOSS) // pip: elo 450, close to DEFAULT_RATING 400
        val afterSimilar = repoSimilar.observeRating().first()

        val repoFar = newRepository()
        repoFar.recordGameResult("grandmaster", GameOutcome.LOSS) // elo 2600, far above 400
        val afterFar = repoFar.observeRating().first()

        val dropSimilar = RoadmapProgressRepository.DEFAULT_RATING - afterSimilar
        val dropFar = RoadmapProgressRepository.DEFAULT_RATING - afterFar
        assertTrue(dropSimilar > dropFar)
    }

    @Test
    fun starRating_neverDowngradesOnReplayLoss() = runBlocking {
        val repo = newRepository()

        repo.recordGameResult("theo", GameOutcome.WIN) // baseline stars = 2
        repo.recordGameResult("theo", GameOutcome.LOSS) // baseline for LOSS = 0, but must not downgrade

        assertEquals(2, starsOf(repo, "theo"))
    }

    @Test
    fun applyAccuracyBonus_upgradesStarsOnlyAfterAWin_andOnlyAboveThreshold() = runBlocking {
        val repo = newRepository()
        repo.recordGameResult("theo", GameOutcome.WIN) // baseline stars = 2

        repo.applyAccuracyBonus("theo", accuracy = 50.0) // below threshold — no change
        assertEquals(2, starsOf(repo, "theo"))

        repo.applyAccuracyBonus("theo", accuracy = 90.0) // above threshold — upgrades to 3
        assertEquals(3, starsOf(repo, "theo"))
    }

    @Test
    fun applyAccuracyBonus_doesNothingWhenLastResultWasNotAWin() = runBlocking {
        val repo = newRepository()
        repo.recordGameResult("theo", GameOutcome.LOSS) // baseline stars = 0, lastResult = LOSS

        repo.applyAccuracyBonus("theo", accuracy = 99.0)

        assertEquals(0, starsOf(repo, "theo"))
    }

    private suspend fun starsOf(repo: RoadmapProgressRepository, botId: String): Int =
        (repo.observeRoadmapState().first().first { it.bot.id == botId }.state as NodeState.Completed).stars
}
