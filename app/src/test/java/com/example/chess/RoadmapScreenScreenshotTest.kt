package com.example.chess

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.chess.data.model.BotCatalog
import com.example.chess.domain.model.NodeState
import com.example.chess.domain.model.RoadmapNodeUiState
import com.example.chess.presentation.RoadmapScreen
import com.example.chess.ui.theme.ChessTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

// RoadmapScreen takes plain data (no ViewModel/Hilt), so a bare createComposeRule() is
// enough here — no need for createAndroidComposeRule<MainActivity>(), which would drag
// in Hilt injection and the real Stockfish native startup for a pure layout test.
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RoadmapScreenScreenshotTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // Same shape as RoadmapScreen.kt's own (private) previewState() — a mix of
    // Completed/Current/Locked so every node visual state renders in one screenshot.
    private fun sampleState(): List<RoadmapNodeUiState> = BotCatalog.bots.mapIndexed { index, bot ->
        val state = when {
            index < 3 -> NodeState.Completed(stars = (index % 3) + 1)
            index == 3 -> NodeState.Current
            else -> NodeState.Locked
        }
        RoadmapNodeUiState(bot, state)
    }

    @Test
    fun roadmapScreen_light() {
        composeTestRule.setContent {
            ChessTheme {
                RoadmapScreen(state = sampleState(), rating = 1240, onBotSelected = {}, onBack = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage("roadmap_light.png")
    }

    @Test
    fun roadmapScreen_dark() {
        composeTestRule.setContent {
            ChessTheme(darkTheme = true) {
                RoadmapScreen(state = sampleState(), rating = 1240, onBotSelected = {}, onBack = {})
            }
        }
        composeTestRule.onRoot().captureRoboImage("roadmap_dark.png")
    }
}
